package piedpipers.interceptor;
import piedpipers.interceptor.Vector;

import piedpipers.sim.Piedpipers;

public class InterceptorMath {
    public static Vector getInterceptLocation(Vector r, Vector rv, Vector p, double p_speed) {
        Double intercept_time = getInterceptTime(r,rv,p,p_speed);

        if (intercept_time == null) return null;

        return getInterceptCourse(r,rv,p,p_speed).scale(-intercept_time);
    }
    public static Vector getInterceptCourse(Vector r, Vector rv, Vector p, double p_speed) {
        return r.sub(p).add(rv.scale(getInterceptTime(r,rv,p,p_speed))).unit();
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
