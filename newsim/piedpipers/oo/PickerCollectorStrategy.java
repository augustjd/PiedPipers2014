package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class PickerCollectorStrategy implements Strategy {
    Strategy inner;
    public PickerCollectorStrategy(Player p, Scene s) {
        if (p.id % 2 == 0) {
            inner = new PickerStrategy();
        } else {
            inner = new CollectorStrategy();
        }
    }
    
    public Vector getMove(Player p, Scene s) {
        if (s.getFreeRats().size() == 0 && amIClosest(p,s)) {
            p.setStrategy(new ReturnToGateStrategy(s));
        }
        p.music = true;
        return inner.getMove(p,s);
    }

    public boolean amIClosest(Player p, Scene s) {
        for (Integer i : 
    }

    public String getName() { return "PickerCollectorStrategy"; }
}



