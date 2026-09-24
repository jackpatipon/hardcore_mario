package com.hardcoremario.model.world;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.model.entity.Entity;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Tile represents blocks, platforms, hazards, and interactive world objects.
 */
public class Tile extends Entity {

    public enum TileType {
        GROUND("block_ground", true, false),
        PLATFORM("block_platform", true, false),
        METAL("block_metal", true, false),
        BARREL("barrel", true, false),
        SPIKES("hazard_spikes", false, true),
        EXIT("exit", false, false);

        private final String assetKey;
        private final boolean solid;
        private final boolean hazard;

        TileType(String assetKey, boolean solid, boolean hazard) {
            this.assetKey = assetKey;
            this.solid = solid;
            this.hazard = hazard;
        }

        public String getAssetKey() { return assetKey; }
        public boolean isSolid() { return solid; }
        public boolean isHazard() { return hazard; }
    }

    private final TileType type;

    public Tile(double x, double y, int width, int height, TileType type) {
        super(x, y, width, height);
        this.type = type;
    }

    public TileType getType() { return type; }
    public boolean isSolid() { return type.isSolid(); }
    public boolean isHazard() { return type.isHazard(); }

    @Override
    public void update(double deltaTime) {
        // Static tile, no continuous physics update needed
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        if (type == TileType.EXIT) {
            // High-tech sci-fi extraction portal
            g.setColor(new Color(0, 255, 255, 80));
            g.fillRoundRect(drawX, drawY, width, height, 12, 12);
            g.setColor(new Color(0, 255, 255));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(drawX, drawY, width, height, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("EXIT", drawX + width / 2 - 14, drawY + height / 2 + 5);
            return;
        }

        BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            // Fallback rendering
            g.setColor(type.isHazard() ? Color.RED : Color.GRAY);
            g.fillRect(drawX, drawY, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(drawX, drawY, width, height);
        }
    }
}
