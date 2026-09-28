package com.hardcoremario.model.entity;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Boss Protection Core.
 * Generates an impenetrable energy barrier shielding the Boss from all player damage.
 * The player must locate and destroy all cores in the arena to make the Boss vulnerable.
 */
public class BossCore extends LivingEntity {

    private double animTimer = 0.0;
    private double baseY;
    private Boss targetBoss = null;

    public BossCore(double x, double y, int width, int height) {
        super(x, y, width, height, 50); // 50 HP per core
        this.baseY = y;
        this.animTimer = (x * 0.05) % (Math.PI * 2); // Stagger animations between cores
    }

    public void setTargetBoss(Boss boss) {
        this.targetBoss = boss;
    }

    @Override
    public void update(double deltaTime) {
        animTimer += deltaTime * 3.5;
        // Subtle floating / bobbing in place
        position.setY(baseY + Math.sin(animTimer) * 5.0);

        if (invulnerableTimer > 0) {
            invulnerableTimer -= deltaTime;
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (isDead()) return;
        currentHp = Math.max(0, currentHp - amount);
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.CYAN);
        SoundManager.getInstance().playCoreHit();
        if (currentHp <= 0) {
            onDeath();
        }
    }

    @Override
    protected void onDeath() {
        setActive(false);
        SoundManager.getInstance().playCoreDestroy();
        // Massive energy explosion when core is shattered
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.CYAN);
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.WHITE);
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.YELLOW);
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        if (!isActive() || isDead()) return;

        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);
        int centerX = (int) (getCenterX() - offsetX);
        int centerY = (int) (getCenterY() - offsetY);

        // 1. Draw glowing energy tether beam connecting to Boss
        if (targetBoss != null && !targetBoss.isDead() && targetBoss.isActive()) {
            int bossCenterX = (int) (targetBoss.getCenterX() - offsetX);
            int bossCenterY = (int) (targetBoss.getCenterY() - offsetY);

            int pulseAlpha = 80 + (int) (Math.sin(animTimer * 2.0) * 35);
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0, 240, 255, pulseAlpha));
            g.drawLine(centerX, centerY, bossCenterX, bossCenterY);

            // Inner bright core beam
            g.setStroke(new BasicStroke(1.0f));
            g.setColor(new Color(255, 255, 255, pulseAlpha + 20));
            g.drawLine(centerX, centerY, bossCenterX, bossCenterY);
        }

        // 2. Draw Core Sprite or Procedural Fallback
        BufferedImage img = AssetManager.getInstance().getImage("boss_core");
        if (img != null) {
            g.drawImage(img, drawX, drawY, width, height, null);
        } else {
            // Procedural glowing plasma sphere
            // Outer glow
            g.setColor(new Color(0, 230, 255, 60));
            g.fillOval(drawX - 4, drawY - 4, width + 8, height + 8);

            // Containment ring
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(new Color(0, 210, 255, 220));
            g.drawOval(drawX + 2, drawY + 2, width - 4, height - 4);

            // Core sphere
            g.setColor(new Color(0, 160, 255));
            g.fillOval(drawX + 6, drawY + 6, width - 12, height - 12);

            // Hot center
            g.setColor(Color.WHITE);
            g.fillOval(drawX + 14, drawY + 14, width - 28, height - 28);
        }

        // 3. Mini Health Bar over Core
        int barW = width + 8;
        int barH = 5;
        int barX = drawX - 4;
        int barY = drawY - 10;

        g.setColor(new Color(20, 20, 20, 200));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(new Color(0, 220, 255));
        double hpRatio = (double) currentHp / maxHp;
        g.fillRect(barX, barY, (int) (barW * hpRatio), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);
    }
}
