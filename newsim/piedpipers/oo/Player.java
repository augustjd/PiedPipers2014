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
            return Piedpipers.MUSICPIPER_SPEED;
        } else {
            return Piedpipers.PIPER_SPEED;
        }
    }

    Strategy strategy = null;

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
            super.addDot(p,c);
        }
    }

    public void addDot(Point p, Color c, double d) {
        if (SHOW_DOTS) {
            super.addDot(p,c,d);
        }
    }

    public void setDefaultStrategy(Scene s) {
        setStrategy(
            new PhaseStrategy(new PhaseStrategy.Phase[] {
                    new PhaseStrategy.DensityPhase(0.0, 0.00035, new PickerCollectorStrategy(this,s)),
                    //new PhaseStrategy.AlwaysPhase(new VerticalSweepStrategy(this,s)),
                    //new PhaseStrategy.AlwaysPhase(new PickerCollectorStrategy(this, s)),
                    //new PhaseStrategy.DensityPhase(0.002, Double.POSITIVE_INFINITY, new VerticalSweepStrategy(this, s)),
                    new PhaseStrategy.AlwaysPhase(new InterceptRatStrategy())
                }, 
                new ReturnToGateStrategy(s) // default strategy
            )
        );
    }
    public void setStrategy(Strategy newStrategy) {
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
