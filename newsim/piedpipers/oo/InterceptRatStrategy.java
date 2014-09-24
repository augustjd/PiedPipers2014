package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class InterceptRatStrategy extends TargetStrategy {
    List<InterceptorMath.Intercept> intercepts = null;
    public boolean do_partitions = true;
    public boolean do_closest_partition = false;

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
        return s.getPiper(p.id).distanceTo(best.location) < Player.WALK_DIST;
    }
    @Override
    public Vector getMove(Player p, Scene s) {
        try {
            if (s.getFreeRats().size() == 0) {
                p.setStrategy(new ReturnToGateStrategy(p, s));
            }
        } catch (NullPointerException e) {
        }

        Vector me = s.getPiper(p.id);
        Vector closest = s.getRat(getClosestRat(p,s));

        if (best == null || nearTarget(p,s)) {
            best = null;
            intercepts = getInterceptTimes(p, s);
            best = getBestIntercept(p,s);
            if (best == null) {
                best = getBestIntercept(p,s, false);
            }
        }

        if (best != null && nearTarget(p,s)) {
            p.music = true;
        }
        if (best == null) {
            Integer closestRat = getClosestRat(p,s);
            if (closestRat != null && s.getPiperClosestToRat(closestRat) == p.id) {
                this.target = s.getRat(closestRat);
            } else {
                p.setStrategy(new ReturnToGateStrategy(p, s));
            }
            return super.getMove(p,s);
        }
        this.target = best.location;

        return super.getMove(p,s);
    }

    public Integer getClosestRat(Player p, Scene s) {
        double best = Double.POSITIVE_INFINITY;
        Vector me = s.getPiper(p.id);
        Integer best_rat = null;
        for (Integer i : s.getFreeRats()) {
            if (s.getRat(i).distanceTo(me) < best) {
                best = s.getRat(i).distanceTo(me);
                best_rat = i;
            }
        }
        return best_rat;
    }

    public boolean amISoonestIntercept(Player p, Scene s, int rat) {
        return amISoonestIntercept(p, s, InterceptorMath.getSoonestIntercept(rat, p.id, p.getSpeed(), s));
    }
    public boolean amISoonestIntercept(Player p, Scene s, InterceptorMath.Intercept i) {
        for (int j = 0; j < s.getNumberOfPipers(); j++) {
            if (j == p.id) continue;
            InterceptorMath.Intercept intercept = InterceptorMath.getSoonestIntercept(
                                                      i.rat_index, j, s.getPiperSpeed(j), s);

            if (intercept != null && intercept.time <= i.time) {
                return false;
            }
        }
        return true;
    }

    public boolean inMyPartition(Player p, Scene s, InterceptorMath.Intercept i) {
       if (!do_partitions) return true;
       if (do_closest_partition) return amISoonestIntercept(p,s,i);

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
