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
	boolean last_phase=false;
	
	static int ncluster=5;
	
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
	
	List<Point> rats_on_right(Point rats[],Point gate)
	{
		List<Point> rats_on_right = new ArrayList<Point>();
		for (Point p : rats) 
		{
            if (p.x > dimension * 0.5) 
            {
            	rats_on_right.add(p);
            }
		}
		return rats_on_right;
	}
		
	List<Point> rats_left(Point current, Point rats[],Point gate)
	{
		List<Point> rats_left_to_catch = new ArrayList<Point>();
		for (Point p : rats) 
		{
            if (p.x > dimension * 0.5) 
            {
            	/*System.out.Math.abs(angle_bw(p,border1)+angle_bw(p,border2)+angle_bw(p,gate)));
            	if((int)Math.abs(angle_bw(p,border1)+angle_bw(p,border2)+angle_bw(p,gate))!=0)
            	{*/
            	if(partition.contains(p.x,p.y))
            		if (distance(current,p)>radius_of_influence)
            		{
            			rats_left_to_catch.add(p);
            			//System.out.println(id+" rat: "+p.x+" "+p.y);
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


	public Point findTarget(List<Point> rats_in_partition, Point current)
	{
		int count = 0;
		double min_dis = Double.POSITIVE_INFINITY;
		double cur_dis;
		Point min_dis_point = new Point();
		kmeans(10, rats_in_partition);
		Collections.sort(clusters, new Comparator<Cluster>() {
			  public int compare(Cluster c1, Cluster c2) {
			    if (c1.points.size() > c2.points.size()) return 1;
			    if (c1.points.size() < c2.points.size()) return -1;
			    return 0;
			  }});
        for(Cluster a : clusters)
        {
        	count++;
        	cur_dis = distance(current, a.centroid);
        	if(cur_dis < min_dis)
        	{
        		min_dis = cur_dis;
        		min_dis_point = a.centroid;
        	}
        		
        	if(count > ncluster)
        		break;
        }
        
        return min_dis_point;
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
		System.out.println("RATS ON RIGHT: "+rats_on_right(rats,gate).size());
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
		else if (!closetoWall(current) && finishround) {
			seeking_gate=false;
			to_init=true;
			this.music = false;
			int theta = (id+1)*(180/(npipers+1));
			//System.out.println("ID "+id+"Theta"+theta);
			thetas[id]=theta;
			ox = pspeed * Math.sin(thetas[id] * Math.PI / 180);
			oy = pspeed * Math.cos(thetas[id] * Math.PI / 180);
		}
		else if (!dropoff && !safedrop && !last_phase) 
		{
			finishround=false;
			to_init=false;
			this.music=true;
			play_music();
			loc[id]=current;
			border1=new Point();
			border2=new Point();
			set_limits(npipers,gate);
			//System.out.println("id"+id+" "+"border1x "+border1.x+"border1y "+border1.y+"border2x "+border2.x+"border2y "+border2.y);
			search=true;
		}
		
		
		if(search)
		{
			List<Point> rats_in_partition=rats_left(pipers[id],rats,gate);
			List<Point> rats_with_me=rats_under_influence(rats,current);
			if(rats_in_partition.size()>7)
			{
				if (!working)
				{  
		             target = findTarget(rats_in_partition,current);
		             working=true;
				}
				else
				{
					if(same_point(current,target))
					{
						double caught=rats_with_me.size();
						double left=rats_in_partition.size();
						if((caught/(caught+left))>=(0.33))
						{
							//handoff=true;
							dropoff=true;
							target=gate;
							search=false;
							//System.out.println("HANDOFF "+id+" "+caught+" "+left);
						}
						else
						{
							this.music=true;
							play_music();
							dropoff=false;
							//handoff=false;
							working=false;
						}
					}
				}
			}
			else
			{
				last_phase=true;
				search=false;
				//working=false;
			}
		}
		
		if(last_phase)
		{
			System.out.println(id+" LASSSSSTTTTT PHHHHASSSEEE");

			this.music=true;
			play_music();
			List<Point> rats_in_partition=rats_left(pipers[id],rats,gate);
			if(rats_in_partition.size()!=0)
			{
				dropoff=false;
				double min_dis=Double.POSITIVE_INFINITY;
				Point closest_rat=null;
				for (Point r: rats_in_partition)
				{
					if((closest_rat==null)||(distance(r,current)<min_dis))
					{
						min_dis=distance(r,current);
						closest_rat=r;
					}
				}
				target=closest_rat;
			}
			else
			{
				dropoff=true;
				safedrop = false;
				target=gate;
				last_phase=false;
			}
		}
		
		if(dropoff)
		{
			if(last_phase)
				System.out.println("Last phase dropoff");
			if(distance(current, target) <= mpspeed/*same_point(current,target)*/)
			{
				if(safedrop)
				{
					this.music=false;
					stop_music();
					seeking_gate=true;
					target=gate;
					dropoff=false;
					safedrop=false;
					if(last_phase)
						System.out.println("DROPPED SAFELY");
				}
				else
				{
					target.x=target.x-20;
					safedrop=true;
					if(last_phase)
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