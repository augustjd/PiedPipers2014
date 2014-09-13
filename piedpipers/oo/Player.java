package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {

    public static final double EPSILON = 0.01;
    public static class Vector {
        public double x;
        public double y;

        public Vector(Point p) {
            this(p.x, p.y); 
        }
        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vector sub(double x, double y) {
            return new Vector(this.x - x, this.y - y);
        }

        public Vector sub_ip(double dx, double dy) {
            this.x -= dx;
            this.y -= dy;
            return this;
        }

        public Vector sub(Vector v) {
            return this.sub(v.x, v.y);
        }
        public Vector sub_ip(Vector v) {
            return this.sub_ip(v.x, v.y);
        }

        public Vector add(double dx, double dy) {
            return new Vector(dx + this.x, dy + this.y);
        }
        public Vector add_ip(double dx, double dy) {
            this.x += dx;
            this.y += dy;
            return this;
        }

        public Vector add(Vector v) {
            return this.add(v.x, v.y);
        }
        public Vector add_ip(Vector v) {
            return this.add_ip(v.x, v.y);
        }

        public double dot(Vector v) {
            return this.x * v.x + this.y * v.y;
        }

        public Vector scale(double factor) {
            return new Vector(x * factor, y * factor);
        }

        public Vector scale_ip(double factor) {
            this.x *= factor;
            this.y *= factor;
            return this;
        }

        public Vector unit() {
            return this.scale(1/this.length());
        }

        public Vector unit_ip() {
            return this.scale_ip(1/this.length());
        }

        public double length() {
            return Math.sqrt(x*x + y*y);
        }

        public double distanceTo(Vector b) {
            return Math.sqrt((this.x - b.x) * (this.x - b.x) + (this.y - b.y) * (this.y - b.y));
        }

        public boolean equals(Vector b) {
            return this.x == b.x && this.y == b.y;
        }

        public Point asPoint() {
            return new Point(this.x, this.y);
        }

        public static Vector fromPointsDifference(Point a, Point b) {
            return new Vector(b.x - a.x, b.y - a.y);
        }

        public String toString() {
            return String.format("(%f, %f)", this.x, this.y);
        }
    }

    public abstract static class Strategy {
        public abstract Point generateMove(Player p, Scene s);

        public abstract String getName();
    }

    public static class TargetStrategy extends Strategy {
        Vector target;
        Vector intermediate_target;

        public TargetStrategy(Vector target) {
            this.target = target;
            this.intermediate_target = target;
        }

        public static final int GATE_BUFFER = 2;

        public Vector getSideOfGate(Scene s, Scene.Side side) {
            if (side == Scene.Side.RIGHT) {
                return s.getGatePosition().add_ip(GATE_BUFFER, 0);
            } else {
                return s.getGatePosition().sub_ip(GATE_BUFFER, 0);
            }
        }

        public Scene.Side getOppositeSide(Scene.Side side) {
            if (side == Scene.Side.RIGHT) {
                return Scene.Side.LEFT;
            } else {
                return Scene.Side.RIGHT;
            }
        }

        boolean crossed_gate = false;

        public Vector getGateTarget(Scene.Side player_side, Scene.Side destination_side, Scene s, Player p) {
            Vector position = s.getPlayerPosition(p);
            if (position.equals(this.intermediate_target)) {
                if (crossed_gate) {
                    return this.target;
                } else {
                    crossed_gate = true;
                    return getSideOfGate(s, destination_side);
                }
            } else {
                /*System.out.format("Intermediate is player side: %s Distance to target: %f\n", player_side.toString(),
                        position.distanceTo(intermediate_target));*/
                return getSideOfGate(s, player_side);
            }
        }



        @Override
        public Point generateMove(Player p, Scene s) {
            Vector position = s.getPlayerPosition(p);
            if (this.reachedTarget(p,s)) {
                System.out.println("Reached target.");
                p.chooseStrategy(s);
            } else {
                Scene.Side player_side      = s.getSide(position);
                Scene.Side destination_side = s.getSide(this.target);

                if (player_side != destination_side && !position.equals(intermediate_target)) {
                    this.intermediate_target = s.getGatePosition();
                } else {
                    this.intermediate_target = this.target;
                }
            }

            Vector difference = this.intermediate_target.sub(position);

            double step_size = Math.min(p.getSpeed(), difference.length());

            if (step_size == 0) {
                if (!this.intermediate_target.equals(this.target)) {
                    this.intermediate_target = this.target;
                }
                return position.asPoint();
            }

            Vector step = difference.unit_ip().scale_ip(step_size).add_ip(position);

            return step.asPoint();
        }

        private boolean reachedTarget(Player p, Scene s) {
            return this.target.distanceTo(s.getPlayerPosition(p)) < EPSILON;
        }

        public String getName() { return "TargetStrategy"; }
    }

    public static class FartherFromGateComparator extends Comparator<Vector> {
        Scene s;
        public FartherFromGateComparator(Scene s) {
            this.s = s;
        }

        public int compare(int i1, int i2) {
            Vector gate = s.getGatePosition();
            return new Double(s.rats_vector.get(i1).distanceTo(gate)).compareTo(s.rats_vector.get(i2).distanceTo(gate));
        }
    }

    public static class RatTargetStrategy extends TargetStrategy {
        int rat_index;
        static Random rand = new Random();
        public RatTargetStrategy(Player p, Scene s) {
            super(s.getGatePosition());
            this.rat_index = this.chooseRat(p, s);
        }

        public int chooseRat(Player p, Scene s) {
            double furthest = 0;
            int closest_index = 0;

            Vector position = s.getPlayerPosition(p);

            Collections.sort(s.rats, new FartherFromGateComparator(s));

            closest_index = s.rats[id];
            assert(s.getSide(s.absolute_rats[closest_index]) == Scene.Side.RIGHT);
            System.out.format("%d: %s\n", closest_index,  s.rat_vectors.get(closest_index));
            return closest_index; 
        }


        public static final int RATS_UNTIL_DROPOFF = 5;
        @Override
        public Point generateMove(Player p, Scene s) {
            if (nearTargetRat(p, s)) {
                p.turnOnMusic();

                int rats_influenced = s.getRatsUnderInfluenceOfPiper(s.getPlayerPosition(p).asPoint());
                if (rats_influenced >= RATS_UNTIL_DROPOFF ||
                    s.getRemainingRats() == 0) {
                    p.setStrategy(new ReturnToGateStrategy(s));
                } else {
                    this.rat_index = chooseRat(p, s);
                    if (this.rat_index < 0) {
                    }
                }
            }

            this.target = getTargetRatPosition(s);
            return super.generateMove(p, s);
        }

        public Vector getTargetRatPosition(Scene s) {
            return new Vector(s.absolute_rats[rat_index]);
        }

        public boolean nearTargetRat(Player p, Scene s) {
            Vector position = s.getPlayerPosition(p);
            Vector target = getTargetRatPosition(s);
            return position.distanceTo(target) < Scene.RANGE_OF_INFLUENCE &&
                s.getSide(position) == s.getSide(target);
        }

        public String getName() { return "RatTargetStrategy"; }
    }

    public static class ReturnToGateStrategy extends TargetStrategy {
        boolean crossed_gate = false;
        private static final double GATE_BUFFER = 10.0;
        public ReturnToGateStrategy(Scene s) {
            super(s.getGatePosition().add_ip(GATE_BUFFER,0));
        }
        public Point generateMove(Player p, Scene s) {
            if (super.reachedTarget(p, s) && crossed_gate) {
                p.turnOffMusic();
            } else if (super.reachedTarget(p, s)) {
                this.target = s.getGatePosition().sub_ip(2*GATE_BUFFER,GATE_BUFFER);
                crossed_gate = true;
            }
            return super.generateMove(p,s);
        }
    }

    private Strategy strategy = null;


    public void turnOnMusic() {
        this.music = true;
    }
    public void turnOffMusic() {
        this.music = false;
    }
    public void setStrategy(Strategy newStrategy) {
        this.strategy = newStrategy;
    }

    public Vector getPosition(List<Vector> pipers) {
        return pipers.get(id);
    }

    public static class Scene {
        public int remaining;

        public static final double RANGE_OF_INFLUENCE = 10;

        public Set<Integer> rats   = new HashSet<Integer>();
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
            if (v.x > this.dimension / 2) {
                return Side.RIGHT;
            } else {
                return Side.LEFT;
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

        public Scene(int dimension, Point[] pipers, Point[] rats) {
            this.dimension = dimension;
            this.absolute_rats = rats;
            for (Point p : pipers) {
                this.pipers.add(new Vector(p));
            }

            for (int i = 0; i < rats.length; i++) {
                Point r = rats[i];
                Vector v = new Vector(r);
                this.rat_vectors.add(v);

                if (getSide(r) == Side.RIGHT && !underInfluenceOfAnyPiper(v, this.pipers)) {
                    this.rats.add(new Integer(i));
                    this.live_rat_vectors.add(v);
                }
            }
        }
    }

	static int npipers;

	static double radius_of_influence = 10.0;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;

    public double getSpeed() {
        if (this.music) {
            return mpspeed;
        } else {
            return pspeed;
        }
    }
	
    public void init() {
    }

    public void chooseStrategy(Scene s) {
        this.setStrategy(new RatTargetStrategy(this, s));
    }

	public Point move(Point[] pipers, Point[] rats) {
        Scene s = new Scene(dimension, pipers, rats);
        if (strategy == null) {
            System.out.println(id);
            double theta = id * Math.PI / pipers.length;
            System.out.println(theta);

            Vector target = new Vector(dimension/2 + 10 * Math.sin(theta),
                                       dimension/2 + 10 * Math.cos(theta));

            System.out.println(target);
            this.setStrategy(new TargetStrategy(target));
        }
        //s.printRemainingRats();

        return this.strategy.generateMove(this, s);	
    }
}
