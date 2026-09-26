package com.hardcoremario.model.world;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.model.entity.Entity;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Tile represents blocks, platforms, breakable obstacles, hazards (spikes), and exit portals.
 */
public class Tile extends Entity {

    public enum TileType {
        GROUND("block_ground", true, false, false),
        SOLID_BLOCK("block_platform", true, false, false),
        BREAKABLE_BLOCK("block_metal", true, false, true), // Shoot to destroy (Player only)
        BARREL("barrel", true, false, true),
        SPIKES("hazard_spikes", false, true, false),       // Instant kill on contact
        EXIT("exit", false, false, false);

        private final String assetKey;
        private final boolean solid;
        private final boolean hazard;
        private final boolean breakable;

        TileType(String assetKey, boolean solid, boolean hazard, boolean breakable) {
            this.assetKey = assetKey;
            this.solid = solid;
            this.hazard = hazard;
            this.breakable = breakable;
        }

        public String getAssetKey() { return assetKey; }
        public boolean isSolid() { return solid; }
        public boolean isHazard() { return hazard; }
        public boolean isBreakable() { return breakable; }
    }

    private final TileType type;
    private int hp;
    private final int maxHp;
    private Color customColor = null;
    private boolean pointingDown = false;

    public Tile(double x, double y, int width, int height, TileType type) {
        super(x, y, width, height);
        this.type = type;
        this.maxHp = type.isBreakable() ? 25 : 999999;
        this.hp = this.maxHp;
    }

    public Tile(double x, double y, double width, double height, TileType type) {
        this(x, y, (int) Math.round(width), (int) Math.round(height), type);
    }

    public Tile(double x, double y, double width, double height, TileType type, Color customColor) {
        this(x, y, (int) Math.round(width), (int) Math.round(height), type);
        this.customColor = customColor;
    }

    public Color getCustomColor() { return customColor; }
    public void setCustomColor(Color customColor) { this.customColor = customColor; }

    public boolean isPointingDown() { return pointingDown; }
    public void setPointingDown(boolean pointingDown) { this.pointingDown = pointingDown; }

    public TileType getType() { return type; }
    public boolean isSolid() { return type.isSolid(); }
    public boolean isHazard() { return type.isHazard(); }
    public boolean isBreakable() { return type.isBreakable(); }

    @Override
    public java.awt.geom.Rectangle2D.Double getHitbox() {
        if (type == TileType.SPIKES) {
            // For spikes, use a fair triangular/inset hitbox so empty corners don't kill player
            double insetX = 4.0;
            double hitW = Math.max(4.0, width - insetX * 2);
            double hitH = height * 0.70;
            if (pointingDown) {
                // Lethal zone starts at top (against ceiling) and extends down 70%
                return new java.awt.geom.Rectangle2D.Double(position.getX() + insetX, position.getY(), hitW, hitH);
            } else {
                // Lethal zone starts at bottom 70% (against floor)
                return new java.awt.geom.Rectangle2D.Double(position.getX() + insetX, position.getY() + (height - hitH), hitW, hitH);
            }
        }
        return super.getHitbox();
    }

