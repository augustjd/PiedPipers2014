package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class InterceptRatStrategy extends TargetStrategy {
    List<InterceptorMath.Intercept> intercepts = null;

    public static List<InterceptorMath.Intercept> getInterceptTimes(Player p, Scene s) {
        List<InterceptorMath.Intercept> result = new ArrayList<InterceptorMath.Intercept>();
        for (Integer i : s.getFreeRats()) {
            InterceptorMath.Intercept intercept = InterceptorMath.getSoonestIntercept(i, p.id, p.getSpeed(), s);
            if (intercept != null) {
                System.out.format("Intercepting rat %d in %f ticks.\n", i, intercept.time);
                result.add(intercept);
            }
        }
        return result;
    }

    InterceptorMath.Intercept best = null;
    @Override
    public Vector getMove(Player p, Scene s) {
        intercepts = getInterceptTimes(p, s);
        p.music = true;

        for (InterceptorMath.Intercept i : intercepts) {
            p.addDot(i.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.5f), 9.0);
            if (s.getSide(i.location) == Scene.Side.RIGHT &&
                (best == null || i.time < best.time)) {
                best = i;
            }
        }

        if (best != null) {
            this.target = best.location;
            p.addDot(best.location.asPoint(), new Color(1.0f, 1.0f, 1.0f, 0.9f), 3.0);
        }
        best = null;

        return super.getMove(p, s);
    }

    @Override
    public String getName() { return "InterceptRatStrategy"; }
}
