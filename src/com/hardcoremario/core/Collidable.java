package com.hardcoremario.core;

import java.awt.geom.Rectangle2D;

/**
 * Interface for game objects that have physical hitboxes and can collide.
 */
public interface Collidable {
    Rectangle2D.Double getHitbox();
    boolean collidesWith(Collidable other);
}
