package tools;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GenerateAssetTemplates {

    public static void main(String[] args) {
        String baseDir = "assets";
        System.out.println("Generating asset folders and placeholder templates in: " + baseDir);

        // 1. Player 8 directions (64x64)
        String[] dirs = {"e", "ne", "n", "nw", "w", "sw", "s", "se"};
        double[] angles = {0, 45, 90, 135, 180, 225, 270, 315}; // In degrees (standard math, E is 0, N is 90)
        String playerDir = baseDir + "/characters/player/8directions";
        new File(playerDir).mkdirs();

        for (int i = 0; i < dirs.length; i++) {
            BufferedImage img = createDirectionalCharacter(64, 64, angles[i], dirs[i].toUpperCase(), new Color(220, 50, 50), new Color(40, 140, 40));
            saveImage(img, playerDir + "/aim_" + dirs[i] + ".png");
        }

        // Player states
        String playerBase = baseDir + "/characters/player";
        saveImage(createCharacterState(64, 64, "IDLE", new Color(220, 50, 50), false), playerBase + "/idle.png");
        saveImage(createCharacterState(64, 64, "JUMP", new Color(220, 50, 50), false), playerBase + "/jump.png");
        saveImage(createCharacterState(64, 40, "CROUCH", new Color(220, 50, 50), true), playerBase + "/crouch.png");

        // 2. Guard / Enemy (64x64)
        String guardDir = baseDir + "/characters/guard/8directions";
        new File(guardDir).mkdirs();
        for (int i = 0; i < dirs.length; i++) {
            BufferedImage img = createDirectionalCharacter(64, 64, angles[i], dirs[i].toUpperCase(), new Color(60, 60, 75), new Color(180, 30, 30));
            saveImage(img, guardDir + "/aim_" + dirs[i] + ".png");
        }
        String guardBase = baseDir + "/characters/guard";
        saveImage(createCharacterState(64, 64, "GUARD R", new Color(60, 60, 75), false), guardBase + "/idle_right.png");
        saveImage(createCharacterState(64, 64, "GUARD L", new Color(60, 60, 75), false), guardBase + "/idle_left.png");

        // 3. Projectiles (24x24)
        String projDir = baseDir + "/projectiles";
        new File(projDir).mkdirs();
        saveImage(createBullet(24, 24, new Color(255, 215, 0), new Color(255, 100, 0)), projDir + "/bullet_player.png");
        saveImage(createBullet(24, 24, new Color(255, 50, 50), new Color(180, 0, 0)), projDir + "/bullet_enemy.png");

        // 4. Items (32x32)
        String itemDir = baseDir + "/items";
        new File(itemDir).mkdirs();
        saveImage(createHealthPack(32, 32), itemDir + "/health_pack.png");
        saveImage(createAmmoBox(32, 32), itemDir + "/ammo_box.png");

        // 5. Environment & Blocks (48x48)
        String envDir = baseDir + "/environment";
        new File(envDir).mkdirs();
        saveImage(createPlatformBlock(48, 48, new Color(218, 165, 32), "PLATFORM"), envDir + "/block_platform.png");
        saveImage(createPlatformBlock(48, 48, new Color(100, 100, 105), "GROUND"), envDir + "/block_ground.png");
        saveImage(createPlatformBlock(48, 48, new Color(70, 85, 100), "STEEL"), envDir + "/block_metal.png");
        saveImage(createBarrel(48, 48), envDir + "/barrel.png");
        saveImage(createSpikes(48, 24), envDir + "/hazard_spikes.png");

        // 6. Backgrounds (1280x720)
        String bgDir = baseDir + "/backgrounds";
        new File(bgDir).mkdirs();
        saveImage(createBackground(1280, 720), bgDir + "/bg_main.png");
        saveImage(createBackgroundFar(1280, 720), bgDir + "/bg_far.png");

        // 7. UI / Crosshair
        String uiDir = baseDir + "/ui";
        new File(uiDir).mkdirs();
        saveImage(createCrosshair(32, 32), uiDir + "/crosshair.png");
        saveImage(createHeartIcon(24, 24), uiDir + "/icon_health.png");
        saveImage(createAmmoIcon(24, 24), uiDir + "/icon_ammo.png");

        System.out.println("All asset templates successfully created!");
    }

    private static void saveImage(BufferedImage img, String path) {
        try {
            File f = new File(path);
            f.getParentFile().mkdirs();
            ImageIO.write(img, "PNG", f);
            System.out.println("Saved: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage createDirectionalCharacter(int w, int h, double deg, String label, Color capColor, Color vestColor) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Body base
        int cx = w / 2;
        int cy = h / 2 + 6;

        // Legs
        g.setColor(new Color(40, 50, 60));
        g.fillRect(cx - 10, cy + 8, 8, 14);
        g.fillRect(cx + 2, cy + 8, 8, 14);

        // Torso / Vest
        g.setColor(vestColor);
        g.fillRoundRect(cx - 12, cy - 10, 24, 20, 6, 6);

        // Head
        g.setColor(new Color(240, 190, 150));
        g.fillOval(cx - 9, cy - 25, 18, 18);

        // Cap / Bandana
        g.setColor(capColor);
        g.fillRect(cx - 11, cy - 26, 22, 9);
        g.fillOval(cx - 10, cy - 28, 20, 10);

        // Gun pointing in direction deg
        double rad = Math.toRadians(deg);
        // Note: Java screen coordinates y goes down, while standard math y goes up.
        // For screen math: dx = cos(rad), dy = -sin(rad)
        double dx = Math.cos(rad);
        double dy = -Math.sin(rad);

        int gunLen = 22;
        int gx2 = (int) (cx + dx * gunLen);
        int gy2 = (int) ((cy - 2) + dy * gunLen);

        g.setColor(new Color(20, 20, 20));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx, cy - 2, gx2, gy2);

        // Muzzle indicator
        g.setColor(Color.ORANGE);
        g.fillOval(gx2 - 3, gy2 - 3, 6, 6);

        // Label box
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(2, 2, 26, 14);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(label, 4, 13);

        g.dispose();
        return img;
    }

    private static BufferedImage createCharacterState(int w, int h, String text, Color capColor, boolean crouch) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = w / 2;
        int cy = h / 2 + (crouch ? 2 : 4);

        if (!crouch) {
            // Legs
            g.setColor(new Color(40, 50, 60));
            g.fillRect(cx - 10, cy + 8, 8, 14);
            g.fillRect(cx + 2, cy + 8, 8, 14);
        } else {
            // Crouched legs
            g.setColor(new Color(40, 50, 60));
            g.fillRect(cx - 14, cy + 4, 28, 10);
        }

        // Torso
        g.setColor(new Color(40, 140, 40));
        g.fillRoundRect(cx - 12, cy - 10, 24, crouch ? 16 : 20, 6, 6);

        // Head
        g.setColor(new Color(240, 190, 150));
        g.fillOval(cx - 9, cy - (crouch ? 20 : 25), 18, 18);

        // Cap
        g.setColor(capColor);
        g.fillRect(cx - 11, cy - (crouch ? 21 : 26), 22, 8);

        // Label
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(2, 2, w - 4, 14);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(text, 6, 13);

        g.dispose();
        return img;
    }

    private static BufferedImage createBullet(int w, int h, Color inner, Color outer) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(outer);
        g.fillOval(2, h / 2 - 5, w - 4, 10);

        g.setColor(inner);
        g.fillOval(4, h / 2 - 3, w - 8, 6);

        g.dispose();
        return img;
    }

    private static BufferedImage createHealthPack(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // White kit box with red border
        g.setColor(new Color(240, 240, 240));
        g.fillRoundRect(3, 5, w - 6, h - 8, 6, 6);
        g.setColor(new Color(180, 20, 20));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(3, 5, w - 6, h - 8, 6, 6);

        // Handle
        g.setColor(new Color(90, 90, 90));
        g.drawArc(w / 2 - 5, 2, 10, 6, 0, 180);

        // Red cross
        g.setColor(new Color(220, 30, 30));
        int cx = w / 2;
        int cy = h / 2 + 1;
        g.fillRect(cx - 3, cy - 8, 6, 16);
        g.fillRect(cx - 8, cy - 3, 16, 6);

        g.dispose();
        return img;
    }

    private static BufferedImage createAmmoBox(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Military green ammo crate
        g.setColor(new Color(60, 90, 45));
        g.fillRoundRect(3, 6, w - 6, h - 10, 4, 4);

        g.setColor(new Color(35, 55, 25));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(3, 6, w - 6, h - 10, 4, 4);

        // Gold bullet markings
        g.setColor(new Color(255, 215, 0));
        g.fillRect(w / 2 - 7, h / 2 - 2, 3, 8);
        g.fillRect(w / 2 - 2, h / 2 - 2, 3, 8);
        g.fillRect(w / 2 + 3, h / 2 - 2, 3, 8);

        // Stencil text "AMMO"
        g.setFont(new Font("Arial", Font.BOLD, 7));
        g.drawString("AMMO", 4, 13);

        g.dispose();
        return img;
    }

    private static BufferedImage createPlatformBlock(int w, int h, Color mainColor, String label) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Base block
        g.setColor(mainColor);
        g.fillRect(0, 0, w, h);

        // Bevel highlight and shadow
        g.setColor(new Color(255, 255, 255, 90));
        g.fillRect(0, 0, w, 4);
        g.fillRect(0, 0, 4, h);

        g.setColor(new Color(0, 0, 0, 90));
        g.fillRect(0, h - 4, w, 4);
        g.fillRect(w - 4, 0, 4, h);

        // Texture studs or rivets
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(6, 6, 5, 5);
        g.fillOval(w - 11, 6, 5, 5);
        g.fillOval(6, h - 11, 5, 5);
        g.fillOval(w - 11, h - 11, 5, 5);

        // Label
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.setColor(new Color(255, 255, 255, 180));
        FontMetrics fm = g.getFontMetrics();
        int tx = (w - fm.stringWidth(label)) / 2;
        int ty = (h + fm.getAscent()) / 2 - 2;
        g.drawString(label, tx, ty);

        g.dispose();
        return img;
    }

    private static BufferedImage createBarrel(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Rusty oil/explosive barrel
        g.setColor(new Color(180, 50, 40));
        g.fillRoundRect(8, 4, w - 16, h - 8, 8, 8);

        // Metal ribs
        g.setColor(new Color(40, 40, 40));
        g.drawRoundRect(8, 4, w - 16, h - 8, 8, 8);
        g.drawLine(8, h / 3, w - 8, h / 3);
        g.drawLine(8, 2 * h / 3, w - 8, 2 * h / 3);

        // Hazard symbol
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("!", w / 2 - 2, h / 2 + 4);

        g.dispose();
        return img;
    }

    private static BufferedImage createSpikes(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(170, 180, 190));
        int numSpikes = 4;
        int spikeW = w / numSpikes;

        for (int i = 0; i < numSpikes; i++) {
            int sx = i * spikeW;
            int[] xs = {sx, sx + spikeW / 2, sx + spikeW};
            int[] ys = {h, 2, h};
            g.fillPolygon(xs, ys, 3);
            g.setColor(new Color(80, 85, 90));
            g.drawPolygon(xs, ys, 3);
            g.setColor(new Color(170, 180, 190));
        }

        g.dispose();
        return img;
    }

    private static BufferedImage createBackground(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Dark sci-fi facility / cyberpunk ruins gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(25, 20, 35), 0, h, new Color(75, 45, 30));
        g.setPaint(gp);
        g.fillRect(0, 0, w, h);

        // Distant ruined industrial silhouettes
        g.setColor(new Color(45, 30, 40));
        for (int i = 0; i < w; i += 90) {
            int bh = 150 + (int) (Math.sin(i) * 60 + 50);
            g.fillRect(i, h - bh - 80, 80, bh);
        }

        // Glowing red / neon cyberpunk details
        g.setColor(new Color(255, 60, 60, 120));
        g.fillRect(100, 300, 200, 10);
        g.fillRect(500, 260, 150, 8);
        g.fillRect(900, 320, 250, 12);

        // Apex Syndicate warning text in background
        g.setColor(new Color(255, 255, 255, 40));
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        g.drawString("APEX SYNDICATE - RESEARCH FACILITY 07", 80, 120);

        g.dispose();
        return img;
    }

    private static BufferedImage createBackgroundFar(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Distant smoggy sky
        GradientPaint gp = new GradientPaint(0, 0, new Color(15, 10, 25), 0, h, new Color(50, 25, 20));
        g.setPaint(gp);
        g.fillRect(0, 0, w, h);

        // Distant clouds / smoke
        g.setColor(new Color(80, 40, 30, 60));
        g.fillOval(100, 80, 400, 120);
        g.fillOval(450, 60, 500, 150);
        g.fillOval(850, 90, 420, 130);

        g.dispose();
        return img;
    }

    private static BufferedImage createCrosshair(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = w / 2;
        int cy = h / 2;

        g.setColor(new Color(0, 255, 255));
        g.setStroke(new BasicStroke(1.8f));
        g.drawOval(cx - 8, cy - 8, 16, 16);
        g.drawLine(cx, cy - 12, cx, cy - 4);
        g.drawLine(cx, cy + 4, cx, cy + 12);
        g.drawLine(cx - 12, cy, cx - 4, cy);
        g.drawLine(cx + 4, cy, cx + 12, cy);

        // Center dot
        g.setColor(Color.RED);
        g.fillRect(cx - 1, cy - 1, 3, 3);

        g.dispose();
        return img;
    }

    private static BufferedImage createHeartIcon(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(230, 40, 40));
        int cx = w / 2;
        int cy = h / 2;

        // Heart shape
        g.fillOval(cx - 8, cy - 7, 8, 8);
        g.fillOval(cx, cy - 7, 8, 8);
        int[] xs = {cx - 8, cx + 8, cx};
        int[] ys = {cy - 2, cy - 2, cy + 8};
        g.fillPolygon(xs, ys, 3);

        g.dispose();
        return img;
    }

    private static BufferedImage createAmmoIcon(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Three golden rifle cartridges
        g.setColor(new Color(255, 215, 0));
        g.fillRect(5, 7, 4, 12);
        g.fillRect(10, 5, 4, 14);
        g.fillRect(15, 7, 4, 12);

        // Bullet tips
        g.setColor(new Color(255, 120, 0));
        int[] t1x = {5, 7, 9};
        int[] t1y = {7, 3, 7};
        g.fillPolygon(t1x, t1y, 3);

        int[] t2x = {10, 12, 14};
        int[] t2y = {5, 1, 5};
        g.fillPolygon(t2x, t2y, 3);

        int[] t3x = {15, 17, 19};
        int[] t3y = {7, 3, 7};
        g.fillPolygon(t3x, t3y, 3);

        g.dispose();
        return img;
    }
}
