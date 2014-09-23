package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class PhaseStrategy implements Strategy {
    public abstract static class Phase {
        Strategy strategy;
        public Phase(Strategy strategy) {
            this.strategy = strategy;
        }

        public Vector getMove(Player p, Scene s) { return strategy.getMove(p, s); };
        public abstract boolean shouldBeActive(Player p, Scene s);
    }

    public static class DensityPhase extends Phase {
        double min;
        double max;

        public DensityPhase(double min_density, double max_density, Strategy s) {
            super(s);

            this.min = min_density;
            this.max = max_density;
        }

        public boolean shouldBeActive(Player p, Scene s) {
            double rat_density = s.getRatDensity();

            return rat_density > min && rat_density < max;
        }
    }

    public static class AlwaysPhase extends Phase {
        public AlwaysPhase(Strategy strategy) {
            super(strategy);
        }
        public boolean shouldBeActive(Player p, Scene s) {
            return true;
        }
    }

    ArrayList<Phase> phases;

    public void removePhase(int i) {
        phases.remove(i);
    }

    public PhaseStrategy(Phase[] phases, Strategy default_strategy) {
        this(new ArrayList<Phase>(Arrays.asList(phases)), default_strategy);
    }

    public PhaseStrategy(ArrayList<Phase> phases, Strategy default_strategy) {
        this.phases = phases;
        this.phases.add(new AlwaysPhase(default_strategy));
    }

    public Vector getMove(Player player, Scene s) {
        for (Phase p : phases) {
            if (p.shouldBeActive(player, s)) {
                return p.getMove(player, s);
            }
        }
        return null;
    }

    public String getName() {
        return "PhaseStrategy";
    }
}
