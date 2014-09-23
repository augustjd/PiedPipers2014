package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class ClusterDropoffStrategy extends TargetStrategy {
    
    @Override
    public Vector getMove(Player p, Scene s) {
    	this.target=s.getPiper(0);
    	if(s.getPiper(p.id).distanceTo(s.getPiper(0)) < 4.5)
    	{
    		p.music=false;
    		p.setStrategy(new ClusterHelperStrategy());
    	}
        return super.getMove(p, s);
}
    
   
    @Override
    public String getName() { return "InterceptRatStrategy"; }
}
