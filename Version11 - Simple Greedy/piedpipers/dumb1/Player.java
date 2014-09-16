package piedpipers.dumb1;

import java.awt.*;
import java.util.*;
import java.util.List;  

import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	
	static Point target = new Point();
	static int[] thetas;
	static boolean finishround = true;
	static boolean initi = false;
	
	public void init() {
		thetas = new int[npipers];
		/*for (int i=0; i< npipers; i++) {
			Random random = new Random();
			int theta = random.nextInt(180);
			thetas[i]=theta;
			System.out.println(thetas[i]);
		}*/
	}
	
	static Point loc[];
	
	Point border1,border2;
	Polygon partition;
	double current_speed;
	static double radius_of_influence = 10.0;
	boolean to_init=false;
	boolean seeking_gate=true;
	boolean search=false;
	boolean waiting=false;
	boolean working=false;
	boolean handoff=false;
	boolean lookout=false;
	boolean dropoff=false;
	boolean safedrop=false;
	boolean sweep_phase=false;
	boolean all_in_center=false;
	boolean comeback=false;
	
	int phase1_pass=0;
	
	static double EPSILON = 1;
	static boolean same_point(Point a, Point b) {
		return distance(a,b) < EPSILON;
	}
	
	public static double angle_bw(Point p1, Point p2)
    {
        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;
        return Math.atan2(yDiff, xDiff);
    }
	
	
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
        

        public double redefineCentroid() 
        {
            Point oldcentroid = new Point(centroid.x, centroid.y);
            this.centroid = centroid(this.points);

            total_distance_centroid_moved = distance(oldcentroid, this.centroid);
            return total_distance_centroid_moved;
        }
    }
	
	void kmeans(int k, List<Point> rats) 
	{
        clusters.clear();

        for (int i = 0; i < k; i++) 
        {
            clusters.add(new Cluster(rats.get(i), new ArrayList<Point>()));
        }

        while (true) {
            for (int j = 0; j < rats.size(); j++) 
            {
                int best_centroid = getBestCentroid(rats.get(j), clusters);
                clusters.get(best_centroid).points.add(rats.get(j));
            }

            double total_centroid_motion = 0;
            for (Cluster c : clusters) {
                total_centroid_motion += c.redefineCentroid();
            }
            if ((int)total_centroid_motion == 0) {
                System.out.println("Exiting with threshold " + total_centroid_motion);
                break;
            }
            System.out.println("" + total_centroid_motion);
        }
    }
	
	int getBestCentroid(Point p, List<Cluster> clusters)
    {
        double d = Double.POSITIVE_INFINITY;
        int best = -1;
        for (int i = 0; i < clusters.size(); i++) 
        {
            Cluster c = clusters.get(i);
            if (distance(c.centroid, p) < d) 
            {
                d = distance(c.centroid, p);
                best = i;
            }
        }
        return best;
    }
	
	
	List<Cluster> clusters = new ArrayList<Cluster>();
	
	
	void play_music()
	{
		current_speed=mpspeed;
	}
	
	void stop_music()
	{
		current_speed=pspeed;
	}
	
	
	void set_limits(int npipers, Point gate)
	{		
		Point corner=new Point();
		corner.x=dimension;
		corner.y=dimension;
		double rad=distance(gate,corner);
		int theta1 =  (id)*(180/(npipers+1));
		int theta2 =  (id+1)*(180/(npipers+1));
		if (id==0)
		{
			border1.x=dimension/2;
			border1.y=dimension;
		}
		else
		{
			border1.x=gate.x + rad * Math.sin(theta1 * Math.PI / 180);
			border1.y=gate.y + rad * Math.cos(theta1 * Math.PI / 180);
		}
		
		if (id==(npipers-1))
		{
			border2.x=dimension/2;
			border2.y=0;
		}
		else
		{
			border2.x=gate.x + rad * Math.sin(theta2 * Math.PI / 180);
			border2.y=gate.y + rad * Math.cos(theta2 * Math.PI / 180);
		}
		partition=new Polygon();
		partition.addPoint((int)gate.x,(int)gate.y);
		partition.addPoint((int)border1.x,(int)border1.y);
		partition.addPoint((int)border2.x,(int)border2.y);
	}
	

		
	/*List<Point> rats_left(Point current, Point rats[],Point gate)
	{
		List<Point> rats_left_to_catch = new ArrayList<Point>();
		for (Point p : rats) 
		{
            if (p.x > dimension * 0.5) 
            {
            	if(partition.contains(p.x,p.y))
            		if (distance(current,p)>radius_of_influence)
            		{
            			rats_left_to_catch.add(p);
            			//System.out.println(id+" rat: "+p.x+" "+p.y);
            		}
            }  
            
        }
		return rats_left_to_catch;
	}*/
	
	List<Point> rats_left(Point current, Point rats[],Point gate, Point pipers[])
	{
		boolean catchit=false;
		List<Point> rats_left_to_catch = new ArrayList<Point>();
		for (Point p : rats) 
		{
			catchit=true;
            if (p.x > dimension * 0.5) 
            {
            	if(partition.contains(p.x,p.y))
            	{
            			for(Point k : pipers)
            			{
            				if (distance(p,k)<radius_of_influence)
            				{
            					catchit=false;
            					break;
            				}
            			}          
            			if(catchit)
            				rats_left_to_catch.add(p);
            	}
            	
            }  
            
        }
		return rats_left_to_catch;
	}
	
	public List<Point> rats_under_influence(Point rats[],Point current)
	{
		List<Point> rats_under_influence = new ArrayList<Point>();
		for (Point p : rats) 
		{
            	if (distance(current,p)<radius_of_influence)
            			rats_under_influence.add(p);
		}
		return rats_under_influence;
	}
	
	public List<Point> rats_on_right(Point rats[],Point current)
	{
		List<Point> rats_right = new ArrayList<Point>();
		for (Point p : rats) 
		{
			if (p.x > dimension * 0.5) 
            			rats_right.add(p);
		}
		return rats_right;
	}


	public Point findTarget(List<Point> rats_in_partition)
	{
		kmeans(5, rats_in_partition);
        Cluster best = null;
        for (Cluster c : clusters) 
        {
            if (best == null || c.points.size() > best.points.size()) 
            {
                best = c;
            }
        }
        return best.centroid;
	}
	
	public Point moveToTarget(Point current, Point target) 
	{
		double dist = distance(current, target);
		assert dist > 0;
		double ox = 0, oy = 0;
		ox = (target.x - current.x) / dist * current_speed;
		oy = (target.y - current.y) / dist * current_speed;
		current.x += ox;
		current.y += oy;
		return current;
	}
	

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	// Return: the next position
	// my position: dogs[id-1]

	public Point move(Point[] pipers, // positions of dogs
			Point[] rats) { // positions of the sheeps
		npipers = pipers.length;
		System.out.println(initi);
		Point gate = new Point(dimension/2, dimension/2);
		Point gate_front=new Point();
		gate_front.x=gate.x+20;
		gate_front.y=gate.y;
		if (!initi) {
			this.init();initi = true;
		}
		loc=new Point[npipers];
		//Point handoff_point=new Point();
		Point current = pipers[id];
		double ox = 0, oy = 0;
		//nrats = rats.length;
		if ((getSide(current) == 0)&&(seeking_gate)) {
			finishround = true;
			this.music = false;
			stop_music();
			target=gate;
			System.out.println("move toward the right side");
		} 
		else if (!closetoWall(current) && finishround &&(phase1_pass==0)) {
			seeking_gate=false;
			to_init=true;
			this.music = false;
			stop_music();
			int theta = (id+1)*(180/(npipers+1));
			//System.out.println("ID "+id+"Theta"+theta);
			thetas[id]=theta;
			ox = pspeed * Math.sin(thetas[id] * Math.PI / 180);
			oy = pspeed * Math.cos(thetas[id] * Math.PI / 180);
		}
		else if (!dropoff && !safedrop && !sweep_phase) 
		{
			finishround=false;
			phase1_pass++;
			to_init=false;
			loc[id]=current;
			border1=new Point();
			border2=new Point();
			set_limits(npipers,gate);
			sweep_phase=true;
		}
		
		if(sweep_phase)
		{
			if(same_point(current,gate))
			{
				this.music=false;
				stop_music();
			}
			List<Point> rats_in_partition=rats_left(pipers[id],rats,gate,pipers);
			if(rats_in_partition.size()>0)
			{
				dropoff=false;
				Point t=null;
				double min=Double.POSITIVE_INFINITY;
				for(Point k: rats_in_partition)
				{
					if ((distance(k,current)<min)||(t==null))
					{
						min=distance(k,current);
						t=k;
					}
				}
				target=t;
			}
			else
			{
				dropoff=true;
				target=gate;
				sweep_phase=false;
				safedrop=false;
				comeback=false;
			}
		}
		
		
		
		
		if(dropoff)
		{
			if(same_point(current,target))
			{
				if(safedrop)
				{
					this.music=false;
					stop_music();
					comeback=true;
					target=gate_front;
					safedrop=false;
					System.out.println("DROPPED SAFELY");
				}
				else if (comeback)
				{
					sweep_phase=true;
					dropoff=false;
					comeback=false;
				}
				else
				{
					target.x=target.x-20;
					safedrop=true;
					System.out.println("SAFEDROP IS NOW TRUE");
				}
				
			}
		}
		
	
		
		if(to_init)
		{
			current.x += ox;
			current.y += oy;
			return current;
		}
		else
			return moveToTarget(current,target);
	}
	boolean closetoWall (Point current) {
		boolean wall = false;
		if (Math.abs(current.x-dimension)<pspeed) {
			wall = true;
		}
		if (Math.abs(current.y-dimension)<pspeed) {
			wall = true;
		}
		if (Math.abs(current.y)<pspeed) {
			wall = true;
		}
		return wall;
	}
	int getSide(double x, double y) {
		if (x < dimension * 0.5)
			return 0;
		else if (x > dimension * 0.5)
			return 1;
		else
			return 2;
	}

	int getSide(Point p) {
		return getSide(p.x, p.y);
	}

}