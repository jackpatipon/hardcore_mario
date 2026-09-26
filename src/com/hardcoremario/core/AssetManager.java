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
        // Dynamically load all 8-direction sprites (including animation frames _1, _2, _0, etc.)
        loadDirectionalSprites("player", "assets/characters/player/8directions");
        loadDirectionalSprites("guard", "assets/characters/guard/8directions");

        // Player states
        loadImage("player_idle", "assets/characters/player/idle.png");
        loadImage("player_jump", "assets/characters/player/jump.png");
        loadImage("player_crouch", "assets/characters/player/crouch.png");
        loadImage("player_crouch_e", "assets/characters/player/crouch_e.png");
        loadImage("player_crouch_w", "assets/characters/player/crouch_w.png");

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
        loadImage("exit", "assets/environment/exit.png");

        // Backgrounds
        loadImage("bg_main", "assets/backgrounds/bg_main.png");
        loadImage("bg_far", "assets/backgrounds/bg_far.png");

        // UI
        loadImage("crosshair", "assets/ui/crosshair.png");
        loadImage("icon_health", "assets/ui/icon_health.png");
        loadImage("icon_ammo", "assets/ui/icon_ammo.png");
    }

    private void loadDirectionalSprites(String entityType, String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    String baseName = name.substring(0, name.lastIndexOf('.'));
                    try {
                        BufferedImage img = ImageIO.read(file);
                        if (img != null) {
                            imageCache.put(entityType + "_" + baseName, img);
                        }
                    } catch (IOException e) {
                        System.err.println("Warning: Could not read sprite " + file.getPath() + ": " + e.getMessage());
                    }
                }
            }
        }
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
    }

    public boolean hasImage(String key) {
        return imageCache.containsKey(key);
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
     * Retrieves an 8-directional sprite for the given entity (e.g. "player", "guard"),
     * direction (e.g. "e", "ne", "n"), and animation frame (1 or 2).
     *
     * Fallback resolution order:
     * 1. <entity>_aim_<dir>_<frame>   (e.g. player_aim_e_1, player_aim_e_2)
     * 2. <entity>_aim_<dir>_1         (default standing/idle frame)
     * 3. <entity>_aim_<dir>_0         (e.g. aim_n_0, aim_s_0)
     * 4. <entity>_aim_<dir>           (e.g. guard_aim_e without frame suffix)
     * 5. <entity>_aim_<dir>_2         (if frame 1 missing)
     * 6. Fallback procedural image
     */
    public BufferedImage getDirectionalSprite(String entityType, String dirKey, int frame) {
        // 1. Exact frame (e.g. player_aim_e_1 or player_aim_e_2)
        String key = entityType + "_aim_" + dirKey + "_" + frame;
        BufferedImage img = imageCache.get(key);
        if (img != null) return img;

        // 2. Default standing frame 1
        key = entityType + "_aim_" + dirKey + "_1";
        img = imageCache.get(key);
        if (img != null) return img;

        // 3. Frame 0 (for north/south single-frame sprites like aim_n_0, aim_s_0)
        key = entityType + "_aim_" + dirKey + "_0";
        img = imageCache.get(key);
        if (img != null) return img;

        // 4. Base directional sprite (e.g. guard_aim_e)
        key = entityType + "_aim_" + dirKey;
        img = imageCache.get(key);
        if (img != null) return img;

        // 5. Try frame 2 if frame 1 was missing
        key = entityType + "_aim_" + dirKey + "_2";
        img = imageCache.get(key);
        if (img != null) return img;

        // 6. Return procedural fallback
        return getImage(entityType + "_aim_" + dirKey);
    }

    public BufferedImage getDirectionalSprite(String entityType, String dirKey) {
        return getDirectionalSprite(entityType, dirKey, 1);
    }

    /**
    /**
     * Retrieves the 2-directional crouching sprite for an entity (East/Right or West/Left).
     * @param entityType "player" or "guard"
     * @param facingRight true if aiming right (East), false if aiming left (West)
     * @return crouch_e (right) or crouch_w (left)
     */
    public BufferedImage getCrouchSprite(String entityType, boolean facingRight) {
        String key = entityType + (facingRight ? "_crouch_e" : "_crouch_w");
        BufferedImage img = imageCache.get(key);
        if (img != null) return img;

        // Try opposite with fallback if needed
        String oppKey = entityType + (facingRight ? "_crouch_w" : "_crouch_e");
        img = imageCache.get(oppKey);
        if (img != null) return img;

        // General crouch
        img = imageCache.get(entityType + "_crouch");
        if (img != null) return img;

        return getDirectionalSprite(entityType, facingRight ? "e" : "w", 1);
    }

    public BufferedImage getCrouchSprite(String entityType, String dirKey) {
        boolean facingRight = !dirKey.contains("w");
        return getCrouchSprite(entityType, facingRight);
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
