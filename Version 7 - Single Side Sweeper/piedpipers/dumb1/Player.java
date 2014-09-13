package piedpipers.dumb1;

import java.util.*;

import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	
	static Point target = new Point();
	static int[] thetas;
	static boolean finishround = true;
	static boolean initi = false;
	
	double current_speed = pspeed;
	boolean seeking_gate=true;
	boolean goup=false;
	boolean godown=false;
	boolean moveinit=false;
	boolean seeking_center=false;
	boolean dropoff=false;
	boolean safedrop=false;
	
	public void init() {
		thetas = new int[npipers];
		/*for (int i=0; i< npipers; i++) {
			Random random = new Random();
			int theta = random.nextInt(180);
			thetas[i]=theta;
			System.out.println(thetas[i]);
		}*/
	}
	
	static boolean same_point(Point a, Point b) 
	{
		return (int)distance(a,b) == 0;
	}
	void play_music()
	{
		current_speed=mpspeed;
	}
	
	void stop_music()
	{
		current_speed=pspeed;
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
		if (!initi) {
			this.init();initi = true;
		}
		Point current = pipers[id];
		//nrats = rats.length;
		if ((getSide(current)==0) && (seeking_gate))
		{
			this.music = false;
			stop_music();
			target=gate;
			System.out.println("move toward the right side");
		} 
		else if (seeking_gate && !(getSide(current) == 0))
		{
			seeking_gate=false;
			moveinit=true;
		}
		
		
		if (moveinit)
		{
				target.y=dimension;
				target.x=10+(dimension*0.5)+(dimension*0.5/npipers)*id;
			
			if(same_point(current,target))
			{
				this.music=true;
				play_music();
				goup=true;
				moveinit=false;
			}
		}
		
		if (godown)
		{
				target.y=dimension;
			if(same_point(current,target))
			{
				this.music=true;
				play_music();
				seeking_center=true;
				godown=false;
			}
		}
		
		
		if(goup)
		{
			target.y=0;
			if(same_point(current,target))
			{
				godown=true;
				goup=false;
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
					seeking_gate=true;
					target=gate;
					dropoff=false;
					safedrop=false;
					System.out.println("DROPPED SAFELY");
				}
				else
				{
					target.x=target.x-20;
					safedrop=true;
					System.out.println("SAFEDROP IS NOW TRUE");
				}
			}
		}
			
			
		
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