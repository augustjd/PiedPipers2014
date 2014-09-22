package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class RadialInterceptRatStrategy extends InterceptRatStrategy {
    Point border1,border2;
    Polygon partition;

    @Override
    public Vector getMove(Player p, Scene s) {
        if (partition == null) {
            set_limits(p,s);
        }
        return super.getMove(p,s);
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
		int theta1 =  (p.id)*(180/(s.pipers.length+1));
		int theta2 =  (p.id+1)*(180/(s.pipers.length+1));
		if (p.id==0)
		{
			border1.x=s.dimension/2;
			border1.y=s.dimension;
		}
		else
		{
			border1.x=s.getGatePosition().x + rad * Math.sin(theta1 * Math.PI / 180);
			border1.y=s.getGatePosition().y + rad * Math.cos(theta1 * Math.PI / 180);
		}
		
		if (p.id==(s.pipers.length-1))
		{
			border2.x=s.dimension/2;
			border2.y=0;
		}
		else
		{
			border2.x=s.getGatePosition().x  + rad * Math.sin(theta2 * Math.PI / 180);
			border2.y=s.getGatePosition().y  + rad * Math.cos(theta2 * Math.PI / 180);
		}
		partition=new Polygon();
		partition.addPoint((int)s.getGatePosition().x,(int)s.getGatePosition().y);
		partition.addPoint((int)border1.x,(int)border1.y);
		partition.addPoint((int)border2.x,(int)border2.y);
	}
    public boolean inMyPartition(Player p, Scene s, InterceptorMath.Intercept i) {
        return partition.contains(i.location.x, i.location.y);
    }

    @Override
    public String getName() { return "RadialInterceptRatStrategy"; }
}
