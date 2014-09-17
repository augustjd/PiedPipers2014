package piedpipers.interceptor;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;
import piedpipers.interceptor.Vector;

public class Player extends piedpipers.sim.Player {

    public static final double EPSILON = 0.01;

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
                //System.out.println("Reached target.");
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

    public static class CrossGateStrategy extends TargetStrategy {
        public CrossGateStrategy(Player p, Scene s) {
            super(s.getGatePosition().add_ip(10,0));
        }
    }

    public static class ClosestToMeComparator implements Comparator<Integer> {
        Player p;
        Scene s;
        public ClosestToMeComparator(Player p, Scene s) {
            this.p = p;
            this.s = s;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            Vector r1 = s.rat_vectors.get(i1);
            Vector r2 = s.rat_vectors.get(i2);

            Vector rv1 = s.rat_velocities[i1];
            Vector rv2 = s.rat_velocities[i2];

            Vector player = s.getPlayerPosition(p);

            Double score1 = r1.distanceTo(player);
            if (rv1.dot(r1.sub(player)) > 0.0) {
                score1 = Double.POSITIVE_INFINITY;
            }
            Double score2 = r2.distanceTo(player);
            if (rv2.dot(r2.sub(player)) > 0.0) {
                score2 = Double.POSITIVE_INFINITY;
            }

            return score1.compareTo(score2);

            /*
            Double time1 = InterceptorMath.calculateInterceptionTime(r1, rv1, player, Piedpipers.MUSICPIPER_SPEED);
            Vector int1 = InterceptorMath.calculateInterceptionPosition(r1, rv1, player, Piedpipers.MUSICPIPER_SPEED);
            if (time1 == null || !s.isValidIntercept(int1)) {
                time1 = Double.POSITIVE_INFINITY;
            }

            Double time2 = InterceptorMath.calculateInterceptionTime(r2, rv2, player, Piedpipers.MUSICPIPER_SPEED);
            Vector int2 = InterceptorMath.calculateInterceptionPosition(r2, rv2, player, Piedpipers.MUSICPIPER_SPEED);
            if (time2 == null || !s.isValidIntercept(int2)) {
                time2 = Double.POSITIVE_INFINITY;
            }

            return time1.compareTo(time2);
            */
        }
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

        public RatTargetStrategy(Player p, Scene s, boolean should_try_others) {
            super(s.getGatePosition());
            this.rat_index = this.chooseRat(p, s);

            this.try_others = should_try_others;
        }
        public RatTargetStrategy(Player p, Scene s) {
            this(p, s, true);
        }

        public boolean targetMovingAway(Player p, Scene s) {
            Vector r = getTargetRatPosition(s);
            Vector v = s.rat_velocities[this.rat_index];

            Vector piper = s.getPlayerPosition(p);

            double value = r.sub(piper).dot(v);
            //System.out.format("Player %d: %f %s -> %s\n", p.id, value, r.sub(piper).toString(), v.toString());
            return r.sub(piper).dot(v) > 0.0;
        }

        public int chooseRat(Player p, Scene s) {
            double furthest = 0;
            int closest_index = 0;

            Vector position = s.getPlayerPosition(p);

            Collections.sort(s.rats, new ClosestToMeComparator(p, s));

            if (s.rats.size() > 0) {
                closest_index = s.rats.get(p.id % s.rats.size());
            }
            //System.out.format("%d: %s\n", closest_index,  s.rat_vectors.get(closest_index));
            return closest_index; 
        }


        public static final int RATS_UNTIL_DROPOFF = 8;
        public static final int TURNS_UNTIL_RECHOOSE = 50;
        public int turns_until_rechoose = TURNS_UNTIL_RECHOOSE;
        public boolean try_others;

        boolean targeted = false;
        @Override
        public Point generateMove(Player p, Scene s) {
            if (s.getRemainingRats() == 0) {
                p.setStrategy(new ReturnToGateStrategy(s));
            }

            turns_until_rechoose--;
            if (turns_until_rechoose == 0 || targetMovingAway(p, s)) {
                turns_until_rechoose = TURNS_UNTIL_RECHOOSE;
                this.rat_index = chooseRat(p, s);
            }

            p.music = true;

            Vector target_rat = getTargetRatPosition(s);
            Vector piper = s.getPlayerPosition(p);
            double speed;

            if (p.music) {
                speed = Piedpipers.MUSICPIPER_SPEED;
            } else {
                speed = Piedpipers.PIPER_SPEED;
            }
            Vector rat_velocity = s.rat_velocities[this.rat_index];

            Vector interceptLocation = InterceptorMath.calculateInterceptionPosition(target_rat, rat_velocity, piper, speed/1.1);
            Double intercept_time    = InterceptorMath.calculateInterceptionTime(target_rat, rat_velocity, piper, speed/1.1);

            if (intercept_time  == null || intercept_time < 0.0 || 
                !s.onField(interceptLocation) || s.getSide(interceptLocation) == Scene.Side.LEFT || p.id >= 1) {
                this.target = target_rat;
                //System.out.println("No interception found.");
            } else {
                /*System.out.format("Player %d will intercept rat %s at %s in %f ticks\n", 
                                  p.id, 
                                  target_rat.toString(), 
                                  interceptLocation.toString(), 
                                  intercept_time);
                                  */
                this.target = interceptLocation;
            }
            p.addDot(this.target.asPoint(), Color.GREEN, true);
            p.addDot(s.absolute_rats[rat_index], Color.YELLOW, false);

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
                //System.out.format("Player %d is done!\n", p.id);
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
        System.out.format("New strategy for player %d: %s\n", this.id, newStrategy.getName());
        this.strategy = newStrategy;
    }

    public Vector getPosition(List<Vector> pipers) {
        return pipers.get(id);
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
        this.setStrategy(new RatTargetStrategy(this, s, false));
    }


    static Scene s = null;
    static int pipers_moved = 0;
	public Point move(Point[] pipers, Point[] rats, boolean[] pipermusic, int[] thetas) {
        s = new Scene(dimension, pipers, rats, thetas);

        if (strategy == null) {
            double theta = id * Math.PI / pipers.length;

            Vector target = new Vector(dimension/2 + 10 * Math.sin(theta),
                                       dimension/2 + 10 * Math.cos(theta));

            /*
            this.setStrategy(new PhaseStrategy(Arrays.asList(
                            new PhaseStrategy.Phase(0.0, new RatTargetStrategy(this, s)),
                            new PhaseStrategy.Phase(0.00015, new RadialGreedyStrategy(this, s)),
                            new PhaseStrategy.Phase(0.0026, new VerticalSweepStrategy(this, s)))));
                            */
            this.setStrategy(new CrossGateStrategy(this, s));
        }
        //s.printRemainingRats();

        return this.strategy.generateMove(this, s);	
    }
}
