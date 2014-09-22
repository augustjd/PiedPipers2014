package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class ReturnToGateStrategy extends TargetStrategy {
    public ReturnToGateStrategy(Scene s) {
        super(s.getGatePosition().sub(32,12));
    }

    @Override
    public void onReachedTarget(Player p) {
        p.music = false;
    }

    @Override
    public String getName() { return "ReturnToGateStrategy"; }
}
