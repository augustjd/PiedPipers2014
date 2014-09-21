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
    }

    public static Vector getInterceptLocation(Vector r, Vector rv, Vector p, double p_speed) {
        Double intercept_time = getInterceptTime(r,rv,p,p_speed);

        if (intercept_time == null) return null;

        return getInterceptCourse(r,rv,p,p_speed).scale(-intercept_time);
    }
    public static Vector getInterceptCourse(Vector r, Vector rv, Vector p, double p_speed) {
        return r.sub(p).add(rv.scale(getInterceptTime(r,rv,p,p_speed))).unit();
    }
    public static Double getInterceptTime(Player piper, int rat, Scene s) {
        return getInterceptTime(s.getRat(rat), s.getRatVelocity(rat), s.getPiper(piper.id), piper.getSpeed());
    }
    public static Double getInterceptTime(Vector r, Vector rv, Vector p, double p_speed) {
        Vector offset = p.sub(r);

        double a = rv.dot(rv) - p_speed*p_speed;
        double b = 2*offset.dot(rv);
        double c = offset.dot(offset);

        return getQuadraticSolutions(a,b,c);
    }
    public static Double getQuadraticSolutions(double a, double b, double c) {
        double criterion = b*b-4*a*c;
        if (criterion < 0.0) return null;
        return (-b + Math.sqrt(criterion)) / (2*a);
    }

    public static Vector calculateInterceptionPosition(final Vector a, final Vector v, final Vector b, final double s) {
       Double interceptionTime = calculateInterceptionTime(a,v,b,s);

       if (interceptionTime == null) {
           return null;
       } else {
           return a.add(v.scale(interceptionTime));
       }
    }

    public static Vector getNextBounceLocation(int rat_index, Scene s) {
        Double next_bounce_time = getNextBounceTime(rat_index, s);
        if (next_bounce_time == null) return null;

        Vector pos = s.getRat(rat_index);
        Vector vel = s.getRatVelocity(rat_index);

        return pos.add_ip(vel.scale_ip(Math.floor(next_bounce_time)));
    }

    public static Double getNextBounceTime(int rat_index, Scene s) {
        Vector pos = s.getRat(rat_index);
        Vector vel = s.getRatVelocity(rat_index);

        Double left_collision   = getTimeOfCollisionWithWall(pos.x, vel.x, s.dimension/2);
        Double right_collision  = getTimeOfCollisionWithWall(pos.x, vel.x, s.dimension);
        Double top_collision    = getTimeOfCollisionWithWall(pos.y, vel.y, 0);
        Double bottom_collision = getTimeOfCollisionWithWall(pos.y, vel.y, s.dimension);

        Double min = Math.min(bottom_collision, 
                              Math.min(top_collision, 
                                       Math.min(left_collision, right_collision)));

        if (isCollisionWithGate(pos, vel, min, s)) {
            return null;
        }
        return min;
    }
    public static boolean isCollisionWithGate(Vector pos, Vector vel, double time, Scene s) {
        Vector collision_location = vel.scale(time).add_ip(pos);
        double BUFFER = 0.1;

        return collision_location.distanceTo(s.getGatePosition()) < (2.0 + BUFFER);
    }
    public static Double getTimeOfCollisionWithWall(double x, double vx, double wall_pos) {
        Double result = (wall_pos - x) / vx;

        if (result < 0) 
            return Double.POSITIVE_INFINITY;
        else 
            return result;
    }

    public static Double calculateInterceptionTime(Player piper, int rat, Scene s) {
        Double result = null;
        Double additional = 0.0;
        Scene current = s;
        do {
            result = calculateInterceptionTime(
                    current.getRat(rat), 
                    current.getRatVelocity(rat), 
                    current.getPiper(piper.id), 
                    piper.getSpeed());

            if (result == null) {
                Double bounce = getNextBounceTime(rat, current);
                if (bounce == null) { // rat hit gate
                    return null;
                }
                int ticks_until_bounce = (int)Math.ceil(bounce);
                System.out.format("Looked ahead to %d. Next bounce of rat %d in %d ticks. Position of rat: %s\n", current.getTime(), rat, ticks_until_bounce, current.getRat(rat));

                additional += ticks_until_bounce;
            }
        } while (false);
        if (result == null) return null;
        return result + additional;
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
 
            t = tMin;// > 0 ? tMin : tMax; // get the smaller of the two times, unless it's negative
            if (t < 0) { // we don't want a solution in the past
                return null;
            }
        }
 
        // calculate the point of interception using the found intercept time and return it
        return t;
    }
}
