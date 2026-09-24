package com.hardcoremario.model.entity;

import com.hardcoremario.core.Collidable;
import com.hardcoremario.core.Renderable;
import com.hardcoremario.core.Updatable;
import com.hardcoremario.util.Vector2D;
import java.awt.geom.Rectangle2D;

/**
 * Abstract Base Class for all game objects in the world.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Entity implements Updatable, Renderable, Collidable {
    protected Vector2D position;
    protected Vector2D velocity;
    protected int width;
    protected int height;
    protected boolean active = true;

    public Entity(double x, double y, int width, int height) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.width = width;
        this.height = height;
    }

    public double getX() { return position.getX(); }
    public void setX(double x) { position.setX(x); }

    public double getY() { return position.getY(); }
    public void setY(double y) { position.setY(y); }

    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public double getCenterX() { return position.getX() + width / 2.0; }
    public double getCenterY() { return position.getY() + height / 2.0; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public Rectangle2D.Double getHitbox() {
        return new Rectangle2D.Double(position.getX(), position.getY(), width, height);
    }

    @Override
    public boolean collidesWith(Collidable other) {
        if (other == null) return false;
        return getHitbox().intersects(other.getHitbox());
    }
}
