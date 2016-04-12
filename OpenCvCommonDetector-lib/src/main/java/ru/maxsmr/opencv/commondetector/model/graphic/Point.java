package ru.maxsmr.opencv.commondetector.model.graphic;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = -7133954493416044654L;

    public int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        this(0, 0);
    }

    public Point(int[] vals) {
        this();
        set(vals);
    }

    public void set(int[] vals) {
        if (vals != null) {
            x = vals.length > 0 ? vals[0] : 0;
            y = vals.length > 1 ? vals[1] : 0;
        } else {
            x = 0;
            y = 0;
        }
    }

    @Override
    public Point clone() {
        return new Point(x, y);
    }

    public double dot(Point p) {
        return x * p.x + y * p.y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    public boolean inside(Rect r) {
        if (r == null)
            return false;
        return (x >= r.x && x <= r.x + r.width) && (y >= r.y && y <= r.y + r.height);
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }

}
