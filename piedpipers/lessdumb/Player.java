package piedpipers.lessdumb;

import java.util.*;

import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;

	static double radius_of_influence = 10.0;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	
	static Point target;
	static int[] thetas;
	static boolean finishround = true;
	static boolean initi = false;

	public void init() {
		thetas = new int[npipers];
	}

	static Point sum(Point a, Point b) {
		return new Point(b.x + a.x, b.y + a.y);
	}
	static Point difference(Point a, Point b) {
		return new Point(b.x - a.x, b.y - a.y);
	}

	static Point scale(Point a, double r) {
		return new Point(a.x * r, a.y * r);
	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	static double length(Point a) {
		return Math.sqrt(a.x*a.x + a.y*a.y);
	}

	static Point unit(Point p) {
		double len = length(p);
		return scale(p, 1.0/len);
	}

	static double EPSILON = 1;
	static boolean same_point(Point a, Point b) {
		return distance(a,b) < EPSILON;
	}


	boolean seeking_center = true;

	// Return: the next position
	// my position: dogs[id-1]
	//
	public Point getGatePosition() {
		return new Point(dimension/2, dimension/2);
	}

	public Point move(Point[] pipers, // positions of dogs
			Point[] rats) {
		Point pos = pipers[id];

		if (target == null && seeking_center) {
			target = getGatePosition();
		} 

		if (seeking_center && same_point(pos, target)) {
			seeking_center = false;
		}

		if (pipers.length > 1 && !seeking_center) {
			if (id == 0) {
				this.music = true;
				target = getSweeperTarget(pipers, rats, pos);
			} else {
				target = getUsualTarget(pipers, pos);
			}
		}

		return moveToTarget(pos, target);
	}

	public boolean isClosestToPoint(Point[] pipers, Point test) {
		double mydistance = distance(pipers[id], test);
		for (int i = 0; i < pipers.length; i++) {
			if (i == id) continue;
			if (distance(pipers[i], test) < mydistance) {
				return false;
			}
		}
		return true;
	}

	public int getInfluencedRatsCount(Point[] pipers, Point[] rats, Point pos) {
		int count = 0;
		for (Point p : rats) {
			if (distance(p, pos) < radius_of_influence && isClosestToPoint(pipers, p)) {
				count++;
			}
		}
		return count;
	}
	public int getCorraledRatsCount(Point[] rats) {
		int count = 0;
		for (Point p : rats) {
			if (p.x < dimension/2) {
				count++;
			}
		}
		return count;
	}

	public boolean sweeperNearby(Point[] pipers, Point pos) {
		return distance(pipers[0], pos) < radius_of_influence;
	}

	boolean moving_out = true;
	public Point getUsualTarget(Point[] pipers, Point pos) {
		int perimeter = 2 * dimension;
		int my_portion = id * perimeter / pipers.length;
		Point gate = getGatePosition();

		if (moving_out && same_point(pos, target)) {
			moving_out = false;
			this.music = true;
		} else if (!moving_out && same_point(pos, target)) {
			if (sweeperNearby(pipers, pos)) {
				moving_out = true;
				this.music = false;
			}
		}

		if (!moving_out) {
			gate.x += dimension/4;
			return gate;
		}


		if (my_portion < dimension/2) {
			// top wall
			return new Point(dimension/2 + my_portion, 0);
		} else if (my_portion > dimension/2 && my_portion < dimension * 1.5) {
			// right wall
			return new Point(dimension, my_portion - dimension/2);
		} else {
			// bottom wall
			return new Point(dimension - (my_portion - dimension * 1.5), dimension);
		}
	}


	boolean moving_right = true;

	boolean final_move = false;
	public Point getSweeperTarget(Point[] pipers, Point[] rats, Point pos) {
		Point gate = getGatePosition();
		Point right_wall = getGatePosition();
		right_wall.x = dimension;

		if (getInfluencedRatsCount(pipers, rats, pos) + getCorraledRatsCount(rats) == rats.length) {
			final_move = true;
		}

		if (final_move) {
			gate.x -= 20;
			return gate;
		}

		if (moving_right && same_point(pos, right_wall)) {
			moving_right = false;
		}
		else if (!moving_right && same_point(pos, gate)) {
			moving_right = true;
		}

		if (moving_right) {
			return right_wall;
		} else {
			return gate;
		}
	}

	public Point moveToTarget(Point current, Point target) {
		Point direction = unit(difference(current, target));

		double speed = 0.0;
		if (this.music) {
			speed = mpspeed;
		} else {
			speed = pspeed;
		}
		Point scaled = scale(direction, speed);
		return Player.sum(current, scaled);
	}

	boolean closetoWall (Point current) {
		boolean wall = false;
		if (Math.abs(current.x-dimension)<pspeed) {
			wall = true;
		}
		if (Math.abs(current.y-dimension)<pspeed) {
			wall = true;
		}
		if (Math.abs(current.y)<pspeed) {
			wall = true;
		}
		return wall;
	}

	int getSide(double x, double y) {
		if (x < dimension * 0.5)
			return 0;
		else if (x > dimension * 0.5)
			return 1;
		else
			return 2;
	}

	int getSide(Point p) {
		return getSide(p.x, p.y);
	}

}
