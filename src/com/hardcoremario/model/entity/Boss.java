package com.hardcoremario.model.entity;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.model.projectile.BossBullet;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.model.world.Tile;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Boss enemy for Stage 4 (Apex Overlord).
 * - Patrols horizontally back and forth between walls in a continuous loop.
 * - Fires a 5-way spread of block-penetrating plasma bolts every 4.0 seconds.
 * - Protected by an impenetrable energy shield until all Boss Protection Cores are destroyed.
 */
public class Boss extends Enemy {

    private Level level;
    private boolean movingRight = false;
    public static final int BURST_COUNT_PER_SET = 3; // 3 rapid trailing shots per set ("3 นัดรวด")
    public static final double BURST_INTERVAL = 0.08; // 0.08s (80ms) interval so bullets follow each other closely like a tail ("ติดตามกันเป็นหาง")
    public static final double SHOOT_COOLDOWN = 4.0; // 4 seconds cooldown between sets

    private double cooldownTimer = 2.0; // Initial delay before first barrage set
    private int burstShotsRemaining = 0; // Number of volleys remaining in current set
    private double burstIntervalTimer = 0.0; // Timer between volleys in current burst
    private double lockedBaseAngle = 0.0; // Locked aim angle for the trailing tail

    private double shieldAnimTimer = 0.0;
    private double shieldHitTimer = 0.0;
    private double walkAnimTimer = 0.0;

    public Boss(double x, double y, int width, int height) {
        super(x, y, width, height, 500, 2000.0, SHOOT_COOLDOWN);
        this.facingRight = false;
        this.movingRight = false;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isShielded() {
        return level != null && level.getActiveBossCoreCount() > 0;
    }

    @Override
    public void updateAI(Player player, Level level, double deltaTime) {
        this.level = level;
        if (isDead()) return;

        shieldAnimTimer += deltaTime * 4.0;
        if (shieldHitTimer > 0) shieldHitTimer -= deltaTime;

        // Attack handling: 3 rapid shots trailing like a tail per set, then 4.0s cooldown
        if (burstShotsRemaining > 0) {
            burstIntervalTimer -= deltaTime;
            if (burstIntervalTimer <= 0) {
                fireSpreadBarrage(lockedBaseAngle, level);
                burstShotsRemaining--;
                if (burstShotsRemaining > 0) {
                    burstIntervalTimer = BURST_INTERVAL;
                } else {
                    cooldownTimer = SHOOT_COOLDOWN; // Set finished -> start 4.0s cooldown
                }
            }
        } else {
            cooldownTimer -= deltaTime;
            if (cooldownTimer <= 0) {
                // Lock aim angle towards player for this 3-shot tail set
                double centerX = getCenterX();
                double centerY = getCenterY();
                lockedBaseAngle = Math.atan2(player.getCenterY() - centerY, player.getCenterX() - centerX);

                // Fire 1st shot of the tail
                fireSpreadBarrage(lockedBaseAngle, level);
                burstShotsRemaining = BURST_COUNT_PER_SET - 1; // 2 more shots to follow in tail
                burstIntervalTimer = BURST_INTERVAL;
            }
        }
    }

    /**
     * Fires a 5-way spread barrage of block-penetrating BossBullets along the specified base angle.
     */
    private void fireSpreadBarrage(double baseAngle, Level level) {
        double centerX = getCenterX();
        double centerY = getCenterY();

        // 5-way spread angle offsets: -28°, -14°, 0°, +14°, +28°
        double[] angleOffsets = {-28.0, -14.0, 0.0, 14.0, 28.0};
        double bulletSpeed = 440.0;
        int bulletDamage = 25;

        for (double offsetDeg : angleOffsets) {
            double angle = baseAngle + Math.toRadians(offsetDeg);
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);

            BossBullet bullet = new BossBullet(centerX, centerY, dirX, dirY, bulletSpeed, bulletDamage, this);
            level.addProjectile(bullet);
        }

        // Muzzle energy flash along firing vector & heavy cannon sound
        double muzzleX = centerX + Math.cos(baseAngle) * 36.0;
        double muzzleY = centerY + Math.sin(baseAngle) * 20.0;
        ParticleSystem.getInstance().spawnMuzzleFlash(muzzleX, muzzleY);
        SoundManager.getInstance().playEnemyShoot();
    }

    @Override
    public void update(double deltaTime) {
        if (invulnerableTimer > 0) invulnerableTimer -= deltaTime;
        walkAnimTimer += deltaTime;
    }

