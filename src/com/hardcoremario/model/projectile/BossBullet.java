package com.hardcoremario.model.projectile;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.model.entity.LivingEntity;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Destructive high-energy projectile fired by the Boss.
 * Penetrates all solid tiles/blocks and damages the player.
 */
public class BossBullet extends EnemyBullet {

    private double animTimer = 0.0;

    public BossBullet(double x, double y, double dirX, double dirY, double speed, int damage, LivingEntity shooter) {
        super(x, y, dirX, dirY, speed, damage, shooter);
        this.width = 20;
        this.height = 20;
        // Infinite range: travels across the entire map without despawning until out of bounds
        this.lifeTime = 999.0;
    }

    /**
     * Flag indicating this bullet ignores and penetrates all environmental blocks.
     */
    public boolean canPenetrateTiles() {
        return true;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        animTimer += deltaTime * 12.0;
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        BufferedImage img = AssetManager.getInstance().getImage("bullet_boss");
        if (img != null) {
            // Draw sprite centered
            g.drawImage(img, drawX - 6, drawY - 6, width + 12, height + 12, null);
        } else {
            // High-visibility glowing plasma projectile
            int pulse = (int) (Math.sin(animTimer) * 3);

            // Outer corona glow
            g.setColor(new Color(220, 20, 255, 75));
            g.fillOval(drawX - 5 - pulse, drawY - 5 - pulse, width + 10 + pulse * 2, height + 10 + pulse * 2);

            // Fiery violet ring
            g.setColor(new Color(255, 0, 128, 200));
            g.fillOval(drawX - 2, drawY - 2, width + 4, height + 4);

            // Bright core
            g.setColor(new Color(255, 140, 240));
            g.fillOval(drawX + 2, drawY + 2, width - 4, height - 4);

            // White hot center
            g.setColor(Color.WHITE);
            g.fillOval(drawX + 5, drawY + 5, width - 10, height - 10);
        }
    }
}
