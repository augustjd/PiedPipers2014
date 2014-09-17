package piedpipers.interceptor;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.Point;
import piedpipers.sim.Piedpipers;
import java.awt.Color;

public class Scene {
    public int remaining;

    public static final double RANGE_OF_INFLUENCE = 10;

    public List<Integer> rats   = new ArrayList<Integer>();
    public List<Vector> pipers = new ArrayList<Vector>();
    public List<Vector> rat_vectors = new ArrayList<Vector>();
    public List<Vector> live_rat_vectors = new ArrayList<Vector>();

    public enum Side {
        LEFT,
        RIGHT
    };

    public int dimension;
    public Side getSide(Point p) {
        if (p.x > this.dimension / 2) {
            return Side.RIGHT;
        } else {
            return Side.LEFT;
        }
    }
    public Side getSide(Vector v) {
        try {
        if (v.x > this.dimension / 2.0) {
            return Side.RIGHT;
        } else {
            return Side.LEFT;
        }
        } catch(NullPointerException e) {
            return Side.RIGHT;
        }
    }

    public Vector getGatePosition() {
        return new Vector(this.dimension/2.0, this.dimension/2.0);
    }

    public int getRemainingRats() {
        return this.rats.size();
    }

    public double getRatsDensity() {
        return this.getRemainingRats() / (dimension * dimension / 2.0);
    }

    public Vector magnetPosition() {
        return this.pipers.get(0);
    }

    public Vector closestPiper(Vector p) {
        double distance = Double.POSITIVE_INFINITY;
        Vector best = null;

        for(Vector piper : pipers) {
            double current_distance = piper.distanceTo(p);
            if (current_distance < distance) {
                distance = current_distance;
                best = piper;
            }
        }

        return best;
    }

    public int getRatsUnderInfluenceOfPiper(Point piper) {
        int i = 0;
        for (Point p : absolute_rats) {
            if (Math.sqrt((p.x - piper.x) * (p.x-piper.x) + (p.y-piper.y) * (p.y-piper.y)) < RANGE_OF_INFLUENCE) {
                i++;
            }
        }
        return i;
    }

    public boolean underInfluenceOfPiper(Vector rat, Vector piper) {
        return piper.distanceTo(rat) < RANGE_OF_INFLUENCE;
    }

    public boolean underInfluenceOfAnyPiper(Vector rat, List<Vector> pipers) {
        for (Vector p : pipers) {
            if (underInfluenceOfPiper(rat, p)) {
                return true;
            }
        }
        return false;
    }

    public Vector getPlayerPosition(Player p) {
        return p.getPosition(this.pipers);
    }

    public void printRemainingRats() {
        System.out.format("%d rats remain. Density: %f\n", this.getRemainingRats(), this.getRatsDensity());
    }

    public Point[] absolute_rats;

    public Vector[] rat_velocities;

    public boolean onField(Vector v) {
        return v.x >= 0 && v.x < dimension && 
               v.y < dimension && v.y >= 0;
    }

    public double timeOfNextBounce(int rat_index) {
        Vector rat_pos = rat_velocities[rat_index];
        Vector rat_vel = rat_vectors.get(rat_index);

        double time_to_right_wall;
        double time_to_left_wall;

        double time_to_top_wall;
        double time_to_bottom_wall;

        if (rat_vel.x > 0) {
            time_to_right_wall = (this.dimension - rat_pos.x) / rat_vel.x;
            time_to_left_wall = Double.POSITIVE_INFINITY;
        } else {
            time_to_right_wall = Double.POSITIVE_INFINITY;
            time_to_left_wall = (rat_pos.x - (this.dimension/2.0)) / rat_vel.x;
            if (rat_vel.scale(time_to_left_wall).add(rat_pos).x == this.dimension/2.0) {
                // we hit the gate! so basically no bounce.
                time_to_left_wall = Double.POSITIVE_INFINITY;
            }
        }

        if (rat_vel.y > 0) {
            time_to_bottom_wall = (this.dimension - rat_pos.y) / rat_vel.y;
            time_to_top_wall = Double.POSITIVE_INFINITY;
        } else {
            time_to_bottom_wall = Double.POSITIVE_INFINITY;
            time_to_top_wall = (rat_pos.y - (this.dimension)) / rat_vel.y;
        }
        return 0;
    }

    Piedpipers pp;
    public Scene(int dimension, Point[] pipers, Point[] rats, int[] thetas, Piedpipers pp) {
        this.pp = pp;
        this.dimension = dimension;
        this.absolute_rats = rats;
        for (Point p : pipers) {
            this.pipers.add(new Vector(p));
        }

        this.rat_velocities = new Vector[rats.length];

        for (int i = 0; i < rats.length; i++) {
            Point r = rats[i];
            Vector v = new Vector(r);
            this.rat_vectors.add(v);

            double rat_theta = thetas[i] * 2.0 * Math.PI / 360.0;

            this.rat_velocities[i] = new Vector(Math.cos(rat_theta), Math.sin(rat_theta)).scale_ip(Piedpipers.WALK_SPEED);

            if (getSide(r) == Side.RIGHT && !underInfluenceOfAnyPiper(v, this.pipers)) {
                this.rats.add(new Integer(i));
                this.live_rat_vectors.add(v);
            }
        }
    }
}