    /**
     * Relentless horizontal movement until hitting a wall / dead end, then turning around.
     */
    public void updatePhysicsAndCollisions(Level level, double deltaTime) {
        this.level = level;
        if (isDead()) return;

        double moveSpeed = 150.0;
        velocity.setX(movingRight ? moveSpeed : -moveSpeed);
        facingRight = movingRight;

        // Apply gravity to stay grounded
        velocity.setY(Math.min(velocity.getY() + Constants.GRAVITY * deltaTime, Constants.MAX_FALL_SPEED));

        // 1. Move X and check solid tile wall collisions
        position.setX(position.getX() + velocity.getX() * deltaTime);
        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isSolid() || !tile.isActive()) continue;
            if (hb.intersects(tile.getHitbox())) {
                if (velocity.getX() > 0) {
                    position.setX(tile.getX() - width);
                    movingRight = false;
                    facingRight = false;
                } else if (velocity.getX() < 0) {
                    position.setX(tile.getX() + tile.getWidth());
                    movingRight = true;
                    facingRight = true;
                }
                velocity.setX(0);
                hb = getHitbox();
                break;
            }
        }

        // Check world map boundaries
        if (position.getX() + width >= level.getMaxX() - 10) {
            position.setX(level.getMaxX() - 10 - width);
            movingRight = false;
            facingRight = false;
        } else if (position.getX() <= level.getMinX() + 10) {
            position.setX(level.getMinX() + 10);
            movingRight = true;
            facingRight = true;
        }

        // 2. Move Y and resolve floor collision
        position.setY(position.getY() + velocity.getY() * deltaTime);
        isGrounded = false;
        hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isSolid() || !tile.isActive()) continue;
            if (hb.intersects(tile.getHitbox())) {
                if (velocity.getY() > 0) {
                    position.setY(tile.getY() - height);
                    velocity.setY(0);
                    isGrounded = true;
                } else if (velocity.getY() < 0) {
                    position.setY(tile.getY() + tile.getHeight());
                    velocity.setY(0);
                }
                hb = getHitbox();
            }
        }

        walkAnimTimer += deltaTime * 8.0;
    }

    @Override
    public void takeDamage(int amount) {
        if (isShielded()) {
            // Invulnerable while any core remains intact!
            shieldHitTimer = 0.25;
            ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.CYAN);
            SoundManager.getInstance().playHit();
            return;
        }
        super.takeDamage(amount);
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.RED);
        SoundManager.getInstance().playHit();
    }

    @Override
    protected void onDeath() {
        // Massive victory fireworks and sparks upon defeating the Boss
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.YELLOW);
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.RED);
        ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.CYAN);
        SoundManager.getInstance().playVictory();
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        if (isDead()) return;

        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);
        int centerX = (int) (getCenterX() - offsetX);
        int centerY = (int) (getCenterY() - offsetY);

        // 1. Draw Boss Sprite or Procedural Fallback
        BufferedImage img = AssetManager.getInstance().getImage("boss");
        if (img != null) {
            if (facingRight) {
                g.drawImage(img, drawX, drawY, width, height, null);
            } else {
                // Flip horizontally when facing left
                g.drawImage(img, drawX + width, drawY, -width, height, null);
            }
        } else {
            // Procedural heavy armored mech
            g.setColor(new Color(40, 45, 55));
            g.fillRoundRect(drawX + 10, drawY + 10, width - 20, height - 20, 16, 16);
            g.setColor(new Color(255, 30, 30));
            int eyeX = facingRight ? drawX + width - 35 : drawX + 15;
            g.fillRect(eyeX, drawY + 25, 20, 8);
        }

        // 2. Impenetrable Shield Forcefield Barrier (when cores exist)
        if (isShielded()) {
            int shieldRadius = (int) (Math.max(width, height) * 0.72);
            int pulse = (int) (Math.sin(shieldAnimTimer) * 4);
            int r = shieldRadius + pulse;

            // Outer glowing shield aura
            int alpha = shieldHitTimer > 0 ? 140 : 60;
            Color shieldGlow = shieldHitTimer > 0 ? new Color(255, 255, 255, alpha) : new Color(0, 230, 255, alpha);
            g.setColor(shieldGlow);
            g.fillOval(centerX - r, centerY - r, r * 2, r * 2);

            // Rotating containment shield rings
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(shieldHitTimer > 0 ? Color.WHITE : new Color(0, 240, 255, 220));
            g.drawOval(centerX - r, centerY - r, r * 2, r * 2);

            // Shield status label
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(shieldHitTimer > 0 ? Color.WHITE : new Color(0, 240, 255));
            String status = "[SHIELD PROTECTED: " + level.getActiveBossCoreCount() + " CORES]";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(status, centerX - fm.stringWidth(status) / 2, drawY - 14);
        }
    }
}
