package piedpipers.oo;

import java.util.*;

import piedpipers.sim.Point;
import piedpipers.sim.*;
import piedpipers.oo.InterceptorMath;

import java.awt.Color;
public class Player extends piedpipers.sim.Player {
    public static final boolean DEBUG = false;

    public void init() {
    }

    public boolean SHOW_DOTS = false;

    public static int pipers_moved = 0;
    public double getSpeed() {
        if (this.music) {
            return Player.MUSICPIPER_SPEED;
        } else {
            return Player.PIPER_SPEED;
        }
    }

    Strategy strategy = null;

	public static double WALK_DIST = 10.0; // <10m, rats walk with music piper
	public static double STOP_DIST = 2.0; // <2m, rats stop

	public static double WALK_SPEED = 0.1; // 1m/s, walking speed for rats
	public static double MUSICPIPER_SPEED = 0.1; // 1m/s, walking speed for music piper
	public static double PIPER_SPEED = 0.5; // 5m/s, walking speed for no music piper

    public static int tick = 0;
	public Point move(Point[] pipers, 
                      Point[] rats, 
                      boolean[] music, 
                      int[] thetas) {
        Scene scene = Scene.getOrCreateScene(pipers, rats, music, thetas, dimension, tick);
        tick++;

        if (this.strategy == null) {
            this.strategy = new CrossGateStrategy(this, scene);
        }
        if (DEBUG) {
            System.out.format("Rat %d %s\n", this.id, strategy.getName());
        }

        return strategy.getMove(this, scene).asPoint();
	}

    public void addDot(Point p, Color c) {
        if (SHOW_DOTS) {
        }
    }

    public void addDot(Point p, Color c, double d) {
        if (SHOW_DOTS) {
        }
    }

    public void setDefaultStrategy(Scene s) {
        setStrategy(
            new PhaseStrategy(new PhaseStrategy.Phase[] {
                    new PickerCollectorStrategy.PickerCollectorPhase(this,s),
                    new VerticalSweepStrategy.VerticalSweepPhase(this,s),
                    new PhaseStrategy.AlwaysPhase(new InterceptRatStrategy())
                }, 
                new ReturnToGateStrategy(this, s) // default strategy
            )
        );
    }
    public void setStrategy(Strategy newStrategy) {
        this.strategy = newStrategy;
    }
    public void drawBounces(Scene scene) {
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
