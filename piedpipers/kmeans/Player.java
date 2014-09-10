package piedpipers.kmeans;

import java.util.*;
import java.lang.Double;

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



	// Return: the next position
	// my position: dogs[id-1]
	//
	public Point getGatePosition() {
		return new Point(dimension/2 + 3, dimension/2);
	}

	boolean seeking_center = true;

	boolean seeking_gate = true;
	boolean returning_to_gate = false;
	boolean dropping_off = false;
	public Point move(Point[] pipers, // positions of dogs
			Point[] rats) {
		Point pos = pipers[id];
        System.out.println("Moving...");

		if (target == null && seeking_center) {
			target = getGatePosition();
		} 
        List<Point> rats_left_to_catch = new ArrayList<Point>();

        if (npipers > 1) {
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
        } else {
            if (same_point(pos, target) && seeking_gate) {
                seeking_gate = false;
                for (Point p : rats) {
                    if (p.x > dimension / 2.0) {
                        rats_left_to_catch.add(p);
                    }
                }
                kmeans(npipers + 6, rats_left_to_catch);

                Cluster best = null;
                for (Cluster c : clusters) {
                    if (best == null || c.points.size() > best.points.size()) {
                        best = c;
                    }
                }

                target = best.centroid;
            } else if (same_point(pos, target) && returning_to_gate) {
                returning_to_gate = false;
                dropping_off = true;
                target = getGatePosition();
                target.x -= 20;
            } else if (same_point(pos, target) && dropping_off) {
                this.music = false;
                this.dropping_off = false;
                seeking_gate = true;
                target = getGatePosition();
            } else if (same_point(pos, target)) {
                this.music = true;
                target = getGatePosition();
                returning_to_gate = true;
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


    List<Cluster> clusters = new ArrayList<Cluster>();

    public class Cluster {
        public Point centroid;
        public List<Point> points;

        public double total_distance_centroid_moved = 0.0;

        public Cluster(Point centroid, List<Point> points) {
            this.centroid = centroid;
            this.points = points;
        }

        public double redefineCentroid() {
            Point oldcentroid = new Point(centroid.x, centroid.y);
            this.centroid = centroid(this.points);

            total_distance_centroid_moved = distance(oldcentroid, this.centroid);

            return total_distance_centroid_moved;
        }
    }

    // TODO: ignore rats on other side of gate
    void kmeans(int k, List<Point> rats) {
        clusters.clear();

        for (int i = 0; i < k; i++) {
            clusters.add(new Cluster(rats.get(i), new ArrayList<Point>()));
        }

        double MADE_UP_THRESHOLD = 1.0;
        while (true) {
            for (int j = 0; j < rats.size(); j++) {
                int best_centroid = getBestCentroid(rats.get(j), clusters);
                clusters.get(best_centroid).points.add(rats.get(j));
            }

            double total_centroid_motion = 0;
            for (Cluster c : clusters) {
                total_centroid_motion += c.redefineCentroid();
            }
            if ((int)total_centroid_motion == 0) {
                System.out.println("Exiting with threshold " + total_centroid_motion);
                break;
            }
            System.out.println("" + total_centroid_motion);
        }
    }

    int getBestCentroid(Point p, List<Cluster> clusters) {
        double d = Double.POSITIVE_INFINITY;
        int best = -1;
        for (int i = 0; i < clusters.size(); i++) {
            Cluster c = clusters.get(i);
            if (distance(c.centroid, p) < d) {
                d = distance(c.centroid, p);
                best = i;
            }
        }
        return best;
    }

    Point centroid(List<Point> points) {
        Point total = new Point(0,0);

        for (Point p : points) {
            total.x += p.x;
            total.y += p.y;
        }

        total.x /= points.size();
        total.y /= points.size();

        return total;
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
