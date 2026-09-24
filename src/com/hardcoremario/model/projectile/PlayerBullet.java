package com.hardcoremario.model.projectile;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.model.entity.LivingEntity;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * High-velocity rifle bullet fired by the player Mario towards the mouse cursor.
 */
public class PlayerBullet extends Projectile {

    public PlayerBullet(double x, double y, double dirX, double dirY, double speed, int damage, LivingEntity shooter) {
        super(x, y, 14, 14, dirX, dirY, speed, damage, shooter);
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        BufferedImage img = AssetManager.getInstance().getImage("bullet_player");
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            // Fallback: glowing yellow-orange bullet
            g.setColor(new Color(255, 220, 50));
            g.fillOval(drawX, drawY, width, height);
            g.setColor(Color.WHITE);
            g.fillOval(drawX + 3, drawY + 3, width - 6, height - 6);
        }
    }
}
