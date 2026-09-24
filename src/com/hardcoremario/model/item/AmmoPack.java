package com.hardcoremario.model.item;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.model.entity.Player;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Ammo Pack item that adds reserve bullets when collected.
 */
public class AmmoPack extends Item {
    private final int ammoAmount;

    public AmmoPack(double x, double y) {
        super(x, y, 32, 32);
        this.ammoAmount = 30;
    }

    public AmmoPack(double x, double y, int ammoAmount) {
        super(x, y, 32, 32);
        this.ammoAmount = ammoAmount;
    }

    public int getAmmoAmount() {
        return ammoAmount;
    }

    @Override
    public boolean onPickup(Player player) {
        player.addReserveAmmo(ammoAmount);
        SoundManager.getInstance().playPickup();
        this.active = false;
        return true;
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        // Soft yellow glow behind ammo box
        g.setColor(new Color(255, 215, 0, 60));
        g.fillOval(drawX - 4, drawY - 4, width + 8, height + 8);

        BufferedImage img = AssetManager.getInstance().getImage("ammo_box");
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            g.setColor(new Color(60, 90, 45));
            g.fillRoundRect(drawX, drawY, width, height, 4, 4);
            g.setColor(Color.YELLOW);
            g.drawString("AMMO", drawX + 3, drawY + 20);
        }
    }
}
