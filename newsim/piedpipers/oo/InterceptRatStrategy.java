package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class InterceptRatStrategy extends TargetStrategy {
    List<InterceptorMath.Intercept> intercepts = null;

    public List<InterceptorMath.Intercept> getInterceptTimes(Player p, Scene s) {
        List<InterceptorMath.Intercept> result = new ArrayList<InterceptorMath.Intercept>();
        for (Integer i : s.getFreeRats()) {
            Double time = InterceptorMath.calculateInterceptionTime(p, i, s);
            if (time != null) {
                Vector location = s.getRat(i).add(s.getRatVelocity(i).scale(time));

                if (s.isOnField(location)) {
                    result.add(new InterceptorMath.Intercept(time, location));
                }
            }
        }
        return result;
    }
    @Override
    public Vector getMove(Player p, Scene s) {
        /*
        if (intercepts == null) {
            intercepts = getInterceptTimes(p, s);
        }
        */
        intercepts = getInterceptTimes(p, s);
        this.target = s.getPiper(p.id);
        for (InterceptorMath.Intercept i : intercepts) {
            System.out.println("Got intercept!");
            p.addDot(i.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.5f));
        }

        return super.getMove(p, s);
    }

    @Override
    public String getName() { return "InterceptRatStrategy"; }
}
