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
                p.addDot(intercept.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.25f * (intercept.number_of_bounces + 1)), 9.0);
                //System.out.format("Turn on which I'll catch rat %d after %d bounces: %f\n", i, intercept.number_of_bounces, intercept.time + s.getTime());
                intercept.rat_index = i;
                result.add(intercept);
            }
        }
        return result;
    }

    int rat_target = -1;
    InterceptorMath.Intercept best = null;

    boolean nearTarget(Player p, Scene s) {
        return s.getPiper(p.id).distanceTo(best.location) < Piedpipers.WALK_DIST;
    }
    @Override
    public Vector getMove(Player p, Scene s) {
        if (s.getFreeRats().size() == 0) {
            p.setStrategy(new ReturnToGateStrategy(s));
        }
        if (best == null || nearTarget(p,s)) {
            best = null;
            intercepts = getInterceptTimes(p, s);
            best = getBestIntercept(p,s);
            if (best == null) {
                best = getBestIntercept(p,s, false);
            }
            System.out.format("Chose a new location for piper %d\n", p.id);
        }

        if (best != null && nearTarget(p,s)) {
            p.music = true;
        }
        if (best == null) {
            this.target = s.getPiper(p.id);
            return super.getMove(p,s);
        }
        this.target = best.location;

        return super.getMove(p,s);
    }

    public boolean inMyPartition(Player p, Scene s, InterceptorMath.Intercept i) {
       double partition_size = s.dimension / s.getNumberOfPipers();
       double top_of_mine    = p.id * partition_size;
       double bottom_of_mine = (p.id + 1) * partition_size;

       return i.location.y > top_of_mine && i.location.y < bottom_of_mine;
    }

    InterceptorMath.Intercept getBestIntercept(Player p, Scene s, boolean ignore_partitions) {
        for (InterceptorMath.Intercept i : intercepts) {
            if (s.getSide(i.location) == Scene.Side.RIGHT &&
                (ignore_partitions || inMyPartition(p,s,i)) &&
                (best == null || i.time < best.time)) {
                best = i;
            }
        }
        return best;
    }
    InterceptorMath.Intercept getBestIntercept(Player p, Scene s) {
        return getBestIntercept(p,s,false);
    }

    @Override
    public String getName() { return "InterceptRatStrategy"; }
}
