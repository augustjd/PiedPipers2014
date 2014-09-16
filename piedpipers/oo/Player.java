package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.Point;
import java.awt.Color;

public class Player extends piedpipers.sim.Player {

    public static final double EPSILON = 0.01;
    public static class Vector {
        public double x;
        public double y;

        public Vector() {
            this(0,0);
        }
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
            Double result = Math.sqrt((this.x - b.x) * (this.x - b.x) + 
                                      (this.y - b.y) * (this.y - b.y));
            if (result == null) {
                return Double.POSITIVE_INFINITY;
            }
            else return result;
        }


        public double angleWithOrigin() {
            return Math.atan2(this.y, this.x);
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
        public boolean choose_next = true;

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
                return getSideOfGate(s, player_side);
            }
        }

        @Override
        public Point generateMove(Player p, Scene s) {
            Vector position = s.getPlayerPosition(p);
            if (this.reachedTarget(p,s) && choose_next) {
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

            Vector difference;
            if (this.intermediate_target == null) {
                this.intermediate_target = this.target;
            }
            try {
                difference = this.intermediate_target.sub(position);
            } catch (NullPointerException e) {
                difference = new Vector(0,0);
            }

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
            assert(p != null);
            assert(target != null);
            assert(s != null);
            assert(s.getPlayerPosition(p) != null);
            assert(this != null);

            Vector player_position = s.getPlayerPosition(p);
            boolean result;
            try {
                result = target.distanceTo(player_position) < EPSILON;
            } catch (NullPointerException e) {
                return true;
            }
            return result;
        }

        public String getName() { return "TargetStrategy"; }
    }

    public static class FartherFromGateComparator implements Comparator<Integer> {
        Scene s;
        public FartherFromGateComparator(Scene s) {
            this.s = s;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            Vector gate = s.getGatePosition();
            return - new Double(s.rat_vectors.get(i1).distanceTo(gate)).compareTo(s.rat_vectors.get(i2).distanceTo(gate));
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

            if (s.rats.size() > 0) {
                closest_index = s.rats.get((p.id * 5) % s.rats.size());
            }
            assert(s.getSide(s.absolute_rats[closest_index]) == Scene.Side.RIGHT);
            System.out.format("%d: %s\n", closest_index,  s.rat_vectors.get(closest_index));
            return closest_index; 
        }


        public static final int RATS_UNTIL_DROPOFF = 8;
        public static final int MAX_TURNS_UNTIL_DROPOFF = 200;
        public int turns_until_dropoff = MAX_TURNS_UNTIL_DROPOFF;
        @Override
        public Point generateMove(Player p, Scene s) {
            if (p.music) {
                turns_until_dropoff--;
            }

            if (turns_until_dropoff == 0) {
                p.setStrategy(new ReturnToGateStrategy(s));
            }

            if (nearTargetRat(p, s)) {
                p.turnOnMusic();

                int rats_influenced = s.getRatsUnderInfluenceOfPiper(s.getPlayerPosition(p).asPoint());
                if (rats_influenced >= RATS_UNTIL_DROPOFF ||
                    s.getRemainingRats() == 0) {
                    p.setStrategy(new ReturnToGateStrategy(s));
                } else {
                    this.rat_index = chooseRat(p, s);
                    if (this.rat_index > 0) {
                        //p.setRatColor(this.rat_index, Color.RED);
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

    public static class PhaseStrategy extends Strategy {
        public static class Phase implements Comparable<Phase>{
            public double min_density;
            public Strategy s;
            public Phase(double min_density, Strategy s) {
                this.min_density = min_density;
                this.s = s;
            }

            @Override
            public int compareTo(Phase two) {
                return new Double(this.min_density).compareTo(two.min_density);
            }
        }
        List<Phase> phases;
        public PhaseStrategy(List<Phase> phases) {
            Collections.sort(phases);
            this.phases = phases;
        }
        public String getName() { return "PhaseStrategy"; }
        public Point generateMove(Player p, Scene s) {
            double density = s.getRatsDensity();

            Phase current = null;
            for (int i = this.phases.size() - 1; i >= 0; i--) {
                current = phases.get(i);
                if (current.min_density < density) {
                    break;
                }
            }

            return current.s.generateMove(p, s);
        }
    }

    public static class RadialGreedyStrategy extends TargetStrategy {
        int id;
        int dimension;

        double rads_slice;
        public RadialGreedyStrategy(Player p, Scene s) {
            super(new Vector());

            id = p.id;
            dimension = s.dimension;

            rads_slice = Math.PI / s.pipers.size();
        }

        public List<Vector> ratsInSlice(Player p, Scene s) {
            double start_angle = rads_slice * p.id;
            double end_angle   = rads_slice * (p.id + 1);


            List<Vector> rats_in_slice = new ArrayList<Vector>();
            for (Integer i : s.rats) {
                Vector v = s.rat_vectors.get(i);
                double angle = angleOfPoint(v);
                if (angle > start_angle && angle <= end_angle &&
                        v.distanceTo(s.magnetPosition()) > 1.5 * Scene.RANGE_OF_INFLUENCE) {
                    rats_in_slice.add(v);
                    //p.setRatColor(i, new Color(70 * p.id % 255,30*p.id % 255, 0));
                }
            }
            return rats_in_slice;
        }

        boolean moving_towards_gate = false;
        boolean moving_back = false;
        @Override
        public Point generateMove(Player p, Scene s) {
            Vector me = s.pipers.get(p.id);
            boolean reached_target = super.reachedTarget(p, s);
            this.choose_next = false;

            List<Vector> rats_in_slice = ratsInSlice(p, s);
            int rats_influenced = 0;
            for (Vector v : rats_in_slice) {
                if (me.distanceTo(v) < Scene.RANGE_OF_INFLUENCE) {
                    rats_influenced++;
                }
            }

            if (rats_in_slice.size() == rats_influenced) {
                this.moving_towards_gate = true;
                System.out.format("Player %d is done!\n", p.id);
            }

            if (s.getSide(me) == Scene.Side.RIGHT) {
                p.music = true;
            }

            if (moving_towards_gate) {
                this.target = s.getGatePosition();
                this.target.x -= 10;
            } else {
                Vector closest_rat = closestRat(s.pipers.get(p.id), rats_in_slice);
                this.target = closest_rat;
            }

            return super.generateMove(p, s);
        }

        public Vector closestRat(Vector p, List<Vector> rats) {
            double closest = Double.POSITIVE_INFINITY;
            Vector closest_vec = null;
            for (Vector v : rats) {
                double dist = v.distanceTo(p);
                if (dist < closest && dist > Scene.RANGE_OF_INFLUENCE) {
                    closest = dist;
                    closest_vec = v;
                }
            }

            return closest_vec;
        }

        public double angleOfPoint(Vector p) {
            return p.sub(dimension/2.0, dimension/2.0).angleWithOrigin() + Math.PI / 2.0;
        }

        public Vector centerOfSlice(int id, Scene s) {
            double angle = (rads_slice * (id - 0.5)) - Math.PI / 2.0;
            double radius = s.dimension/4.0;
            return s.getGatePosition().add(Math.cos(angle) * radius, Math.sin(angle) * radius);
        }
    }

    public static class MagnetStrategy extends TargetStrategy {
        boolean moving_left = true;
        public MagnetStrategy(Player p, Scene s) {
            super(new Vector(s.dimension - Scene.RANGE_OF_INFLUENCE, s.dimension/2));
            this.choose_next = false;
        }

        private Vector getClosestPiper(Vector piper, Scene s) {
            double closest_dist = Double.POSITIVE_INFINITY;
            Vector closest = s.getGatePosition();

            for (int i = 1; i < s.pipers.size(); i++) {
                Vector other = s.pipers.get(i);
                if (other.distanceTo(piper) < closest_dist) {
                    closest_dist = other.distanceTo(piper);
                    closest = other;
                }
            }
            return closest;
        }
        private static final double CLOSE_DISTANCE = 50;

        private boolean pipersNearby(Vector piper, Scene s) {
            return getClosestPiper(piper, s).distanceTo(piper) < CLOSE_DISTANCE;
        }


        @Override
        public Point generateMove(Player p, Scene s) {
            boolean reached_target = super.reachedTarget(p, s);
            Vector me = s.pipers.get(0);
            if (s.getSide(me) == Scene.Side.RIGHT) {
                p.music = true;
            }
            if (reached_target) {
                moving_left = !moving_left;
            }
            if (s.getSide(me) == Scene.Side.RIGHT) {
                this.target = getClosestPiper(me, s);
                this.target.y = s.dimension / 2.0;
            } else {
                this.target = s.getGatePosition();
                this.target.x += 10;
            }
            /*

            if (moving_left) {
                this.target = s.getGatePosition();
                this.target.y = s.dimension/2.0;
            } else {
                this.target = s.getGatePosition();
                this.target.x = s.dimension - Scene.RANGE_OF_INFLUENCE;
            }
            if (s.getSide(me) == Scene.Side.RIGHT && pipersNearby(me, s)) {
                this.target = getClosestPiper(me, s);
                this.target.y = s.dimension/2;
            } else {
                if (reached_target) {
                    moving_left = !moving_left;
                }

                if (moving_left) {
                    this.target = s.getGatePosition();
                    this.target.x = s.dimension - Scene.RANGE_OF_INFLUENCE;
                } else {
                    this.target = s.getGatePosition();
                    System.out.println("Magnet is moving left!");
                }
            }
            */

            return super.generateMove(p, s);
        }
    }

    public static class VerticalSweepStrategy extends TargetStrategy {
        int id;
        int dimension;
        public VerticalSweepStrategy(Player p, Scene s) {
            super(new Vector());

            this.id = p.id;
            this.dimension = s.dimension;
            this.target = generateStartTarget();
        }

        public double radius_factor = 2.5;

        boolean moving_to_start = true;
        boolean moving_to_center = false;
        Vector generateMiddleTarget() {
            Vector start_target = new Vector();

            start_target.y = dimension/2;
            start_target.x = dimension/2 + 10 + radius_factor * Scene.RANGE_OF_INFLUENCE * id;

            return start_target;
        }
        Vector generateStartTarget() {
            Vector start_target = new Vector();


            start_target.y = Scene.RANGE_OF_INFLUENCE;
            /*
            if (id % 2 == 0) {
                start_target.y = Scene.RANGE_OF_INFLUENCE;
            } else {
                start_target.y = dimension - Scene.RANGE_OF_INFLUENCE;
            }
            */

            start_target.x = dimension/2 + 10 + radius_factor * Scene.RANGE_OF_INFLUENCE * id;
            return start_target;
        }

        @Override
        public Point generateMove(Player p, Scene s) {
            boolean reached_target = super.reachedTarget(p, s);
            if (reached_target) {
                if (moving_to_start) {
                    p.music = true;

                    moving_to_start = false;
                    moving_to_center = true;

                    this.target = generateMiddleTarget();
                } else if (moving_to_center) {
                    moving_to_center = false;
                    this.target = s.getGatePosition().sub_ip(10,10);
                } else {
                    p.music = false;
                    moving_to_start = true;
                    this.target = generateStartTarget();
                }
            }

            return super.generateMove(p, s);
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
            this.printRemainingRats();
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


    static Scene s = null;
    static int pipers_moved = 0;
	public Point move(Point[] pipers, Point[] rats) {
        s = new Scene(dimension, pipers, rats);

        if (strategy == null) {
            double theta = id * Math.PI / pipers.length;

            Vector target = new Vector(dimension/2 + 10 * Math.sin(theta),
                                       dimension/2 + 10 * Math.cos(theta));

            this.setStrategy(new PhaseStrategy(Arrays.asList(
                            new PhaseStrategy.Phase(0.0, new RatTargetStrategy(this, s)),
                            new PhaseStrategy.Phase(0.00015, new RadialGreedyStrategy(this, s)),
                            new PhaseStrategy.Phase(0.0026, new VerticalSweepStrategy(this, s)))));
        }
        //s.printRemainingRats();

        return this.strategy.generateMove(this, s);	
    }
}
