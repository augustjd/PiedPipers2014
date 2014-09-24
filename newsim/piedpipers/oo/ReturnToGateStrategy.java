package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class ReturnToGateStrategy extends TargetStrategy {
    public ReturnToGateStrategy(Player p, Scene s) {
        super(s.getGatePosition().sub(50,20));
        p.music = true;
    }

    @Override
    public void onReachedTarget(Player p, Scene s) {
        if (s.getFreeRats().size() > 0) {
            p.music = false;
            p.setStrategy(new InterceptRatStrategy());
        }
    }

    @Override
    public String getName() { return "ReturnToGateStrategy"; }
}
