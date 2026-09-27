package com.hardcoremario.view;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.GameSettings;
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
    // Victory Screen Layout Bounds
    public static final int VICTORY_CARD_W = 720;
    public static final int VICTORY_CARD_H = 436;
    public static final int VICTORY_CARD_X = (Constants.SCREEN_WIDTH - VICTORY_CARD_W) / 2;
    public static final int VICTORY_CARD_Y = 50;

    public static final int VICTORY_BTN_W = 400;
    public static final int VICTORY_BTN_H = 48;
    public static final int VICTORY_BTN_X = (Constants.SCREEN_WIDTH - VICTORY_BTN_W) / 2;
    public static final int VICTORY_BTN_Y = VICTORY_CARD_Y + 304;

    public static final Rectangle VICTORY_MENU_BTN_BOUNDS = new Rectangle(
        VICTORY_BTN_X, VICTORY_BTN_Y, VICTORY_BTN_W, VICTORY_BTN_H
    );

    private boolean showHelp = false;

    private static final String THAI_FONT = getThaiFont();

    private static String getThaiFont() {
        String[] candidates = {"Tahoma", "Leelawadee UI", "Microsoft Sans Serif"};
        for (String c : candidates) {
            Font f = new Font(c, Font.PLAIN, 12);
            if (f.canDisplay('ก') && f.canDisplay('ภ')) {
                return c;
            }
        }
        return Font.SANS_SERIF;
    }

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

        if (player.isDebugMode()) {
            g.setColor(new Color(0, 230, 255));
            g.fillRect(barStartX, hpY, hpBarW, hpBarH);
            g.setColor(Color.WHITE);
            g.drawRect(barStartX, hpY, hpBarW, hpBarH);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("HP: GOD MODE (อมตะ)", barStartX + 12, hpY + 16);
        } else {
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
        }

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
        if (player.isDebugMode()) {
            g.setColor(new Color(0, 245, 255));
            g.drawString("∞ / ∞  (กระสุนไม่จำกัด)", barStartX, ammoY + 16);
        } else if (player.isReloading()) {
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

        // Stage Indicator (Top Center Left)
        g.setColor(new Color(20, 20, 20, 210));
        g.fillRoundRect(Constants.SCREEN_WIDTH / 2 - 125, 14, 115, 32, 10, 10);
        g.setColor(new Color(0, 255, 255));
        g.drawRoundRect(Constants.SCREEN_WIDTH / 2 - 125, 14, 115, 32, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("STAGE " + level.getCurrentStage(), Constants.SCREEN_WIDTH / 2 - 100, 35);

        // Difficulty Badge (Top Center Right)
        com.hardcoremario.core.GameSettings.Difficulty diff = com.hardcoremario.core.GameSettings.getInstance().getDifficulty();
        g.setColor(new Color(20, 20, 20, 210));
        g.fillRoundRect(Constants.SCREEN_WIDTH / 2 + 5, 14, 120, 32, 10, 10);
        g.setColor(diff.getBadgeColor());
        g.drawRoundRect(Constants.SCREEN_WIDTH / 2 + 5, 14, 120, 32, 10, 10);
        g.setFont(new Font("Impact", Font.PLAIN, 15));
        g.drawString(diff.getCodeName(), Constants.SCREEN_WIDTH / 2 + 18, 35);

        // Debug Mode Banner (Top Center)
        if (player.isDebugMode()) {
            int dbgW = 340;
            int dbgH = 26;
            int dbgX = (Constants.SCREEN_WIDTH - dbgW) / 2;
            int dbgY = 52;
            g.setColor(new Color(8, 24, 38, 235));
            g.fillRoundRect(dbgX, dbgY, dbgW, dbgH, 8, 8);
            g.setColor(new Color(0, 255, 255, 210));
            g.drawRoundRect(dbgX, dbgY, dbgW, dbgH, 8, 8);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(new Color(0, 255, 255));
            String dbgText = "★ [F12] NOCLIP • GOD MODE • INF AMMO ★";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(dbgText, dbgX + (dbgW - fm.stringWidth(dbgText)) / 2, dbgY + 18);
        }

        // 4. Subtle Controls Hint (Bottom Left)
        g.setColor(new Color(255, 255, 255, 160));
        g.setFont(new Font(THAI_FONT, Font.PLAIN, 11));
        com.hardcoremario.core.GameSettings.BulletColorTheme theme = com.hardcoremario.core.GameSettings.getInstance().getBulletColorTheme();
        g.drawString("[W,A,S,D] Move/Fly | [Mouse] Shoot | [R] Reload | [C] Bullet | [F12] Debug | [ESC] Menu", 16, Constants.SCREEN_HEIGHT - 16);

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
            renderVictory(g, player, input);
        }

        // 8. Custom Crosshair at Mouse (hide during victory screen so regular cursor is visible)
        if (!level.isMissionComplete()) {
            renderCrosshair(g, input.getMouseX(), input.getMouseY());
        }
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
        String restart = "Press [R] to Restart Stage | [M] Main Menu";
        fm = g.getFontMetrics();
        g.drawString(restart, (Constants.SCREEN_WIDTH - fm.stringWidth(restart)) / 2, Constants.SCREEN_HEIGHT / 2 + 60);
    }

    private void renderVictory(Graphics2D g, Player player, InputHandler input) {
        // Semi-transparent dark backdrop overlay
        g.setColor(new Color(6, 10, 20, 225));
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        int cardX = VICTORY_CARD_X;
        int cardY = VICTORY_CARD_Y;
        int cardW = VICTORY_CARD_W;
        int cardH = VICTORY_CARD_H;

        // Glowing outer shadow
        g.setColor(new Color(255, 215, 0, 45));
        g.fillRoundRect(cardX - 4, cardY - 4, cardW + 8, cardH + 8, 24, 24);

        // Card background gradient (Dark cyber aesthetic)
        GradientPaint cardBg = new GradientPaint(
            cardX, cardY, new Color(18, 24, 38, 248),
            cardX, cardY + cardH, new Color(10, 14, 22, 252)
        );
        g.setPaint(cardBg);
        g.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        // Card borders: outer gold, inner emerald accent
        g.setStroke(new BasicStroke(2.2f));
        g.setColor(new Color(255, 215, 0, 210));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        g.setStroke(new BasicStroke(1.0f));
        g.setColor(new Color(46, 204, 113, 130));
        g.drawRoundRect(cardX + 3, cardY + 3, cardW - 6, cardH - 6, 17, 17);

        // Top Laurels / Stars
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String stars = "★ ★ ★   CONGRATULATIONS!   ★ ★ ★";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(stars, cardX + (cardW - fm.stringWidth(stars)) / 2, cardY + 28);

        // Main Title (with shadow)
        String title = "MISSION ACCOMPLISHED";
        g.setFont(new Font("Impact", Font.BOLD, 38));
        fm = g.getFontMetrics();
        int tx = cardX + (cardW - fm.stringWidth(title)) / 2;
        int ty = cardY + 66;

        g.setColor(new Color(0, 0, 0, 230));
        g.drawString(title, tx + 2, ty + 2);

        GradientPaint titleGrad = new GradientPaint(
            tx, ty - 28, new Color(255, 240, 120),
            tx, ty, new Color(255, 180, 20)
        );
        g.setPaint(titleGrad);
        g.drawString(title, tx, ty);

        // Subtitles
        g.setFont(new Font(THAI_FONT, Font.BOLD, 15));
        g.setColor(new Color(46, 204, 113));
        String subThai = "ภารกิจเสร็จสิ้น! คุณเคลียร์ทุกด่านและหลบหนีออกจาก Apex Facility สำเร็จ!";
        fm = g.getFontMetrics();
        g.drawString(subThai, cardX + (cardW - fm.stringWidth(subThai)) / 2, cardY + 92);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(175, 195, 215));
        String subEng = "All 3 Stages Cleared — Syndicate Perimeter Fully Breached!";
        fm = g.getFontMetrics();
        g.drawString(subEng, cardX + (cardW - fm.stringWidth(subEng)) / 2, cardY + 110);

        // Decorative Divider
        g.setPaint(new GradientPaint(
            cardX + 40, cardY + 124, new Color(255, 215, 0, 20),
            cardX + cardW / 2, cardY + 124, new Color(255, 215, 0, 190)
        ));
        g.drawLine(cardX + 40, cardY + 124, cardX + cardW / 2, cardY + 124);
        g.setPaint(new GradientPaint(
            cardX + cardW / 2, cardY + 124, new Color(255, 215, 0, 190),
            cardX + cardW - 40, cardY + 124, new Color(255, 215, 0, 20)
        ));
        g.drawLine(cardX + cardW / 2, cardY + 124, cardX + cardW - 40, cardY + 124);

        // 4 Stat Cards in 2x2 Grid
        int cardLeftX = cardX + 40;
        int cardRightX = cardX + 375;
        int cardRow1Y = cardY + 138;
        int cardRow2Y = cardY + 218;
        int cardBoxW = 305;
        int cardBoxH = 70;

        // Card 1: Clear Status
        renderStatCard(g, cardLeftX, cardRow1Y, cardBoxW, cardBoxH,
            "STATUS", "3 / 3 STAGES CLEARED", "100% Complete (จบครบทุกด่าน)", new Color(255, 215, 0));

        // Card 2: Eliminations
        renderStatCard(g, cardRightX, cardRow1Y, cardBoxW, cardBoxH,
            "ELIMINATIONS", player.getKills() + " GUARDS DEFEATED", "Total Hostiles Neutralized (ศัตรูที่กำจัด)", new Color(255, 80, 80));

        // Card 3: Difficulty
        GameSettings.Difficulty diff = GameSettings.getInstance().getDifficulty();
        renderStatCard(g, cardLeftX, cardRow2Y, cardBoxW, cardBoxH,
            "DIFFICULTY", diff.getCodeName(), diff.getLabelThai(), diff.getBadgeColor());

        // Card 4: Survival Integrity
        int hpPercent = (int) Math.max(0, Math.round((double) player.getHp() / player.getMaxHp() * 100));
        renderStatCard(g, cardRightX, cardRow2Y, cardBoxW, cardBoxH,
            "SURVIVAL INTEGRITY", player.getHp() + " / " + player.getMaxHp() + " HP", "Health Remaining (" + hpPercent + "%)", new Color(46, 204, 113));

        // Return to Main Menu Interactive Button
        Rectangle btnRect = VICTORY_MENU_BTN_BOUNDS;
        boolean hovered = btnRect.contains(input.getMouseX(), input.getMouseY());

        if (hovered) {
            g.setColor(new Color(0, 240, 255, 110));
            g.fillRoundRect(btnRect.x - 4, btnRect.y - 4, btnRect.width + 8, btnRect.height + 8, 16, 16);

            GradientPaint btnGrad = new GradientPaint(
                btnRect.x, btnRect.y, new Color(0, 230, 180),
                btnRect.x + btnRect.width, btnRect.y + btnRect.height, new Color(0, 160, 245)
            );
            g.setPaint(btnGrad);
        } else {
            GradientPaint btnGrad = new GradientPaint(
                btnRect.x, btnRect.y, new Color(24, 70, 56, 240),
                btnRect.x + btnRect.width, btnRect.y + btnRect.height, new Color(16, 48, 38, 250)
            );
            g.setPaint(btnGrad);
        }
        g.fillRoundRect(btnRect.x, btnRect.y, btnRect.width, btnRect.height, 12, 12);

        g.setStroke(new BasicStroke(hovered ? 2.0f : 1.5f));
        g.setColor(hovered ? Color.WHITE : new Color(46, 204, 113, 200));
        g.drawRoundRect(btnRect.x, btnRect.y, btnRect.width, btnRect.height, 12, 12);

        g.setFont(new Font(THAI_FONT, Font.BOLD, 15));
        String btnText = "⮌ กลับสู่หน้าหลัก (RETURN TO MAIN MENU)";
        fm = g.getFontMetrics();
        int bx = btnRect.x + (btnRect.width - fm.stringWidth(btnText)) / 2;
        int by = btnRect.y + ((btnRect.height - fm.getHeight()) / 2) + fm.getAscent();

        if (hovered) {
            g.setColor(new Color(10, 20, 30));
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawString(btnText, bx, by);

        // Hint text below button
        g.setFont(new Font(THAI_FONT, Font.PLAIN, 12));
        g.setColor(new Color(180, 195, 210));
        String hintText = "กดปุ่ม [SPACE], [ENTER], [R], [M] หรือคลิกที่ปุ่มเพื่อกลับสู่หน้าหลัก";
        fm = g.getFontMetrics();
        g.drawString(hintText, cardX + (cardW - fm.stringWidth(hintText)) / 2, cardY + 372);
    }

    private void renderStatCard(Graphics2D g, int x, int y, int w, int h, String tag, String title, String subtitle, Color accent) {
        g.setColor(new Color(14, 18, 28, 220));
        g.fillRoundRect(x, y, w, h, 12, 12);

        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        g.drawRoundRect(x, y, w, h, 12, 12);

        // Accent indicator bar on left edge
        g.setColor(accent);
        g.fillRoundRect(x + 3, y + 4, 4, h - 8, 4, 4);

        // Tag / Category
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(accent);
        g.drawString(tag.toUpperCase(), x + 16, y + 18);

        // Value / Title
        g.setFont(new Font("Impact", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString(title, x + 16, y + 40);

        // Subtitle
        g.setFont(new Font(THAI_FONT, Font.PLAIN, 11));
        g.setColor(new Color(180, 195, 210));
        g.drawString(subtitle, x + 16, y + 58);
    }

    private void renderHelpOverlay(Graphics2D g) {
        int w = 530;
        int h = 305;
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
        g.drawString("• A / D : Walk Left / Right (เหาะซ้าย-ขวาเมื่อเปิด noclip)", x + 30, y + 75);
        g.drawString("• W or Space : Jump (หรือบินขึ้นเมื่อเปิด noclip)", x + 30, y + 102);
        g.drawString("• S : Crouch (หรือบินลงเมื่อเปิด noclip)", x + 30, y + 129);
        g.drawString("• Mouse Cursor : 360-degree aiming", x + 30, y + 156);
        g.drawString("• Left Click : Shoot assault rifle", x + 30, y + 183);
        g.drawString("• R : Reload rifle", x + 30, y + 210);
        g.drawString("• F12 : Debug Mode (Noclip เหาะทะลุฉาก / อมตะ / กระสุนไม่จำกัด)", x + 30, y + 237);
        g.drawString("• Press [H] to close this help window", x + 30, y + 268);
    }

    public void toggleHelp() {
        this.showHelp = !this.showHelp;
    }
}
