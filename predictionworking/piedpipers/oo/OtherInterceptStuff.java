package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class OtherInterceptStuff {

    /*
	///////////////////////////// RAT MOVEMENT PREDICTION ////////////////////////////
	// Solve a quadratic equation specified by its coefficients
	private double[] solveQuad(double a, double b, double c) {
		double[] sol = {0,0};
		if (a == 0) {
			if (b == 0) {
				if (c != 0)
					sol = null;
			} else {
				sol[0] = -c/b;
				sol[1] = -c/b;
			}
		} else {
			double disc = b*b - 4*a*c;
			if (disc >= 0) {
				disc = Math.sqrt(disc);
				a = 2*a;
				sol[0] = (-b-disc)/a;
				sol[1] = (-b+disc)/a;
			}
		}
		return sol;
	}
	
	// Returns the point the current piper should aim at if attempting to intercept an object at point p moving at velocity v, assuming no wall reflections
	private Point findIntercept(Point p, Point v, double tPast, double s) {
		// Adapted from http://stackoverflow.com/questions/2248876/2d-game-fire-at-a-moving-target-by-predicting-intersection-of-projectile-and-u
		
		double tx, ty, tvx, tvy;
		tx =  (p.x - v.x*tPast) - current_loc.x;
		ty =  (p.y - v.y*tPast) - current_loc.y;
		tvx = v.x;
		tvy = v.y;
		
		double a = tvx*tvx + tvy*tvy - s*s;
		double b = 2 * (tvx * tx + tvy * ty);
		double c = tx*tx + ty*ty;
		
		double[] ts = solveQuad(a, b, c);
		
		Point sol = null;
		if (ts != null) {
			double t0 = ts[0];
			double t1 = ts[1];
			double t = Math.min(t0, t1);
			
			if (t < tPast) t = Math.max(t0, t1);
			if (t > tPast) {
				sol = new Point((p.x - v.x*tPast) + tvx*t, (p.y - v.y*tPast) + tvy*t);
			}
		}
		
		return sol;
	}
	
	// Returns the point that the current piper should aim at to most quickly approach the given rat, including the effect of bouncing off walls
	private Point findBestApproach(Point rat_pos, Point rat_v, double speed) {
		double [] next;
		Point intercept = null;
		Point sol = null;
		double t = 0;
		double tIntercept;
		double tBest = 99999999;
		
		int numReflections = 0;
		
		// Keep reflecting off walls until we find an approach vector that works
		// Maximum number of tries
		do {
			intercept = findIntercept(rat_pos, rat_v, t);		
			if (intercept != null) {
				tIntercept = distBetween(current_loc, intercept)/speed;
				if (tIntercept < tBest && rightSide.contains(intercept)) {
					tBest = tIntercept;
					sol = intercept;
				}
			}
			
			next = intersectWall(rat_pos, rat_v);
			rat_pos = new Point(next[0], next[1]);
			rat_v = new Point(next[3], next[4]);
			t += next[2];
			numReflections++;
		} while (numReflections < 5);
		
		return sol;
	}
	
	// Given a point and velocity, returns the point, time, and new heading at which it will hit a wall
	private double[] intersectWall(Point p, Point v, int dimension) {
		// Try it for each wall and return the one with the smallest positive time
		double t  = 999999;
		double tc;
		double sx = 1;
		double sy = 1;
		
		// Right wall: x = dimension
		tc = (dimension - p.x)/v.x;
		if (tc > 0 && tc < t) {
			t = tc;
			sx = -1;
			sy = 1;
		}
		
		// Top wall: y = 0
		tc = (0 - p.y)/v.y;
		if (tc > 0 && tc < t) {
			t = tc;
			sx = 1;
			sy = -1;
		}
		
		// Bottom wall: y = dimension
		tc = (dimension - p.y)/v.y;
		if (tc > 0 && tc < t) {
			t = tc;
			sx = 1;
			sy = -1;
		}
		
		// Left wall: x = 0
		tc = (0 - p.x)/v.x;
		if (tc > 0 && tc < t) {
			t = tc;
			sx = -1;
			sy = 1;
		}
		
		// Center wall: x = dimension/2 with a 2m hole
		tc = (dimension/2 - p.x)/v.x;
		if (tc > 0) {
			Point pc = new Point(p.x + v.x*tc, p.y +v.y*tc);
			if ((pc.y < dimension/2 - 1 || pc.y > dimension/2 + 1) && tc < t) {
				t = Math.min(tc, t);
				sx = -1;
				sy = 1;				
			}
		}
		
		double[] sol = {p.x + v.x*t, p.y + v.y*t, t, v.x*sx, v.y*sy};
		
		return sol;
	}
    */
}
