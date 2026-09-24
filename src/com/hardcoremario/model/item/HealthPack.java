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
        super(x, y, 32, 32);
        this.healAmount = 30;
    }

    public HealthPack(double x, double y, int healAmount) {
        super(x, y, 32, 32);
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
        g.fillOval(drawX - 4, drawY - 4, width + 8, height + 8);

        BufferedImage img = AssetManager.getInstance().getImage("health_pack");
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRoundRect(drawX, drawY, width, height, 6, 6);
            g.setColor(Color.RED);
            g.fillRect(drawX + width / 2 - 2, drawY + 4, 4, height - 8);
            g.fillRect(drawX + 4, drawY + height / 2 - 2, width - 8, 4);
        }
    }
}
