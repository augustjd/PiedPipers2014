package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class CrossGateStrategy extends TargetStrategy {

    public static int spacing = 10;
    public CrossGateStrategy(Player p, Scene s) {
        super(s.getGatePosition().add(2, getYPosAfterGate(p.id, s.getNumberOfPipers())));
    }

    private static double getYPosAfterGate(int id, int number_of_pipers) {
        return (id - number_of_pipers /2.0) * spacing;
    }

    @Override
    public void onReachedTarget(Player p, Scene s) {
        p.setDefaultStrategy(s);
    }

    @Override
    public String getName() { return "CrossGateStrategy"; }
}
