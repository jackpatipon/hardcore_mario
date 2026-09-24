package com.hardcoremario.model.item;

import com.hardcoremario.model.entity.Entity;
import com.hardcoremario.model.entity.Player;

/**
 * Abstract class for collectable item pickups.
 */
public abstract class Item extends Entity {
    protected double bobTimer = 0.0;
    protected double initialY;

    public Item(double x, double y, int width, int height) {
        super(x, y, width, height);
        this.initialY = y;
    }

    @Override
    public void update(double deltaTime) {
        // Floating/bobbing animation
        bobTimer += deltaTime * 4.0;
        setY(initialY + Math.sin(bobTimer) * 4.0);
    }

    /**
     * Triggered when player touches and collects this item.
     */
    public abstract boolean onPickup(Player player);
}
