package com.hardcoremario.model.item;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.model.entity.Player;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Health Pack item that restores HP when collected.
 */
public class HealthPack extends Item {
    public static final int DEFAULT_SIZE = 36;
    private final int healAmount;

    public HealthPack(double x, double y) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
        this.healAmount = 30;
    }

    public HealthPack(double x, double y, int width, int height) {
        super(x, y, width, height);
        this.healAmount = 30;
    }

    public HealthPack(double x, double y, int healAmount, int width, int height) {
        super(x, y, width, height);
        this.healAmount = healAmount;
    }

    public int getHealAmount() {
        return healAmount;
    }

    @Override
    public boolean onPickup(Player player) {
        if (player.getHp() < player.getMaxHp()) {
            player.heal(healAmount);
            SoundManager.getInstance().playPickup();
            this.active = false;
            return true;
        }
        return false; // Already full health
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        // Gentle synchronized floating bob animation for all health packs (both mid-air and on blocks)
        drawY += (int) (Math.sin(bobTimer) * 3);

        // Pulsating green medical glow behind health pack for high visibility
        int glowAlpha = 70 + (int) (Math.sin(bobTimer * 2.5) * 35);
        g.setColor(new Color(46, 204, 113, Math.max(25, Math.min(125, glowAlpha))));
        g.fillOval(drawX - 4, drawY - 4, width + 8, height + 8);

        BufferedImage img = AssetManager.getInstance().getImage("health_pack");
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRoundRect(drawX, drawY, width, height, Math.min(8, width / 3), Math.min(8, height / 3));
            g.setColor(new Color(230, 40, 40));
            int crossThick = Math.max(3, width / 5);
            g.fillRect(drawX + (width - crossThick) / 2, drawY + 4, crossThick, height - 8);
            g.fillRect(drawX + 4, drawY + (height - crossThick) / 2, width - 8, crossThick);
        }
    }
}
