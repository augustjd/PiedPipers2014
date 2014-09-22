package piedpipers.oo;

import java.util.*;

import piedpipers.sim.Point;
import piedpipers.sim.*;
import piedpipers.oo.InterceptorMath;

import java.awt.Color;
public class Player extends piedpipers.sim.Player {
    public static final boolean DEBUG = true;

    public void init() {
    }

    public boolean SHOW_DOTS = false;

    public static int pipers_moved = 0;
    public double getSpeed() {
        if (this.music) {
            return Piedpipers.MUSICPIPER_SPEED;
        } else {
            return Piedpipers.PIPER_SPEED;
        }
    }

    Strategy strategy = null;

	public Point move(Point[] pipers, 
                      Point[] rats, 
                      boolean[] music, 
                      int[] thetas, 
                      int tick) {
        Scene scene = Scene.getOrCreateScene(pipers, rats, music, thetas, dimension, tick);

        this.clearDots();
        if (this.strategy == null) {
            this.strategy = new CrossGateStrategy(scene);
        }

        return strategy.getMove(this, scene).asPoint();
	}

    @Override
    public void addDot(Point p, Color c) {
        if (SHOW_DOTS) {
            super.addDot(p,c);
        }
    }

    @Override
    public void addDot(Point p, Color c, double d) {
        if (SHOW_DOTS) {
            super.addDot(p,c,d);
        }
    }

    public void setStrategy(Strategy newStrategy) {
        if (DEBUG) {
            System.out.format("Rat %d %s --> \033[33m%s\033[0m\n", this.id, strategy.getName(), newStrategy.getName());
        }
        this.strategy = newStrategy;
    }
    public void drawBounces(Scene scene) {

            this.clearDots();
            for (int i = 0; i < scene.getNumberOfRats(); i++) {
                //addDot(next.getRat(i).asPoint(), Color.BLUE);
                //addDot(second_next.getRat(i).asPoint(), Color.RED);
                Vector next_bounce = InterceptorMath.getNextBounceLocation(i, scene);
                if (next_bounce != null) {
                    addDot(next_bounce.asPoint(), new Color(0.0f,0.5f,0.9f,0.5f), 4.0);
                }
                //addDot(second_next.getRat(i).asPoint(), Color.RED);
            }
    }
}
