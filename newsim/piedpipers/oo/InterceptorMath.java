package piedpipers.oo;

import piedpipers.oo.Vector;
import piedpipers.oo.Scene;

import piedpipers.sim.Piedpipers;

public class InterceptorMath {
    public static class Intercept {
        public double time;
        public Vector location;

        public Intercept(double time, Vector location) {
            this.time = time;
            this.location = location;
        }

        public static Intercept fromVectorsAndTime(Vector pos, Vector vel, double time) {
            return new Intercept(time, pos.add(vel.scale(time)));
        }
    }

    public static Vector getNextBounceLocation(int rat_index, Scene s) {
        Double next_bounce_time = getNextBounceTime(rat_index, s);
        if (next_bounce_time == null) return null;

        Vector pos = s.getRat(rat_index);
        Vector vel = s.getRatVelocity(rat_index);

        return pos.add(vel.scale(Math.floor(next_bounce_time)));
    }

    public static Double getNextBounceTime(int rat_index, Scene s) {
        return getNextBounceTime(s.getRat(rat_index), s.getRatVelocity(rat_index), s.dimension);
    }
    public static Double getNextBounceTime(Vector pos, Vector vel, int dimension) {
        Double left_collision   = getTimeOfCollisionWithWall(pos.x, vel.x, dimension/2.0);
        Double right_collision  = getTimeOfCollisionWithWall(pos.x, vel.x, dimension);
        Double top_collision    = getTimeOfCollisionWithWall(pos.y, vel.y, 0);
        Double bottom_collision = getTimeOfCollisionWithWall(pos.y, vel.y, dimension);

        Double min = Math.min(bottom_collision, 
                              Math.min(top_collision, 
                                       Math.min(left_collision, right_collision)));

        if (isCollisionWithGate(pos, vel, min, dimension)) {
            return null;
        }
        return min;
    }
    public static boolean isCollisionWithGate(Vector pos, Vector vel, double time, int dimension) {
        Vector collision_location = vel.scale(time).add(pos);
        double BUFFER = 0.1;

        return collision_location.distanceTo(new Vector(dimension/2.0, dimension/2.0)) < (2.0 + BUFFER);
    }
    public static Double getTimeOfCollisionWithWall(double x, double vx, double wall_pos) {
        Double result = (wall_pos - x) / vx;

        if (result < 0) 
            return Double.POSITIVE_INFINITY;
        else 
            return result;
    }

    public static Vector[] getRatPosVelocityAfterNextCollision(Vector pos, Vector vel, int dimension) {
        Double next_bounce = getNextBounceTime(pos, vel, dimension);

        if (next_bounce == null) {
            return null;
        }

        Vector bounce_position = pos.add(vel.scale(next_bounce));

        Vector bounce_velocity = new Vector(vel.x, vel.y);
        double EPSILON = 1.0;
        if (bounce_position.x > dimension - EPSILON) {
            // bounce is against right wall.
            bounce_velocity.x = -bounce_velocity.x;
        } else if (bounce_position.x < dimension / 2.0 + EPSILON) {
            // bounce is against left wall.
            bounce_velocity.x = -bounce_velocity.x;
        } else if (bounce_position.y < EPSILON) {
            // bounce is against top wall.
            bounce_velocity.y = -bounce_velocity.y;
        } else if (bounce_position.y > dimension - EPSILON) {
            // bounce is against bottom wall.
            bounce_velocity.y = -bounce_velocity.y;
        } else {
            System.out.println("Something fishy happened in getVelocityAfterNextCollision().");
            return null;
        }

        Vector[] result = new Vector[2];
        
        result[0] = bounce_position;
        result[1] = bounce_velocity;

        return result;
    }

    public static Double calculateInterceptionTime(Player piper, int rat, Scene s) {
        Double result = null;

        result = getInterceptTimeOnInfiniteField(
                s.getRat(rat), 
                s.getRatVelocity(rat), 
                s.getPiper(piper.id), 
                piper.getSpeed());

        if (result == null) {
            return null;
        }

        return result;
    }

	private static double[] solveQuad(double a, double b, double c) {
		double[] sol = {0,0};
		if (a == 0) {
			if (b == 0) {
                sol = null;
			} else {
				sol[0] = -c/b;
				sol[1] = -c/b;
			}
		} else {
			double disc = b*b - 4*a*c;
			if (disc >= 0) {
				disc = Math.sqrt(disc);

				sol[0] = (-b-disc)/(2*a);
				sol[1] = (-b+disc)/(2*a);
			} else {
                sol = null;
            }
		}
		return sol;
	}

