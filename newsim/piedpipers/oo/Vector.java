package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.Point;
import java.awt.Color;

public class Vector {
    public double x;
    public double y;

    public Vector() {
        this(0,0);
    }
    public Vector(Point p) {
        this(p.x, p.y); 
    }
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector sub(double x, double y) {
        return new Vector(this.x - x, this.y - y);
    }

    public Vector sub_ip(double dx, double dy) {
        this.x -= dx;
        this.y -= dy;
        return this;
    }

    public Vector sub(Vector v) {
        return this.sub(v.x, v.y);
    }
    public Vector sub_ip(Vector v) {
        return this.sub_ip(v.x, v.y);
    }

    public Vector add(double dx, double dy) {
        return new Vector(dx + this.x, dy + this.y);
    }
    public Vector add_ip(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }

    public Vector add(Vector v) {
        return this.add(v.x, v.y);
    }
    public Vector add_ip(Vector v) {
        return this.add_ip(v.x, v.y);
    }

    public double dot(Vector v) {
        return this.x * v.x + this.y * v.y;
    }

    public Vector scale(double factor) {
        return new Vector(x * factor, y * factor);
    }

    public Vector scale_ip(double factor) {
        this.x *= factor;
        this.y *= factor;
        return this;
    }

    public Vector unit() {
        return this.scale(1/this.length());
    }

    public Vector unit_ip() {
        return this.scale_ip(1/this.length());
    }

    public double length() {
        return Math.sqrt(x*x + y*y);
    }

    public double distanceTo(Vector b) {
        return this.sub(b).length();
    }

    public double angleWithOrigin() {
        return Math.atan2(this.y, this.x);
    }

    public boolean equals(Vector b) {
        return this.x == b.x && this.y == b.y;
    }

    public Point asPoint() {
        return new Point(this.x, this.y);
    }

    public static Vector fromPointsDifference(Point a, Point b) {
        return new Vector(b.x - a.x, b.y - a.y);
    }

    public String toString() {
        return String.format("(%f, %f)", this.x, this.y);
    }
}
