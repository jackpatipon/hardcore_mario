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
    private final int healAmount;

    public HealthPack(double x, double y) {
        super(x, y, 24, 24);
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

        // Soft green glow behind health pack
        g.setColor(new Color(50, 255, 100, 60));
        g.fillOval(drawX - 2, drawY - 2, width + 4, height + 4);

        BufferedImage img = AssetManager.getInstance().getImage("health_pack");
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRoundRect(drawX, drawY, width, height, Math.min(6, width / 3), Math.min(6, height / 3));
            g.setColor(new Color(230, 40, 40));
            int crossThick = Math.max(2, width / 5);
            g.fillRect(drawX + (width - crossThick) / 2, drawY + 2, crossThick, height - 4);
            g.fillRect(drawX + 2, drawY + (height - crossThick) / 2, width - 4, crossThick);
        }
    }
}
