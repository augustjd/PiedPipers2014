package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class ClusterHelperStrategy extends TargetStrategy {
    List<InterceptorMath.Intercept> intercepts = null;
    Point border1,border2,border3,border4;
    Polygon partition;
    
    public static List<InterceptorMath.Intercept> getInterceptTimes(Player p, Scene s) {
        List<InterceptorMath.Intercept> result = new ArrayList<InterceptorMath.Intercept>();
        for (Integer i : s.getFreeRats()) {
            InterceptorMath.Intercept intercept = InterceptorMath.getSoonestIntercept(i, p.id, p.getSpeed(), s);
            if (intercept != null) {
                p.addDot(intercept.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.25f * (intercept.number_of_bounces + 1)), 9.0);
                System.out.format("Turn on which I'll catch rat %d after %d bounces: %f\n", i, intercept.number_of_bounces, intercept.time + s.getTime());
                intercept.rat_index = i;
                result.add(intercept);
            }
        }
        return result;
    }
    
    public void helper_within_radius(Player p, Scene s)
    {
    	for (int v=0; v < s.pipers.length; v++)
    	{
    		if (s.getPiper(v).distanceTo(s.getPiper(p.id))<Piedpipers.WALK_SPEED * 0.5)
    		{
    			if (v < p.id)
    			{
    				p.music=false;
    				p.setStrategy(new ClusterHelperStrategy());
    				break;
    			}
    		}
    	}
    }
    
    int rat_target = -1;
    InterceptorMath.Intercept best = null;
    @Override
    public Vector getMove(Player p, Scene s) {
    	set_limits(p,s);
    	helper_within_radius(p,s);
    	if (s.getPiper(p.id).distanceTo(target) < Piedpipers.WALK_SPEED &&
    			s.getSide(s.getPiper(p.id)) == s.getSide(target))
    	{
    		p.music = true;
    		p.setStrategy(new ClusterDropoffStrategy());
    	}
		intercepts = getInterceptTimes(p, s);
        
        for (InterceptorMath.Intercept i : intercepts) {
            p.addDot(i.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.5f), 9.0);
            if (s.getSide(i.location) == Scene.Side.RIGHT && this.partition.contains(i.location.x,i.location.y) &&
                (best == null || i.time < best.time)) {
                best = i;
            }
        }

        if (best != null) {
            this.target = best.location;
            p.addDot(best.location.asPoint(), new Color(1.0f, 1.0f, 1.0f, 0.9f), 3.0);
        }
        best = null;
        return super.getMove(p, s);
}
    
    public void set_limits(Player p, Scene s)
	{		
    	Point corner=new Point();
		corner.x=s.dimension;
		corner.y=s.dimension;
		Point gate = new Point();
		gate.x=s.getGatePosition().x;
		gate.y=s.getGatePosition().y;
		final double rad=s.distance(gate,corner);
    	border1=new Point();
    	border2=new Point();
    	border3=new Point();
    	border4=new Point();
		
    	border1.x = s.getPiper(0).x-40;
    	border1.y = s.getPiper(0).y-40;
    	border2.x = s.getPiper(0).x+40;
    	border2.y = s.getPiper(0).y+40;
    	border3.x = s.getPiper(0).x-40;
    	border3.y = s.getPiper(0).y+40;
    	border4.x = s.getPiper(0).x+40;
    	border4.y = s.getPiper(0).y-40;
    	
		partition=new Polygon();
		partition.addPoint((int)s.getGatePosition().x,(int)s.getGatePosition().y);
		partition.addPoint((int)border1.x,(int)border1.y);
		partition.addPoint((int)border2.x,(int)border2.y);
		partition.addPoint((int)border3.x,(int)border3.y);
		partition.addPoint((int)border4.x,(int)border4.y);
	}
/*
    public boolean inMyPartition(Player p, Scene s, InterceptorMath.Intercept i) {
       double partition_size = s.dimension / s.getNumberOfPipers();
       double top_of_mine    = p.id * partition_size;
       double bottom_of_mine = (p.id + 1) * partition_size;

       return i.location.y > top_of_mine && i.location.y < bottom_of_mine;
    }

    InterceptorMath.Intercept getBestIntercept(Player p, Scene s) {
        for (InterceptorMath.Intercept i : intercepts) {
            if (s.getSide(i.location) == Scene.Side.RIGHT &&
                inMyPartition(p,s,i) &&
                (best == null || i.time < best.time)) {
                best = i;
            }
        }
        return best;
    }
*/
    @Override
    public String getName() { return "InterceptRatStrategy"; }
}
