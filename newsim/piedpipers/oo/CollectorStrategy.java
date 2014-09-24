package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class CollectorStrategy extends TargetStrategy
{
    	int id;
    	/*
    	public CollectorStrategy(Player p, Scene s) 
    	{
    		 super(s.getGatePosition());
             this.id = p.id;
        }*/
    	
    	@Override
        public Vector getMove(Player p, Scene s) 
    	{
    		Vector me = s.getPiper(p.id);
/*    		if(s.getSide(me) == Scene.Side.RIGHT)
    		{
    			p.music = true;
    		}*/
    		if(p.id - 1 >= 0)
    			this.target = s.getPiper(p.id-1);
            return super.getMove(p, s);
        }
}