    public static Intercept getSoonestIntercept(final int rat, final int piper, final double piper_speed, Scene s) {
        Double total_time = 0.0;
        Intercept intercept = null;

        Vector rat_position = s.getRat(rat);
        Vector rat_velocity = s.getRatVelocity(rat);

        Vector piper_position = s.getRat(piper);

        int MAX_REFLECTION_TRIES = 5;
        for (int i = 0; i < MAX_REFLECTION_TRIES; i++) {
            Double intercept_time = getInterceptTimeOnInfiniteField(
                    rat_position, 
                    rat_velocity, 
                    piper_position, 
                    piper_speed);

            if (intercept_time != null) {
                //System.out.format("Intercept time: %f\n", intercept_time);
                intercept = Intercept.fromVectorsAndTime(rat_position, rat_velocity, intercept_time + total_time);
                if (s.getSide(intercept.location) != Scene.Side.RIGHT) {
                    intercept = null;
                }
            }

            if (intercept == null) { 
                // intercept is still null...
                Double next_bounce_time = null;
                Vector[] after_bounce_stuff = null;
                try {
                    next_bounce_time = getNextBounceTime(rat_position, rat_velocity, s.dimension);
                    after_bounce_stuff = getRatPosVelocityAfterNextCollision(rat_position, rat_velocity, s.dimension);
                    if (next_bounce_time == 0.0) return null;
                    else if (next_bounce_time == null) return null; // it's going through gate
                    else {
                        rat_position = after_bounce_stuff[0];
                        rat_velocity = after_bounce_stuff[1];

                        total_time += next_bounce_time;
                    }
                } catch (NullPointerException e) {
                    return null;
                }
                //System.out.format("Still no intercept! Pos/Vel after bounce in %f seconds: %s / %s\n", next_bounce_time, rat_position, rat_velocity);
            } else {
                break;
            }
        }

        return intercept;
    }

    public static Double getInterceptTimeOnInfiniteField(final Vector r,   // rat position
                                                         final Vector v,   // rat velocity
                                                         final Vector p,   // piper position
                                                         final double s) { // piper speed
        Vector o = r.sub(p);

        final double a = v.dot(v) - s*s;
        final double b = 2 * v.dot(o);
        final double c = o.dot(o);

        double[] solutions = solveQuad(a,b,c);
        if (solutions == null) {
            return null;
        } else {
            double soln = Math.max(solutions[0], solutions[1]);
            if (soln < 0.0) {
                return Math.max(solutions[0], solutions[1]);
            } else {
                return soln;
            }
        }
    }
    public static Double getMinPositiveQuadraticSolution(final double a, final double b, final double c) {
        if (a == 0.0 || c == 0.0) {
            if (c > 0.0 || b == 0.0) {
                return null;
            }
            return -c / b;
        }

        final double discriminant = (b * b) - (4.0 * a * c);
        if (discriminant < 0.0) {
            // System.out.println("Discriminant is less than 0."); 
            return null;
        } else {
            double result = (-b - Math.sqrt(discriminant))/(2*a);
            if (result < 0.0) {
                result = (-b + Math.sqrt(discriminant))/(2*a);
            }

            if (Double.isNaN(result)) {
                //System.out.format("Result is NaN. Discriminant: %f\n", discriminant); 
                return null;
            }

            if (result < 0.0) {
                //System.out.println("Both solutions to the quadratic are negative."); 
                return null;
            } else {
                return result;
            }
        }
    }

    public static Double calculateInterceptionTime(final Vector a, final Vector v, final Vector b, final double s) {
        final double ox = a.x - b.x;
        final double oy = a.y - b.y;
 
        final double h1 = v.x * v.x + v.y * v.y - s * s;
        final double h2 = ox * v.x + oy * v.y;
        double t;
        if (h1 == 0) { // problem collapses into a simple linear equation 
            t = -(ox * ox + oy * oy) / 2*h2;
        } else { // solve the quadratic equation
            final double minusPHalf = -h2 / h1;
 
            final double discriminant = minusPHalf * minusPHalf - (ox * ox + oy * oy) / h1; // term in brackets is h3
            if (discriminant < 0) { // no (real) solution then...
                return null;
            }
 
            final double root = Math.sqrt(discriminant);
 
            final double t1 = minusPHalf + root;
            final double t2 = minusPHalf - root;
 
            final double tMin = Math.min(t1, t2);
            final double tMax = Math.max(t1, t2);
 
            t = tMin > 0 ? tMin : tMax; // get the smaller of the two times, unless it's negative
            if (t < 0) { // we don't want a solution in the past
                return null;
            }
        }
 
        // calculate the point of interception using the found intercept time and return it
        return t;
    }
}
