
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

   /* @Override
    public void onReachedTarget(Player p) {
    	if (p.id % 2 == 0)
    	{
    		p.music=true;
    		p.setStrategy(new PickerStrategy());
    	}
    	else
    	{
    		p.music=true;
    		p.setStrategy(new CollectorStrategy());
    	}
    }*/
    
    @Override
    public void onReachedTarget(Player p) {
    	p.setStrategy(new TotalVerticalSweep());
    }

    @Override
    public String getName() { return "CrossGateStrategy"; }
}
