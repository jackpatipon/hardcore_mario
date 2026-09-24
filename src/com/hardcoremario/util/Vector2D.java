package com.hardcoremario.util;

/**
 * 2D Vector class representing position, velocity, or direction.
 * Demonstrates encapsulation and mathematical operations.
 */
public class Vector2D {
    private double x;
    private double y;

    public Vector2D() {
        this(0, 0);
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public void add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D normalize() {
        double len = length();
        if (len > 0.0001) {
            return new Vector2D(x / len, y / len);
        }
        return new Vector2D(0, 0);
    }

    public double distanceTo(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceTo(double ox, double oy) {
        double dx = this.x - ox;
        double dy = this.y - oy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculates angle in degrees [0, 360) from this point to target (tx, ty)
     * 0° = East (Right), 90° = North (Up), 180° = West (Left), 270° = South (Down)
     */
    public double angleToDegrees(double tx, double ty) {
        double dx = tx - this.x;
        double dy = -(ty - this.y); // Invert y because screen y goes downwards
        double rad = Math.atan2(dy, dx);
        double deg = Math.toDegrees(rad);
        if (deg < 0) {
            deg += 360.0;
        }
        return deg;
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }
}
