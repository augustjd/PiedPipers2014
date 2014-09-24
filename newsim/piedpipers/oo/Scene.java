package piedpipers.oo;

import java.util.*;
import java.util.concurrent.*;

import piedpipers.oo.Vector;
import piedpipers.sim.*;

public class Scene {

    int dimension;

    final Vector[] pipers;
    final Vector[] rats;

    final boolean[] music;

    int tick;

    List<Integer> free_rats = null;

    final Vector[] rat_velocities;
    final int[] thetas;

    public Vector getPiper(int index) { return pipers[index]; }
    public double getPiperSpeed(int index) { 
        if (music[index]) {
            return Player.MUSICPIPER_SPEED;
        } else {
            return Player.PIPER_SPEED;
        }
    }
    public boolean isPlayingMusic(int index) { return music[index]; }
    public Vector getRat(int index) { return rats[index]; }

    public int getNumberOfPipers() { return pipers.length; }
    public int getNumberOfRats() { return rats.length; }
    public int getTime() { return tick; }

    public double getRatDensity() { return this.getFreeRats().size() / (dimension * dimension / 2.0); }

    public enum Side {
        LEFT,
        RIGHT,
        OUTSIDE
    }

    public List<Integer> getFreeRats() {
        if (free_rats == null) {
            free_rats = new ArrayList<Integer>();
            for (int i = 0; i < this.rats.length; i++) {
                Vector v = this.rats[i];
                if (getSide(v) == Side.RIGHT && !isRatUnderInfluenceOfAnyPiper(v)) {
                    free_rats.add(i);
                }
            }
        }

        return free_rats;
    }

    int getPiperClosestToRat(int rat) {
        double best_distance = Double.POSITIVE_INFINITY;
        Vector rat_vec = rats[rat];
        int best_piper = -1;
        for (int i = 0; i < pipers.length; i++) {
            if (pipers[i].distanceTo(rat_vec) < best_distance) {
                best_distance = pipers[i].distanceTo(rat_vec);
                best_piper = i;
            }
        }
        return best_piper;
    }

	static int getSide(double x, double y, int dimension) {
		if (x < dimension * 0.5)
			return 0;
		else if (x > dimension * 0.5)
			return 1;
		else
			return 2;
	}

    public boolean isOnField(Vector v) {
        return getSide(v) != Side.OUTSIDE;
    }
    public Side getSide(Vector v) {
        if (v.x < 0 || v.y < 0 || v.x > dimension || v.y > dimension) {
            return Side.OUTSIDE;
        } else if (v.x > dimension/2.0) {
            return Side.RIGHT;
        } else {
            return Side.LEFT;
        }
    }

    public Vector getGatePosition() {
        return new Vector(dimension/2.0, dimension/2.0 + 0.5);
    }

    public Vector getRatVelocity(int rat_index) {
        return rat_velocities[rat_index];
    }

    public boolean isRatUnderInfluenceOfPiper(Vector rat, int piper_index) {
        return music[piper_index] && rat.distanceTo(getPiper(piper_index)) < (Player.WALK_DIST * 0.90);
    }

