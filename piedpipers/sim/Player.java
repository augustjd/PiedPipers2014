package piedpipers.sim;

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
	public abstract Point move(Point[] pipers, // positions of pipers
			Point[] rats); // positions of the rats

    public void setRatColor(int i, Color c) {
        Piedpipers.rat_colors[i] = c;
    }
}
