package com.hardcoremario.model.item;

import com.hardcoremario.model.entity.Entity;
import com.hardcoremario.model.entity.Player;
import com.hardcoremario.model.world.Tile;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.ParticleSystem;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Abstract class for collectable item pickups.
 * Responds to gravity physics and lands on solid blocks.
 * Disappears immediately if it contacts spikes.
 */
public abstract class Item extends Entity {
    protected double bobTimer = 0.0;
    protected boolean grounded = false;

    public Item(double x, double y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void update(double deltaTime) {
        // Apply gravity
        velocity.setY(Math.min(velocity.getY() + Constants.GRAVITY * deltaTime, Constants.MAX_FALL_SPEED));

        // Subtly bob only when grounded
        if (grounded) {
            bobTimer += deltaTime * 3.5;
        }
    }

    public void updatePhysicsAndCollisions(List<Tile> tiles, double deltaTime) {
        // Move Y
        position.setY(position.getY() + velocity.getY() * deltaTime);
        grounded = false;

        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : tiles) {
            if (!tile.isActive()) continue;

            // 1. Spikes touch item -> item is destroyed immediately!
            if (tile.isHazard() && hb.intersects(tile.getHitbox())) {
                this.active = false;
                ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.GRAY);
                return;
            }

            // 2. Solid tile collision (landing on block)
            if (tile.isSolid() && hb.intersects(tile.getHitbox())) {
                if (velocity.getY() > 0) {
                    position.setY(tile.getY() - height);
                    velocity.setY(0);
                    grounded = true;
                }
                hb = getHitbox();
            }
        }
    }

    /**
     * Triggered when player touches and collects this item.
     */
    public abstract boolean onPickup(Player player);
}
