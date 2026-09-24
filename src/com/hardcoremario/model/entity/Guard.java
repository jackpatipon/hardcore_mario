package com.hardcoremario.model.entity;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.model.projectile.EnemyBullet;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.model.world.Tile;
import com.hardcoremario.util.Constants;
import com.hardcoremario.util.Vector2D;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Guard soldier representing the Apex Syndicate enemy from the Storyboard.
 * Responds to gravity, falls if blocks underneath are destroyed, and dies instantly upon touching spikes.
 */
public class Guard extends Enemy {
    private double patrolStartX;
    private double patrolDistance;
    private boolean movingRight = true;
    private boolean alerted = false;
    private double aimAngle = 0.0;

    public Guard(double x, double y, double patrolDistance) {
        super(x, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT, Constants.GUARD_MAX_HP, Constants.GUARD_SIGHT_RANGE, Constants.GUARD_FIRE_COOLDOWN);
        this.patrolStartX = x;
        this.patrolDistance = patrolDistance;
    }

    @Override
    public void updateAI(Player player, Level level, double deltaTime) {
        if (isDead()) return;

        // Check distance to player
        double dist = position.distanceTo(player.getPosition());
        alerted = (dist <= sightRange && !player.isDead());

        if (alerted) {
            // Face and aim towards player
            aimAngle = position.angleToDegrees(player.getCenterX(), player.getCenterY());
            facingRight = (player.getCenterX() >= getCenterX());

            // Stop moving to shoot
            velocity.setX(0);

            // Shoot at player
            if (attackTimer <= 0) {
                shootAt(player.getCenterX(), player.getCenterY(), level);
                attackTimer = attackCooldown + (random.nextDouble() * 0.4 - 0.2);
            }
        } else {
            // Idle patrol back and forth only if patrolDistance > 0 and grounded
            if (patrolDistance > 0 && isGrounded) {
                if (movingRight) {
                    velocity.setX(Constants.GUARD_MOVE_SPEED);
                    facingRight = true;
                    if (getX() >= patrolStartX + patrolDistance) {
                        movingRight = false;
                    }
                } else {
                    velocity.setX(-Constants.GUARD_MOVE_SPEED);
                    facingRight = false;
                    if (getX() <= patrolStartX) {
                        movingRight = true;
                    }
                }
            } else if (patrolDistance == 0) {
                velocity.setX(0);
            }
        }
    }

    private void shootAt(double targetX, double targetY, Level level) {
        double muzzleX = getCenterX() + (facingRight ? 18 : -18);
        double muzzleY = getCenterY() - 10;

        Vector2D dir = new Vector2D(targetX - muzzleX, targetY - muzzleY).normalize();

        EnemyBullet bullet = new EnemyBullet(
            muzzleX, muzzleY,
            dir.getX(), dir.getY(),
            Constants.ENEMY_BULLET_SPEED,
            Constants.ENEMY_BULLET_DAMAGE,
            this
        );
        level.addProjectile(bullet);

        ParticleSystem.getInstance().spawnMuzzleFlash(muzzleX, muzzleY);
        SoundManager.getInstance().playEnemyShoot();
    }

    @Override
    public void update(double deltaTime) {
        if (attackTimer > 0) attackTimer -= deltaTime;
        if (invulnerableTimer > 0) invulnerableTimer -= deltaTime;

        // Gravity: always pull down so enemy falls if block below is destroyed!
        velocity.setY(Math.min(velocity.getY() + Constants.GRAVITY * deltaTime, Constants.MAX_FALL_SPEED));
    }

    public void updatePhysicsAndCollisions(Level level, double deltaTime) {
        // Move X and resolve solid tile collisions
        position.setX(position.getX() + velocity.getX() * deltaTime);
        checkTileCollisionsX(level);

        // Move Y and resolve solid tile collisions
        position.setY(position.getY() + velocity.getY() * deltaTime);
        isGrounded = false;
        checkTileCollisionsY(level);
    }

    private void checkTileCollisionsX(Level level) {
        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isActive() || !tile.isSolid()) continue;
            if (hb.intersects(tile.getHitbox())) {
                if (velocity.getX() > 0) {
                    position.setX(tile.getX() - width);
                    movingRight = false;
                } else if (velocity.getX() < 0) {
                    position.setX(tile.getX() + tile.getWidth());
                    movingRight = true;
                }
                velocity.setX(0);
                hb = getHitbox();
            }
        }
    }

    private void checkTileCollisionsY(Level level) {
        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isActive()) continue;

            // Instant kill if enemy touches spikes!
            if (tile.isHazard() && hb.intersects(tile.getHitbox())) {
                takeDamage(99999);
                ParticleSystem.getInstance().spawnSparks(getCenterX(), getCenterY(), Color.RED);
                return;
            }

            // Solid tile collision (landing on block)
            if (tile.isSolid() && hb.intersects(tile.getHitbox())) {
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
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        // Flash when damaged
        if (isInvulnerable() && ((int) (System.currentTimeMillis() / 60) % 2 == 0)) {
            return;
        }

        AssetManager am = AssetManager.getInstance();
        BufferedImage sprite;

        if (alerted) {
            String dirKey = am.get8DirectionKey(aimAngle);
            sprite = am.getImage("guard_aim_" + dirKey);
        } else {
            sprite = am.getImage(facingRight ? "guard_idle_right" : "guard_idle_left");
        }

        if (sprite != null) {
            g.drawImage(sprite, drawX - 12, drawY - 6, 64, 64, null);
        } else {
            g.setColor(new Color(60, 60, 75));
            g.fillRect(drawX, drawY, width, height);
        }

        // Mini HP Bar above Guard
        int barW = 36;
        int barH = 5;
        int barX = drawX + (width - barW) / 2;
        int barY = drawY - 10;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
        double hpPercent = (double) currentHp / maxHp;
        g.setColor(Color.RED);
        g.fillRect(barX, barY, (int) (barW * hpPercent), barH);
    }
}