    public boolean isRatUnderInfluenceOfAnyPiper(Vector rat) {
        for (int i = 0; i < pipers.length; i++) {
            if (isRatUnderInfluenceOfPiper(rat, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRatUnderInfluenceOfAnyPiper(int rat_index) {
        return isRatUnderInfluenceOfAnyPiper(getRat(rat_index));
    }

	private Scene(Point[] pipers, 
                 Point[] rats, 
                 boolean[] music, 
                 int[] thetas,
                 int dimension,
                 int tick) {

        this.tick = tick;
        this.dimension = dimension;
        this.thetas = thetas;
        this.pipers = new Vector[pipers.length];
        for (int i = 0; i < pipers.length; i++) {
            this.pipers[i] = new Vector(pipers[i]);
        }

        this.rats           = new Vector[rats.length];
        this.rat_velocities = new Vector[rats.length];
        for (int i = 0; i < rats.length; i++) {
            this.rats[i] = new Vector(rats[i]);
            this.rat_velocities[i] = new Vector(Player.WALK_SPEED * Math.sin(thetas[i] * Math.PI / 180),
                                                Player.WALK_SPEED * Math.cos(thetas[i] * Math.PI / 180));
        }

        this.music = new boolean[music.length];
        for (int i = 0; i < music.length; i++) {
            this.music[i] = music[i];
        }
    }


    // SCENE CACHE
    // To speed up lookaheads and stuff like that, we're going to share a
    // single Scene object among all Players.

    public Scene altLookAhead(int ticks) throws Exception {
        int destination_tick = this.tick + ticks;
        if (Scenes.containsKey(destination_tick)) {
            return Scenes.get(destination_tick);
        }

        if (Scenes.lastEntry().getKey() > destination_tick) {
            throw new Exception("Can't ask for a scene in the past!");
        }
        Scene current = Scenes.lastEntry().getValue();
        do {
            current = nextStep(current);
        } while (current.tick < destination_tick);

        return current;
    }
    static Scene nextStep(Scene current) {
        Point[] next_rats = moveRats(current.rats, current.thetas, current.dimension);
        Point[] pipers = new Point[current.pipers.length];
        for (int i = 0; i < pipers.length; i++) {
            pipers[i] = current.pipers[i].asPoint();
        }

        return new Scene(
                pipers, 
                next_rats, 
                current.music, 
                current.thetas, 
                current.dimension, 
                current.tick + 1);
    }

    public Scene lookAhead(InterceptorMath.Intercept intercept) {
        return lookAhead((int)Math.ceil(intercept.time));
    }
    public Scene lookAhead(int ticks) {
        Point[] next_rats = moveRats(this.rats, this.thetas, this.dimension, Player.WALK_SPEED * ticks);
        Point[] pipers = new Point[this.pipers.length];
        for (int i = 0; i < pipers.length; i++) {
            pipers[i] = this.pipers[i].asPoint();
        }

        return new Scene(
                pipers, 
                next_rats, 
                this.music, 
                this.thetas, 
                this.dimension, 
                this.tick + 1);
    }

	static Point[] moveRats(Vector[] rats, int[] thetas, int dimension, double speed) {
		Point[] newRats = new Point[rats.length];
		for (int i = 0; i < rats.length; ++i) {
			// compute its velocity vector
			newRats[i] = moveRat(rats[i].asPoint(), thetas[i], dimension, speed);
		}
        return newRats;
	}
	static Point[] moveRats(Vector[] rats, int[] thetas, int dimension) {
		Point[] newRats = new Point[rats.length];
		for (int i = 0; i < rats.length; ++i) {
			// compute its velocity vector
			newRats[i] = moveRat(rats[i].asPoint(), thetas[i], dimension);
		}
        return newRats;
	}

	// compute Euclidean distance between two points
	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	static Point moveRat(Point thisRat, int theta, int dimension, double speed) {
		double ox = 0, oy = 0;

        ox = speed * Math.sin(theta * Math.PI / 180);
        oy = speed * Math.cos(theta * Math.PI / 180);

		Point npos = updatePosition(thisRat, ox, oy, theta, dimension);
		return npos;
	}
	static Point moveRat(Point thisRat, int theta, int dimension) {
        return moveRat(thisRat, theta, dimension, Player.WALK_SPEED);
	}

	// update the current point according to the offsets
	static Point updatePosition(Point now, double ox, double oy, int theta, int dimension) {
		double nx = now.x + ox;
        double ny = now.y + oy;

		// hit the left fence
		if (nx < 0) {
			// System.err.println("RAT HITS THE LEFT FENCE!!!");
			// move the point to the left fence
			Point temp = new Point(0, now.y);
			// how much we have already moved in x-axis?
			double moved = 0 - now.x;
			// how much we still need to move
			// BUT in opposite direction
			double ox2 = -(ox - moved);
			//Random random = new Random();
			
			return updatePosition(temp, ox2, oy, -theta, dimension);
		}
		// hit the right fence
		if (nx > dimension) {
			// System.err.println("RAT HITS THE RIGHT FENCE!!!");
			// move the point to the right fence
			Point temp = new Point(dimension, now.y);
			double moved = (dimension - now.x);
			double ox2 = -(ox - moved);
			//Random random = new Random();
			
			return updatePosition(temp, ox2, oy, -theta, dimension);
		}
		// hit the up fence
		if (ny < 0) {
			// System.err.println("RAT HITS THE UP FENCE!!!");
			// move the point to the up fence
			Point temp = new Point(now.x, 0);
			double moved = 0 - now.y;
			double oy2 = -(oy - moved);
			//Random random = new Random();
		
			return updatePosition(temp, ox, oy2, 180 - theta, dimension);
		}
		// hit the bottom fence
		if (ny > dimension) {
			// System.err.println("RAT HITS THE BOTTOM FENCE!!!");
			Point temp = new Point(now.x, dimension);
			double moved = (dimension - now.y);
			double oy2 = -(oy - moved);
			//Random random = new Random();
			return updatePosition(temp, ox, oy2, 180 - theta, dimension);
		}

		assert nx >= 0 && nx <= dimension;
		assert ny >= 0 && ny <= dimension;
		// hit the middle fence
		if (hitTheFence(now.x, now.y, nx, ny, dimension)) {
			// System.err.println("SHEEP HITS THE CENTER FENCE!!!");
			// System.err.println(nx + " " + ny);
			// System.err.println(ox + " " + oy);
			// move the point to the fence
			Point temp = new Point(dimension/2, now.y);
			double moved = (dimension/2 - now.x);
			double ox2 = -(ox - moved);
			//Random random = new Random();
			return updatePosition(temp, ox2, oy, -theta, dimension);
		}
		// otherwise, we are good
		return new Point(nx, ny);
	}

	static boolean hitTheFence(double x1, double y1, double x2, double y2, int dimension) {
		// on the same side
		if (getSide(x1, y1, dimension) == getSide(x2, y2, dimension))
			return false;

		// one point is on the fence
		if (getSide(x1, y1, dimension) == 2 || getSide(x2, y2, dimension) == 2)
			return false;

		// compute the intersection with (50, y3)
		// (y3-y1)/(50-x1) = (y2-y1)/(x2-x1)

		double y3 = (y2 - y1) / (x2 - x1) * (dimension/2 - x1) + y1;

		assert y3 >= 0 && y3 <= dimension;

		// pass the openning?
		if (y3 >= dimension/2-1 && y3 <= dimension/2+1) 
			return false;
		else {
			return true;
		}
	}

    public static int MAX_SCENES_TO_CACHE = 100;
    public static TreeMap<Integer, Scene> Scenes = new TreeMap<Integer, Scene>();

    public static Scene getScene(int tick) {
        return Scenes.get(tick);
    }

    public static Scene getOrCreateScene(Point[] pipers, 
                                         Point[] rats, 
                                         boolean[] music, 
                                         int[] thetas,
                                         int dimension,
                                         int tick) {
        Scene result = getScene(tick);
        if (result == null) {
            result = new Scene(pipers, rats, music, thetas, dimension, tick);
            System.out.format("Turn %d RatsRemaining %d\n", tick, result.getFreeRats().size());
            addScene(result);
        }

        return result;
    }

    public static Scene addScene(Scene s) {
            Scenes.put(s.tick, s);
            pruneScenes();
            return s;
    }

    public static void pruneScenes() {
        while (Scenes.size() > MAX_SCENES_TO_CACHE) {
            Scenes.pollFirstEntry();
        }
    }

    @Override
    public String toString() {
        return String.format("<Scene for turn %d>", tick);
    }
}
