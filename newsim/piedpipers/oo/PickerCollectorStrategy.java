package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class PickerCollectorStrategy implements Strategy {
    public static class PickerCollectorPhase extends PhaseStrategy.Phase {
        public PickerCollectorPhase(Player p, Scene s) {
            super(new PickerCollectorStrategy(p,s));
        }
        public boolean shouldBeActive(Player p, Scene s) {
            double small_board_min_density = 0.0;
            double small_board_max_density = 0.0009;

            double large_board_min_density = 0.0;
            double large_board_max_density = 0.00035;

            if (s.dimension >= 400) {
                return s.getNumberOfPipers() > 3 && 
                    s.getRatDensity() > large_board_min_density && 
                    s.getRatDensity() <= large_board_max_density;
            } else {
                return s.getNumberOfPipers() > 3 && 
                    s.getRatDensity() > small_board_min_density && 
                    s.getRatDensity() <= small_board_max_density;
            }
        }
    }
    Strategy inner;
    public PickerCollectorStrategy(Player p, Scene s) {
        if (p.id % 2 == 0) {
            inner = new PickerStrategy();
        } else {
            inner = new CollectorStrategy();
        }
    }
    
    public Vector getMove(Player p, Scene s) {
        if (s.getFreeRats().size() == 0 ||
            s.getFreeRats().size() < s.getNumberOfPipers() && !amIClosest(p,s)) {
            p.setStrategy(new ReturnToGateStrategy(p, s));
        }
        p.music = true;
        return inner.getMove(p,s);
    }

    public boolean amIClosest(Player p, Scene s) {
        for (Integer i : s.getFreeRats()) {
            int piper = s.getPiperClosestToRat(i);
            if (piper == p.id) return true;
            else if (s.getRat(i).distanceTo(s.getPiper(piper)) - s.getRat(i).distanceTo(s.getPiper(p.id)) < 50.0) {
                return true;
            }
        }
        return false;
    }

    public String getName() { return "PickerCollectorStrategy"; }
}



