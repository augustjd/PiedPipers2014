package piedpipers.sim;
import java.util.*;
import java.awt.Color;

public abstract class Player {
	public int id; // id of the piper, 1,2,3...npiper
	public int dimension;
	
	public boolean music;

	public Player() {
	}

	public abstract void init();

	// Return: the next position
	// my position: pipers[id-1]
	//public abstract Point move(Point[] pipers, // positions of pipers
		//	Point[] rats, boolean[] pipermusic); // positions of the rats
	
	public abstract Point move(Point[] pipers, // positions of pipers
			Point[] rats, boolean[] pipermusic, int[] thetas, int tick); 

    public void addDots(List<Piedpipers.Dot> other_dots) {
        other_dots.addAll(this.dots);
    }

    public void addDot(Point p, Color c, double radius) {
        this.dots.add(new Piedpipers.Dot(p,c,radius));
    }
    public void addDot(Point p, Color c) {
        this.dots.add(new Piedpipers.Dot(p,c));
    }

    public void clearDots() {
        this.dots.clear();
    }

    public List<Piedpipers.Dot> dots = new ArrayList<Piedpipers.Dot>();
}
