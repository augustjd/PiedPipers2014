package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class LookaheadInterceptRatStrategy extends TargetStrategy {
    public Vector getMove(Player p, Scene s) {
        return super.getMove(p, s);
    }

    public InterceptorMath.Intercept getBestInterceptStrategy(Player p, Scene s, int recursion_depth) {
        List<InterceptorMath.Intercept> best_schedule = null;

        for (int i = 0; i < recursion_depth; i++) {

        }
            return null;
    }

    public InterceptorMath.Intercept getBestIntercept(Player p, Scene s) {
        Vector me = s.getPiper(p.id);

        InterceptorMath.Intercept best = null;
        for (Integer i : s.getFreeRats()) {
            InterceptorMath.Intercept intercept = 
                InterceptorMath.getSoonestIntercept(i, p.id, p.getSpeed(), s);
            if (isValidRat(p, i, s) && 
                (best == null || intercept.time < best.time)) {
                best = intercept;
                best.rat_index = i;
            }
        }

        return null;
    }
    boolean isValidRat(Player p, int rat, Scene s) {
        return true;
    }
    public String getName() { return "LookaheadInterceptRatStrategy"; }
}