    /**
     * Called when a player's bullet hits this tile.
     * Only breakable tiles take damage.
     */
    public boolean takeDamage(int amount) {
        if (!isBreakable() || !active) return false;
        hp -= amount;
        if (hp <= 0) {
            active = false;
            // Spawn debris particles
            ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), new Color(255, 140, 20));
            ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), new Color(180, 80, 10));
            return true; // Tile destroyed
        }
        return false;
    }

    @Override
    public void update(double deltaTime) {
        // Static tile
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        if (type == TileType.EXIT) {
            BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
            if (img != null) {
                renderTiled(g, img, drawX, drawY, width, height, false);
                return;
            }
            // Sci-Fi Exit Portal
            g.setColor(new Color(0, 255, 200, 100));
            g.fillRoundRect(drawX, drawY, width, height, 16, 16);
            g.setColor(new Color(0, 255, 200));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(drawX, drawY, width, height, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("EXIT", drawX + (width - 34) / 2, drawY + (height + 6) / 2);
            return;
        }

        if (type == TileType.SPIKES) {
            BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
            if (img != null) {
                renderTiled(g, img, drawX, drawY, width, height, pointingDown);
                return;
            }
            // Render sharp red/steel lethal spikes (fallback)
            int numSpikes = Math.max(1, width / 10);
            int spikeW = width / numSpikes;
            for (int i = 0; i < numSpikes; i++) {
                int sx = drawX + i * spikeW;
                int[] xs = {sx, sx + spikeW / 2, sx + spikeW};
                int[] ys;
                if (pointingDown) {
                    ys = new int[]{drawY, drawY + height - 2, drawY};
                } else {
                    ys = new int[]{drawY + height, drawY + 2, drawY + height};
                }
                g.setColor(new Color(230, 40, 40));
                g.fillPolygon(xs, ys, 3);
                g.setColor(new Color(120, 0, 0));
                g.drawPolygon(xs, ys, 3);
            }
            return;
        }

        if (type == TileType.BREAKABLE_BLOCK) {
            BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
            if (img != null) {
                renderTiled(g, img, drawX, drawY, width, height, false);
                return;
            }
            // Destructible block (fallback)
            g.setColor(new Color(210, 105, 30));
            g.fillRect(drawX, drawY, width, height);
            g.setColor(new Color(255, 140, 0));
            g.setStroke(new BasicStroke(2f));
            g.drawRect(drawX + 1, drawY + 1, width - 2, height - 2);
            g.setColor(new Color(90, 40, 10));
            g.drawLine(drawX + 4, drawY + height / 2, drawX + width - 4, drawY + height / 2);
            g.drawLine(drawX + width / 2, drawY + 4, drawX + width / 2, drawY + height - 4);
            return;
        }

        if (type == TileType.SOLID_BLOCK) {
            BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
            if (img != null && customColor == null) {
                renderTiled(g, img, drawX, drawY, width, height, false);
                return;
            }
            // Indestructible solid blue block (fallback)
            Color base = customColor != null ? customColor : new Color(0, 80, 239);
            g.setColor(base);
            g.fillRect(drawX, drawY, width, height);
            g.setColor(new Color(120, 180, 255));
            g.drawRect(drawX, drawY, width - 1, height - 1);
            g.setColor(new Color(0, 40, 160));
            g.fillRect(drawX + 4, drawY + 4, width - 8, height - 8);
            return;
        }

        // Ground / Large Floor Platform
        if (type == TileType.GROUND) {
            if (customColor != null) {
                renderProceduralGround(g, drawX, drawY, width, height, customColor);
                return;
            }
            BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
            if (img != null) {
                renderTiled(g, img, drawX, drawY, width, height, false);
                return;
            }
            renderProceduralGround(g, drawX, drawY, width, height, new Color(24, 26, 32));
            return;
        }

        // Other generic blocks / barrels
        BufferedImage img = AssetManager.getInstance().getImage(type.getAssetKey());
        if (img != null) {
            renderTiled(g, img, drawX, drawY, width, height, false);
        } else {
            g.setColor(customColor != null ? customColor : new Color(70, 70, 80));
            g.fillRect(drawX, drawY, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(drawX, drawY, width, height);
        }
    }

    private void renderProceduralGround(Graphics2D g, int drawX, int drawY, int width, int height, Color base) {
        g.setColor(base);
        g.fillRect(drawX, drawY, width, height);

        // Sleek metal walking trim on top edge
        g.setColor(new Color(80, 85, 100));
        g.fillRect(drawX, drawY, width, 4);
        g.setColor(new Color(130, 140, 160));
        g.drawLine(drawX, drawY, drawX + width, drawY);

        // Dark accent seam below the trim
        g.setColor(new Color(12, 14, 18));
        g.drawLine(drawX, drawY + 4, drawX + width, drawY + 4);

        // Industrial floor panel seams and rivet accents every 80px
        int panelW = 80;
        for (int px = 0; px < width; px += panelW) {
            int sx = drawX + px;
            g.setColor(new Color(15, 17, 22));
            g.drawLine(sx, drawY + 4, sx, drawY + height);
            g.setColor(new Color(50, 55, 65));
            g.drawLine(sx + 1, drawY + 4, sx + 1, drawY + height);

            // Small metal rivets near top seam
            g.setColor(new Color(90, 95, 110));
            g.fillOval(sx + 6, drawY + 8, 3, 3);
            g.fillOval(sx + panelW - 9, drawY + 8, 3, 3);
        }
    }

    private void renderTiled(Graphics2D g, BufferedImage img, int drawX, int drawY, int width, int height, boolean flipV) {
        int tw = img.getWidth();
        int th = img.getHeight();

        if (width <= tw && height <= th) {
            if (flipV) {
                g.drawImage(img, drawX, drawY + height, width, -height, null);
            } else {
                g.drawImage(img, drawX, drawY, width, height, null);
            }
        } else {
            for (int tx = 0; tx < width; tx += tw) {
                int rw = Math.min(tw, width - tx);
                for (int ty = 0; ty < height; ty += th) {
                    int rh = Math.min(th, height - ty);
                    if (flipV) {
                        g.drawImage(img, drawX + tx, drawY + ty + rh, rw, -rh, null);
                    } else {
                        g.drawImage(img, drawX + tx, drawY + ty, rw, rh, null);
                    }
                }
            }
        }
    }
}
