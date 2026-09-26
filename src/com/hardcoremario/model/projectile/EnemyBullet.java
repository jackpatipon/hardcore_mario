package com.hardcoremario.model.projectile;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.model.entity.LivingEntity;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Bullet fired by Apex Syndicate Guard soldiers towards the player.
 */
public class EnemyBullet extends Projectile {

    public EnemyBullet(double x, double y, double dirX, double dirY, double speed, int damage, LivingEntity shooter) {
        super(x, y, 14, 14, dirX, dirY, speed, damage, shooter);
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        com.hardcoremario.core.GameSettings.BulletColorTheme theme =
            com.hardcoremario.core.GameSettings.getInstance().getBulletColorTheme();

        // 1. High-visibility outer glowing halo (for accessibility and low vision)
        Color glow = new Color(theme.getMainColor().getRed(), theme.getMainColor().getGreen(), theme.getMainColor().getBlue(), 110);
        g.setColor(glow);
        g.fillOval(drawX - 3, drawY - 3, width + 6, height + 6);

        // 2. High-contrast dark edge rim (makes it pop against bright backgrounds)
        g.setColor(new Color(10, 10, 15, 200));
        g.fillOval(drawX - 1, drawY - 1, width + 2, height + 2);

        // 3. Main vivid bullet body
        g.setColor(theme.getMainColor());
        g.fillOval(drawX, drawY, width, height);

        // 4. Ultra-bright hot center core
        g.setColor(theme.getCoreColor());
        g.fillOval(drawX + 3, drawY + 3, width - 6, height - 6);
    }
}
