package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

//import piedpipers.dumb1.Player.Cluster;
import piedpipers.sim.*;

import java.awt.Color;
import java.awt.Polygon;

public class ClusterCollectorStrategy extends TargetStrategy {
    

	public class Cluster 
	{
        public Point centroid;
        public List<Point> points;
        public boolean taken;
        public double total_distance_centroid_moved = 0.0;

        public Cluster(Point centroid, List<Point> points) 
        {
            this.centroid = centroid;
            this.points = points;
            this.taken=false;
        }
        
        Point centroid(List<Point> points) 
        {
            Point total = new Point(0,0);
            
            for (Point p : points) 
            {
                total.x += p.x;
                total.y += p.y;
            }

            total.x /= points.size();
            total.y /= points.size();

            return total;
        }   
        

        public double redefineCentroid(Player p, Scene s) 
        {
            Point oldcentroid = new Point(centroid.x, centroid.y);
            this.centroid = centroid(this.points);

            total_distance_centroid_moved = s.distance(oldcentroid, this.centroid);
            return total_distance_centroid_moved;
        }
    }
	
	void kmeans(int k, Player p, Scene s) 
	{
        clusters.clear();

        for (int i = 0; i < k; i++) 
        {
        	Vector rat_as_centroid = s.getRat(s.getFreeRats().get(i));
        	Point centroid_rat=new Point();
        	centroid_rat.x=rat_as_centroid.x;
        	centroid_rat.y=rat_as_centroid.y;
            clusters.add(new Cluster(centroid_rat, new ArrayList<Point>()));
        }

        while (true) {
            for (int j = 0; j < s.getFreeRats().size(); j++) 
            {
            	Point rat_get=new Point();
            	rat_get.x=s.getRat((s.getFreeRats().get(j))).x;
            	rat_get.y=s.getRat((s.getFreeRats().get(j))).y;
                int best_centroid = getBestCentroid(rat_get, clusters,s);
                clusters.get(best_centroid).points.add(rat_get);
            }

            double total_centroid_motion = 0;
            for (Cluster c : clusters) {
                total_centroid_motion += c.redefineCentroid(p,s);
            }
            if ((int)total_centroid_motion == 0) {
               // System.out.println("Exiting with threshold " + total_centroid_motion);
                break;
            }
           // System.out.println("" + total_centroid_motion);
        }
    }
	
	int getBestCentroid(Point p, List<Cluster> clusters, Scene s)
    {
        double d = Double.POSITIVE_INFINITY;
        int best = -1;
        for (int i = 0; i < clusters.size(); i++) 
        {
            Cluster c = clusters.get(i);
           // Vector vp = new Vector();
            //vp.x=p.x;
            //vp.y=p.y;
            //Vector vc = new Vector();
           // vc.x=c.centroid.x;
            //vc.y=c.centroid.y;
            if (s.distance(c.centroid, p) < d) 
            {
                d = s.distance(c.centroid, p);
                best = i;
            }
        }
        return best;
    }
	
	
	List<Cluster> clusters = new ArrayList<Cluster>();
	
    @Override
    public Vector getMove(Player p, Scene s) {
    	p.music=true;
    	kmeans(9,p,s);
    	Cluster best = null;
        for (Cluster c : clusters) 
        {
            if (best == null || c.points.size() > best.points.size()) 
            {
                best = c;
            }
        }
        Vector bestvector = new Vector(best.centroid.x, best.centroid.y);
        this.target =  bestvector; 
        return super.getMove(p, s);
}
    
  
   // @Override
   // public String getName() { return "InterceptRatStrategy"; }
}
