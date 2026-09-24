package com.hardcoremario.view;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.entity.Player;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Head-Up Display (HUD) rendering player stats, ammo, health, crosshair, and UI overlays.
 */
public class HUD {
    private boolean showHelp = false;

    public void render(Graphics2D g, Level level, InputHandler input) {
        Player player = level.getPlayer();
        AssetManager am = AssetManager.getInstance();

        // 1. Health Bar (Top Left)
        int hpX = 20;
        int hpY = 20;
        int hpBarW = 200;
        int hpBarH = 22;

        // Background shadow
        g.setColor(new Color(20, 20, 20, 210));
        g.fillRoundRect(hpX - 8, hpY - 6, hpBarW + 64, hpBarH + 12, 10, 10);
        g.setColor(new Color(80, 80, 80));
        g.drawRoundRect(hpX - 8, hpY - 6, hpBarW + 64, hpBarH + 12, 10, 10);

        // Heart Icon
        BufferedImage heart = am.getImage("icon_health");
        if (heart != null) {
            g.drawImage(heart, hpX - 2, hpY - 1, 24, 24, null);
        }

        // HP Bar Fill
        int barStartX = hpX + 28;
        g.setColor(new Color(60, 20, 20));
        g.fillRect(barStartX, hpY, hpBarW, hpBarH);

        double hpRatio = Math.max(0, (double) player.getHp() / player.getMaxHp());
        Color hpColor = hpRatio > 0.5 ? new Color(46, 204, 113) : (hpRatio > 0.25 ? new Color(241, 196, 15) : new Color(231, 76, 60));
        g.setColor(hpColor);
        g.fillRect(barStartX, hpY, (int) (hpBarW * hpRatio), hpBarH);

        // Border & HP Text
        g.setColor(Color.WHITE);
        g.drawRect(barStartX, hpY, hpBarW, hpBarH);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String hpText = "HP: " + player.getHp() + " / " + player.getMaxHp();
        g.drawString(hpText, barStartX + 12, hpY + 16);

        // 2. Ammo Indicator (Below HP Bar)
        int ammoY = hpY + 36;
        g.setColor(new Color(20, 20, 20, 210));
        g.fillRoundRect(hpX - 8, ammoY - 6, 180, hpBarH + 12, 10, 10);
        g.setColor(new Color(80, 80, 80));
        g.drawRoundRect(hpX - 8, ammoY - 6, 180, hpBarH + 12, 10, 10);

        BufferedImage ammoIcon = am.getImage("icon_ammo");
        if (ammoIcon != null) {
            g.drawImage(ammoIcon, hpX - 2, ammoY - 1, 24, 24, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        if (player.isReloading()) {
            g.setColor(new Color(255, 180, 0));
            g.drawString("RELOADING...", barStartX, ammoY + 16);
        } else {
            g.drawString(player.getCurrentAmmo() + " / " + player.getReserveAmmo() + "  [R] RELOAD", barStartX, ammoY + 16);
        }

        // 3. Kills Counter (Top Right)
        int killX = Constants.SCREEN_WIDTH - 150;
        int killY = 20;
        g.setColor(new Color(20, 20, 20, 210));
        g.fillRoundRect(killX - 8, killY - 6, 138, 34, 10, 10);
        g.setColor(new Color(80, 80, 80));
        g.drawRoundRect(killX - 8, killY - 6, 138, 34, 10, 10);
        g.setColor(new Color(255, 100, 100));
        g.drawString("KILLS: " + player.getKills(), killX + 12, killY + 18);

        // 4. Subtle Controls Hint (Bottom Left)
        g.setColor(new Color(255, 255, 255, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("[W,A,S,D] Move/Jump/Crouch | [Mouse] Aim | [L-Click] Shoot | [H] Help", 16, Constants.SCREEN_HEIGHT - 16);

        // 5. Help Overlay (if toggled)
        if (showHelp) {
            renderHelpOverlay(g);
        }

        // 6. Game Over Screen
        if (level.isGameOver()) {
            renderGameOver(g, player);
        }

        // 7. Victory Screen
        if (level.isMissionComplete()) {
            renderVictory(g, player);
        }

        // 8. Custom Crosshair at Mouse
        renderCrosshair(g, input.getMouseX(), input.getMouseY());
    }

    private void renderCrosshair(Graphics2D g, int mx, int my) {
        BufferedImage ch = AssetManager.getInstance().getImage("crosshair");
        if (ch != null) {
            g.drawImage(ch, mx - 16, my - 16, 32, 32, null);
        } else {
            g.setColor(Color.GREEN);
            g.drawOval(mx - 8, my - 8, 16, 16);
            g.drawLine(mx, my - 12, mx, my + 12);
            g.drawLine(mx - 12, my, mx + 12, my);
        }
    }

    private void renderGameOver(Graphics2D g, Player player) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        g.setColor(new Color(230, 40, 40));
        g.setFont(new Font("Arial", Font.BOLD, 52));
        String title = "YOU DIED";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (Constants.SCREEN_WIDTH - fm.stringWidth(title)) / 2, Constants.SCREEN_HEIGHT / 2 - 40);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String desc = "Apex Syndicate terminated Test Subject #6504062636187";
        fm = g.getFontMetrics();
        g.drawString(desc, (Constants.SCREEN_WIDTH - fm.stringWidth(desc)) / 2, Constants.SCREEN_HEIGHT / 2 + 10);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String restart = "Press [R] or Click to Restart";
        fm = g.getFontMetrics();
        g.drawString(restart, (Constants.SCREEN_WIDTH - fm.stringWidth(restart)) / 2, Constants.SCREEN_HEIGHT / 2 + 60);
    }

    private void renderVictory(Graphics2D g, Player player) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        g.setColor(new Color(46, 204, 113));
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "FACILITY ESCAPED!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (Constants.SCREEN_WIDTH - fm.stringWidth(title)) / 2, Constants.SCREEN_HEIGHT / 2 - 40);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String desc = "Mario successfully broke through the Apex Syndicate defenses! Kills: " + player.getKills();
        fm = g.getFontMetrics();
        g.drawString(desc, (Constants.SCREEN_WIDTH - fm.stringWidth(desc)) / 2, Constants.SCREEN_HEIGHT / 2 + 10);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String restart = "Press [R] to Play Again";
        fm = g.getFontMetrics();
        g.drawString(restart, (Constants.SCREEN_WIDTH - fm.stringWidth(restart)) / 2, Constants.SCREEN_HEIGHT / 2 + 60);
    }

    private void renderHelpOverlay(Graphics2D g) {
        int w = 500;
        int h = 280;
        int x = (Constants.SCREEN_WIDTH - w) / 2;
        int y = (Constants.SCREEN_HEIGHT - h) / 2;

        g.setColor(new Color(15, 15, 25, 235));
        g.fillRoundRect(x, y, w, h, 16, 16);
        g.setColor(new Color(0, 255, 255));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w, h, 16, 16);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("HOW TO PLAY (Hardcore Mario)", x + 24, y + 40);

        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("• A / D : Walk Left / Right", x + 30, y + 80);
        g.drawString("• W or Space : Jump over blocks and pits", x + 30, y + 110);
        g.drawString("• S : Crouch / Crawl (lowers hitbox to dodge bullets)", x + 30, y + 140);
        g.drawString("• Mouse Cursor : 360-degree aiming", x + 30, y + 170);
        g.drawString("• Left Click : Shoot assault rifle", x + 30, y + 200);
        g.drawString("• R : Reload rifle", x + 30, y + 230);
        g.drawString("• Press [H] to close this help window", x + 30, y + 260);
    }

    public void toggleHelp() {
        this.showHelp = !this.showHelp;
    }
}
