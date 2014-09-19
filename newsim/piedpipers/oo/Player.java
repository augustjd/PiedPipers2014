package piedpipers.oo;

import java.util.*;

import piedpipers.sim.Point;

import java.awt.Color;
public class Player extends piedpipers.sim.Player {
    public void init() {}


    public static int pipers_moved = 0;
	public Point move(Point[] pipers, 
                      Point[] rats, 
                      boolean[] music, 
                      int[] thetas, 
                      int tick) {
        Scene scene;
        pipers_moved = (pipers_moved + 1) % pipers.length;

        if (pipers_moved == 1) {
            scene = Scene.getOrCreateScene(pipers, rats, music, thetas, dimension, tick);

            Scene next = scene;
            Scene second_next = scene;
            try {
                next = next.lookAhead(10);
                second_next = scene.altLookAhead(10);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.clearDots();
            for (int i = 0; i < rats.length; i++) {
                addDot(next.getRat(i).asPoint(), Color.BLUE);
                addDot(second_next.getRat(i).asPoint(), Color.RED);
            }
        }

        return new Point(0,0);
	}
}
