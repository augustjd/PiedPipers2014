package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class VerticalSweepStrategy extends TargetStrategy {
    public static class VerticalSweepPhase extends PhaseStrategy.Phase {
        public VerticalSweepPhase(Player p, Scene s) {
            super(new VerticalSweepStrategy(p,s));
        }
        public double fudge = 0.0;
        public boolean shouldBeActive(Player p, Scene s) {
            double portion = (s.dimension/2.0) / s.getNumberOfPipers();

            return portion - (Player.WALK_DIST * 2) - fudge < 0;
        }
    }
    Stage stage;
    public VerticalSweepStrategy(Player p, Scene s) {
        stage = Stage.GETTING_TO_TOP;
    }

    private enum Stage {
        GETTING_TO_TOP,
        MOVING_DOWN;
    }

    boolean wait_for_everyone = true;
    boolean greedy_strategy = false;

    public Vector getMove(Player player, Scene s) {
        if (s.getFreeRats().size() == 0) {
            player.setStrategy(new ReturnToGateStrategy(player, s));
        }
        this.stage = determineStage(player, s);

        if (stage == Stage.GETTING_TO_TOP) {
            this.target = getTopTarget(player.id, s);
            if (s.getPiper(player.id).equals(this.target)) {
                player.music = true;
            }
        } else {
            this.target = getBottomTarget(player.id, s);
            player.music = true;
        }

        return super.getMove(player, s);
    }

    private Stage determineStage(Player p, Scene s) {
        if (stage == Stage.MOVING_DOWN) return Stage.MOVING_DOWN;
        if (!wait_for_everyone && stage == Stage.GETTING_TO_TOP) {
            if (s.getPiper(p.id).equals(getTopTarget(p.id, s))) {
                return Stage.MOVING_DOWN;
            }
        }

        boolean all_on_top = true;
        for (int i = 0; i < s.getNumberOfPipers(); i++) {
            if (s.getPiper(i).distanceTo(getTopTarget(i, s)) > 1.0) {
                all_on_top = false;
            }
        }

        if (all_on_top) {
            return Stage.MOVING_DOWN;
        } else {
            return Stage.GETTING_TO_TOP;
        }
    }
    public double buffer = Player.WALK_DIST;

    public Vector getTopTarget(int piper_id, Scene s) {
        double increment  = (s.dimension/2.0) / s.getNumberOfPipers();
        double x_position = (s.dimension/2.0) + (piper_id + 0.5) * increment;

        return new Vector(x_position, buffer);
    }

    public Vector getBottomTarget(int piper_id, Scene s) {
        if (greedy_strategy) return getClosestRatBeneath(piper_id, s);

        double increment  = (s.dimension/2.0) / s.getNumberOfPipers();
        double x_position = (s.dimension/2.0) + (piper_id + 0.5) * increment;

        return new Vector(x_position, s.dimension - buffer);
    }
    public Vector getClosestRatBeneath(int piper_id, Scene s) {
        double min_distance = Double.POSITIVE_INFINITY;
        Vector position = s.getPiper(piper_id);

        Vector best = null;
        for (Integer i : s.getFreeRats()) {
            Vector rat = s.getRat(i);
            double distance = rat.distanceTo(position);
            if (distance < min_distance && position.y < rat.y) {
                min_distance = distance;
                best = rat;
            }
        }
        return best;
    }
}
