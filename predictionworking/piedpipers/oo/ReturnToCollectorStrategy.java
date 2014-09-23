package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class ReturnToCollectorStrategy extends TargetStrategy 
{
	
    public ReturnToCollectorStrategy(Scene s) {
        super(s.getGatePosition().add_ip(GATE_BUFFER,0));
    }
    
    public Vector getMove(Player p, Scene s) 
    {
    	//System.out.println("I am a picker and I am in the ReturnToCollectorStrategy");
    	this.target = s.getPiper(p.id+1);
        if (s.getPiper(p.id).distanceTo(this.target) < 4.5)
        {
        	//System.out.println("In the ReturnToCollector, arrive at collecotr");
            p.music=false;
            p.setStrategy(new PickerStrategy());
        } 
        else
        {
        	this.target = s.getPiper(p.id+1);
        }
        return super.getMove(p,s);
    }
}



