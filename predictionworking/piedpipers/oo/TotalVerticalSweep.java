package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class TotalVerticalSweep extends TargetStrategy {
    int id;
    int dimension;
    /*
    public TotalVerticalSweep() {
        this.target = generateStartTarget();
    }
*/
    public double radius_factor = 2.5;

    boolean moving_to_start = true;
    boolean moving_to_center = false;
    int count = 0;
    Vector generateMiddleTarget(Player p, Scene s) {
        Vector start_target = new Vector();

        start_target.y = dimension/2;
        start_target.x = (dimension * 0.5) + (dimension * 0.5 / (s.pipers.length + 1)) * (p.id + 1);

        return start_target;
    }
    Vector generateStartTarget(Player p, Scene s) {
        Vector start_target = new Vector();


        start_target.y = Piedpipers.WALK_DIST;
        /*
        if (id % 2 == 0) {
            start_target.y = Scene.RANGE_OF_INFLUENCE;
        } else {
            start_target.y = dimension - Scene.RANGE_OF_INFLUENCE;
        }
        */

        //start_target.x = (dimension * 0.5) + (dimension * 0.5 / (s.pipers.length + 1)) * (p.id) + Piedpipers.WALK_DIST + 5;
        start_target.x = (dimension * 0.5) + (dimension * 0.5 / (s.pipers.length + 1)) * (p.id + 1);
        return start_target;
    }
    boolean all_reached (Player p, Scene s) {
    	Vector start_loc = new Vector();
    	for (int i=0; i < s.pipers.length; i++)
    	{
    		start_loc.x = (dimension * 0.5) + (dimension * 0.5 / (s.pipers.length + 1)) * (i + 1);
    		start_loc.y = Piedpipers.WALK_DIST;
    		if (s.getPiper(i).distanceTo(start_loc) > 1)
    		{
    			return false;
    		}
    	}
    	return true;
    }
    @Override
    public Vector getMove(Player p, Scene s) {
    	dimension = s.dimension;
    	if (count == 0)
    	{
    		this.target = generateStartTarget(p,s);
    		count++;
    	}
        boolean reached_target = super.reachedTarget(p, s);
        if (reached_target) {
            if (moving_to_start) {
            	if(all_reached(p,s))
            	{
		                p.music = true;
		
		                moving_to_start = false;
		                moving_to_center = true;
		
		                this.target = generateMiddleTarget(p,s);
            	}
            } else if (moving_to_center) {
                moving_to_center = false;
                this.target = s.getGatePosition().sub_ip(10,10);
            } else {
                p.music = false;
                moving_to_start = true;
                this.target = generateStartTarget(p,s);
            }
        }

        return super.getMove(p, s);
    }
}


