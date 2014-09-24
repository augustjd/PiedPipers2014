package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class PickerStrategy extends TargetStrategy{
	
	int rat_index;
	double radius_of_handoff = 5.0;
	int ticks_to_collector = 30;
	
    List<InterceptorMath.Intercept> intercepts = null;
    Point border1,border2;
    Polygon partition;
    
    public static List<InterceptorMath.Intercept> getInterceptTimes(Player p, Scene s) {
        List<InterceptorMath.Intercept> result = new ArrayList<InterceptorMath.Intercept>();
        for (Integer i : s.getFreeRats()) {
            InterceptorMath.Intercept intercept = InterceptorMath.getSoonestIntercept(i, p.id, p.getSpeed(), s);
            if (intercept != null) {
                //p.addDot(intercept.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.25f * (intercept.number_of_bounces + 1)), 9.0);
                //System.out.format("Turn on which I'll catch rat %d after %d bounces: %f\n", i, intercept.number_of_bounces, intercept.time + s.getTime());
                intercept.rat_index = i;
                result.add(intercept);
            }
        }
        return result;
    }

    int rat_target = -1;
    InterceptorMath.Intercept best = null;
    
    /*
    @Override
    public Vector getMove(Player p, Scene s) {
		intercepts = getInterceptTimes(p, s);
        p.music = true;
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
*/

	public boolean hasRats(Player p, Scene s)
	{
		boolean flag_already_attracted = false;
		 for(int i = 0; i < s.rats.length; i++)
	        {
	        	flag_already_attracted = false;
	        	if(s.getSide(s.rats[i]) == Scene.Side.RIGHT)
	        	{
	        		for(int j = 0; j < s.pipers.length; j++)
	        		{
	        			if(j != p.id)
	        			{
	        				double dist = s.rats[i].distanceTo(s.pipers[j]);
	        				if(dist < Player.WALK_DIST && s.music[j]) // the rat is attracted by a piper
	        				{
	        					flag_already_attracted = true;
	        					break;
	        				}
	        			}
	        		}
	        		if(!flag_already_attracted)
	        		{
	        			double dist = s.rats[i].distanceTo(s.pipers[p.id]);
	        			if(dist < Player.WALK_DIST)
	        			{
	        				return true;
	        			}
	        		}
	        	}
	        }
		 return false;
	}
	
    public int chooseRat(Player p, Scene s) 
    {
        double dist_closest = Double.POSITIVE_INFINITY;
        int closest_index = -1;
        boolean flag_already_attracted = false;

        Vector position = s.getPiper(p.id);
        
        //System.out.println("the size of rat is: " + s.rat_vectors.size());
        for(int i = 0; i < s.rats.length; i++)
        {
        	flag_already_attracted = false;
        	//if(s.getSide(s.rat_vectors.get(i)) == Scene.Side.RIGHT)
        	if(s.rats[i].x > s.dimension * 0.5)
        	{
        		for(int j = 0; j < s.pipers.length; j++)
        		{
        			double dist1 = s.rats[i].distanceTo(s.getPiper(j));
        			if(dist1 < Player.WALK_DIST && s.music[j]) // the rat is attracted by a piper
        			{
        				flag_already_attracted = true;
        				break;
        			}
        		}
        		if(!flag_already_attracted)
        		{
        			double dist2 = s.rats[i].distanceTo(s.getPiper(p.id));
        			//System.out.println("Found a rat which is not attracted");
        			if(dist2 < dist_closest)
        			{
        				dist_closest = dist2;
        				closest_index = i;
        			}
        		}
        	}
        }
        //System.out.println("piper: " + p.id);
        //System.out.println("chooserat: " + closest_index);
        return closest_index; 
    }
        
        @Override
        public Vector getMove(Player p, Scene s) 
        {
            if (nearTargetRat(p, s)) 
            {
            	p.music = true;

                //this.rat_index = chooseRat(p, s);
                rat_index = chooseRat(p, s);
                if(rat_index >= 0)
                	this.rat_index = rat_index;
            }
            if (hasRats(p,s)) {
            	p.music = true;
            } else {
            	p.music = false;
            	 rat_index = chooseRat(p, s);
	             if(rat_index >= 0)
	                {
	            	 this.rat_index = rat_index;
	            	 this.target = getTargetRatPosition(s);
	                }
   	            return super.getMove(p, s); 
            }
            
    		intercepts = getInterceptTimes(p, s);
            p.music = true;
            for (InterceptorMath.Intercept i : intercepts) {
                p.addDot(i.location.asPoint(), new Color(0.7f, 0.2f, 0.0f, 0.5f), 9.0);
                if (s.getSide(i.location) == Scene.Side.RIGHT &&
                    (best == null || i.time < best.time)) {
                    best = i;
                }
            }

            if (best != null) {
                this.target = best.location;
                p.addDot(best.location.asPoint(), new Color(1.0f, 1.0f, 1.0f, 0.9f), 3.0);
            }
            best = null;
            
            // if the picker reaches the collector, do hand off
            if(p.id + 1 < s.pipers.length) // the picker has a collector to help it
            {
            	double dist = s.getPiper(p.id).distanceTo(s.getPiper(p.id + 1));
           /* 	if(dist < radius_of_handoff)
            	{
            		System.out.println("---------inside radius_of_handoff, handoff to the collector--------");
            		p.turnOffMusic();
            		//this.rat_index = chooseRat(p, s);
            		rat_index = chooseRat(p, s);
		             if(rat_index >= 0)
		                {
		            	 this.rat_index = rat_index;
		                }
            	}
            	else // decide whether should go to the collector
*/	 	            {
 	            	double dis_picker_rat = Double.POSITIVE_INFINITY;
 	        		double dis_picker_collector = Double.POSITIVE_INFINITY;
 	        		double dis_rat_collector = Double.POSITIVE_INFINITY;
 	        		double t_picker_rat			= Double.POSITIVE_INFINITY;
 	        		double t_picker_collector	= Double.POSITIVE_INFINITY;
 	        	
 	        		if(this.target != null)
 	        		{
	            		//System.out.println("----------i am picker, what should i do, i am not new collector");

 	        			dis_picker_rat = s.getPiper(p.id).distanceTo(this.target);
 	        			if(p.id + 1 < s.pipers.length)
 	        			{
 	        			
 	        				//System.out.println("hi, i am the picker and I have a collector");
 	        				dis_picker_collector = s.getPiper(p.id).distanceTo(s.getPiper(p.id + 1));
 	        				dis_rat_collector    = this.target.distanceTo(s.getPiper(p.id + 1));
 	        				t_picker_rat 	   = dis_picker_rat / 2.0 + dis_rat_collector;
 		        			t_picker_collector   = dis_picker_collector + dis_rat_collector / 5.0;
 		        			//System.out.println("the t_picker_collector is: " + t_picker_collector);
 		        			//System.out.println("the t_picker_rat is: " + t_picker_rat);
 		        			if(t_picker_collector <= ticks_to_collector)
 		        			{
 		        				p.setStrategy(new ReturnToCollectorStrategy(s));
 		        			}
 		        			if(t_picker_collector < t_picker_rat)
 		            		{
 		        				System.out.println("I am the picker, I should do handoff to the collector first");
 		            			p.setStrategy(new ReturnToCollectorStrategy(s));
 		            		}
 		        			else
 		        			{
 		        				System.out.println("I AM THE PICKER, i SHOULD CHASE RAT");
 		        				p.setStrategy(new PickerStrategy());
 		        			}
 	        			}
 	        		}
 	            }
 	            
            }
          
           /* if(s.free_rats.size() == 0)
            	p.setStrategy(new ReturnToGateStrategy(s));
            	*/
            return super.getMove(p, s);
        }

        public Vector getTargetRatPosition(Scene s) 
        {
        	// sunyun change it
        	if(rat_index >= 0)
        		return s.rats[rat_index];
        	else
        		return null;
        }

        public boolean nearTargetRat(Player p, Scene s)
        {
            Vector position = s.getPiper(p.id);
            Vector target = getTargetRatPosition(s);
            // sunyun change it
            if(target != null)
            	return position.distanceTo(target) < Player.WALK_DIST &&
                s.getSide(position) == s.getSide(target);
            else
            	return false;
            
        }
}


