
package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class CrossGateStrategy extends TargetStrategy {
    public CrossGateStrategy(Scene s) {
        super(s.getGatePosition().add(2,0));
    }

    @Override
    public void onReachedTarget(Player p) {
        p.setStrategy(new RadialInterceptRatStrategy());
    }

    @Override
    public String getName() { return "CrossGateStrategy"; }
}
