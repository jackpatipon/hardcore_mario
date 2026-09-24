package com.hardcoremario.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Singleton AssetManager responsible for loading, caching, and serving game sprites.
 * Provides fallback procedural graphics if any image file is missing.
 */
public class AssetManager {
    private static AssetManager instance;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    // Direction angle mappings for 8-direction sprites
    public static final String[] DIR_KEYS = {"e", "ne", "n", "nw", "w", "sw", "s", "se"};

    private AssetManager() {
        preloadAllAssets();
    }

    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    private void preloadAllAssets() {
        // Player 8-direction sprites
        for (String dir : DIR_KEYS) {
            loadImage("player_aim_" + dir, "assets/characters/player/8directions/aim_" + dir + ".png");
            loadImage("guard_aim_" + dir, "assets/characters/guard/8directions/aim_" + dir + ".png");
        }

        // Player states
        loadImage("player_idle", "assets/characters/player/idle.png");
        loadImage("player_jump", "assets/characters/player/jump.png");
        loadImage("player_crouch", "assets/characters/player/crouch.png");

        // Guard states
        loadImage("guard_idle_right", "assets/characters/guard/idle_right.png");
        loadImage("guard_idle_left", "assets/characters/guard/idle_left.png");

        // Bullets
        loadImage("bullet_player", "assets/projectiles/bullet_player.png");
        loadImage("bullet_enemy", "assets/projectiles/bullet_enemy.png");

        // Items
        loadImage("health_pack", "assets/items/health_pack.png");
        loadImage("ammo_box", "assets/items/ammo_box.png");

        // Environment
        loadImage("block_platform", "assets/environment/block_platform.png");
        loadImage("block_ground", "assets/environment/block_ground.png");
        loadImage("block_metal", "assets/environment/block_metal.png");
        loadImage("barrel", "assets/environment/barrel.png");
        loadImage("hazard_spikes", "assets/environment/hazard_spikes.png");

        // Backgrounds
        loadImage("bg_main", "assets/backgrounds/bg_main.png");
        loadImage("bg_far", "assets/backgrounds/bg_far.png");

        // UI
        loadImage("crosshair", "assets/ui/crosshair.png");
        loadImage("icon_health", "assets/ui/icon_health.png");
        loadImage("icon_ammo", "assets/ui/icon_ammo.png");
    }

    private void loadImage(String key, String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    imageCache.put(key, img);
                    return;
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read asset " + path + ": " + e.getMessage());
            }
        }
        // Fallback procedural image
        imageCache.put(key, createFallbackImage(key));
    }

    public BufferedImage getImage(String key) {
        BufferedImage img = imageCache.get(key);
        if (img == null) {
            img = createFallbackImage(key);
            imageCache.put(key, img);
        }
        return img;
    }

    /**
     * Determines which 8-direction sprite key to use based on aiming angle (0 - 360 degrees).
     * 0° = East, 90° = North, 180° = West, 270° = South.
     */
    public String get8DirectionKey(double degrees) {
        // Normalize angle to [0, 360)
        double a = (degrees % 360.0 + 360.0) % 360.0;
        // Each sector spans 45 degrees, centered around:
        // E:  337.5 to 22.5
        // NE: 22.5  to 67.5
        // N:  67.5  to 112.5
        // NW: 112.5 to 157.5
        // W:  157.5 to 202.5
        // SW: 202.5 to 247.5
        // S:  247.5 to 292.5
        // SE: 292.5 to 337.5

        if (a >= 337.5 || a < 22.5) {
            return "e";
        } else if (a >= 22.5 && a < 67.5) {
            return "ne";
        } else if (a >= 67.5 && a < 112.5) {
            return "n";
        } else if (a >= 112.5 && a < 157.5) {
            return "nw";
        } else if (a >= 157.5 && a < 202.5) {
            return "w";
        } else if (a >= 202.5 && a < 247.5) {
            return "sw";
        } else if (a >= 247.5 && a < 292.5) {
            return "s";
        } else {
            return "se";
        }
    }

    private BufferedImage createFallbackImage(String key) {
        int w = 48, h = 48;
        Color c = Color.MAGENTA;
        if (key.contains("player")) {
            w = 40; h = 58; c = Color.RED;
        } else if (key.contains("guard")) {
            w = 40; h = 58; c = Color.DARK_GRAY;
        } else if (key.contains("bullet")) {
            w = 16; h = 16; c = Color.YELLOW;
        } else if (key.contains("item") || key.contains("health") || key.contains("ammo")) {
            w = 32; h = 32; c = Color.GREEN;
        }

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(c);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, w - 1, h - 1);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.drawString(key.length() > 6 ? key.substring(0, 6) : key, 2, h / 2);
        g.dispose();
        return img;
    }
}
