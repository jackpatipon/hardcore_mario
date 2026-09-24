package com.hardcoremario.model.entity;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.model.projectile.PlayerBullet;
import com.hardcoremario.model.projectile.Projectile;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.model.world.Tile;
import com.hardcoremario.util.Constants;
import com.hardcoremario.util.Vector2D;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Player class representing Mario in commando mode.
 * Demonstrates Inheritance from LivingEntity, Encapsulation, and Physics handling.
 */
public class Player extends LivingEntity {
    private int currentAmmo;
    private int maxMagazine;
    private int reserveAmmo;

    private boolean crouching = false;
    private double shootTimer = 0.0;
    private double reloadTimer = 0.0;
    private boolean reloading = false;

    private double aimAngle = 0.0; // In degrees
    private int kills = 0;

    public Player(double x, double y) {
        super(x, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT, Constants.PLAYER_MAX_HP);
        this.maxMagazine = Constants.DEFAULT_MAGAZINE_SIZE;
        this.currentAmmo = this.maxMagazine;
        this.reserveAmmo = Constants.DEFAULT_RESERVE_AMMO;
    }

    public void handleInput(InputHandler input, Level level, double cameraX, double cameraY, double deltaTime) {
        if (isDead()) return;

        // 1. Aiming angle calculation based on mouse cursor in world coordinates
        double mouseWorldX = input.getMouseX() + cameraX;
        double mouseWorldY = input.getMouseY() + cameraY;
        this.aimAngle = position.angleToDegrees(mouseWorldX, mouseWorldY);

        // Turn facing direction based on mouse position relative to player center
        facingRight = mouseWorldX >= getCenterX();

        // 2. Crouch handling (lowers head hitbox downwards towards feet)
        boolean wantsCrouch = input.isCrouch();
        if (wantsCrouch != crouching) {
            crouching = wantsCrouch;
            int oldHeight = this.height;
            this.height = crouching ? Constants.PLAYER_CROUCH_HEIGHT : Constants.PLAYER_HEIGHT;
            // Shift Y downwards so the head ducks down while feet stay grounded / aligned
            position.setY(position.getY() + (oldHeight - this.height));
        }

        // 3. Horizontal movement
        double targetVx = 0.0;
        // In mid-air, maintain full forward jump speed even while tucked! Only slow down when crawling on solid ground.
        double moveSpeed = (!isGrounded || !crouching) ? Constants.PLAYER_MOVE_SPEED : Constants.PLAYER_MOVE_SPEED * 0.45;
        if (input.isMoveLeft()) targetVx -= moveSpeed;
        if (input.isMoveRight()) targetVx += moveSpeed;
        velocity.setX(targetVx);

        // 4. Jump handling
        if (input.isJump() && isGrounded) {
            if (crouching) {
                // If crouching on ground and player presses jump, uncrouch and jump
                crouching = false;
                position.setY(position.getY() - (Constants.PLAYER_HEIGHT - this.height));
                this.height = Constants.PLAYER_HEIGHT;
            }
            velocity.setY(Constants.PLAYER_JUMP_SPEED);
            isGrounded = false;
            SoundManager.getInstance().playJump();
        }

        // 5. Reload handling
        if (input.consumeReload() || (currentAmmo <= 0 && input.isMouseLeftPressed())) {
            startReload();
        }

        // 6. Shooting handling
        if (input.isMouseLeftPressed() && shootTimer <= 0 && !reloading) {
            if (currentAmmo > 0) {
                shoot(mouseWorldX, mouseWorldY, level);
            } else {
                startReload();
            }
        }
    }

    public void startReload() {
        if (!reloading && currentAmmo < maxMagazine && reserveAmmo > 0) {
            reloading = true;
            reloadTimer = Constants.RELOAD_TIME;
            SoundManager.getInstance().playReload();
        }
    }

    private void shoot(double targetX, double targetY, Level level) {
        currentAmmo--;
        shootTimer = Constants.FIRE_RATE_INTERVAL;

        // Weapon muzzle position offset
        double muzzleX = getCenterX() + (facingRight ? 18 : -18);
        double muzzleY = getCenterY() - (crouching ? 4 : 10);

        // Calculate normalized direction vector towards target
        Vector2D dir = new Vector2D(targetX - muzzleX, targetY - muzzleY).normalize();

        // Spawn bullet
        PlayerBullet bullet = new PlayerBullet(
            muzzleX, muzzleY,
            dir.getX(), dir.getY(),
            Constants.PLAYER_BULLET_SPEED,
            Constants.PLAYER_BULLET_DAMAGE,
            this
        );
        level.addProjectile(bullet);

        // Spawn muzzle flash & cartridge particle
        ParticleSystem.getInstance().spawnMuzzleFlash(muzzleX, muzzleY);
        ParticleSystem.getInstance().spawnCasing(muzzleX, muzzleY, !facingRight);

        // Sound effect
        SoundManager.getInstance().playPlayerShoot();
    }

    @Override
    public void update(double deltaTime) {
        // Update timers
        if (shootTimer > 0) shootTimer -= deltaTime;
        if (invulnerableTimer > 0) invulnerableTimer -= deltaTime;

        // Reload timer
        if (reloading) {
            reloadTimer -= deltaTime;
            if (reloadTimer <= 0) {
                reloading = false;
                int needed = maxMagazine - currentAmmo;
                int actual = Math.min(needed, reserveAmmo);
                currentAmmo += actual;
                reserveAmmo -= actual;
            }
        }

        // Apply gravity
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
            if (!tile.isSolid()) continue;
            if (hb.intersects(tile.getHitbox())) {
                if (velocity.getX() > 0) {
                    position.setX(tile.getX() - width);
                } else if (velocity.getX() < 0) {
                    position.setX(tile.getX() + tile.getWidth());
                }
                velocity.setX(0);
                hb = getHitbox();
            }
        }
    }

    private void checkTileCollisionsY(Level level) {
        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isSolid()) continue;
            if (hb.intersects(tile.getHitbox())) {
                if (velocity.getY() > 0) { // Falling onto ground
                    position.setY(tile.getY() - height);
                    velocity.setY(0);
                    isGrounded = true;
                } else if (velocity.getY() < 0) { // Bumping head into ceiling
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

        // Flash opacity when invulnerable
        if (isInvulnerable() && ((int) (System.currentTimeMillis() / 60) % 2 == 0)) {
            return; // Skip rendering frame for blink effect
        }

        // Select sprite
        BufferedImage sprite;
        AssetManager am = AssetManager.getInstance();

        if (crouching) {
            sprite = am.getImage("player_crouch");
        } else if (!isGrounded) {
            sprite = am.getImage("player_jump");
        } else {
            // 8-directional aiming sprite based on aimAngle
            String dirKey = am.get8DirectionKey(aimAngle);
            sprite = am.getImage("player_aim_" + dirKey);
        }

        if (sprite != null) {
            // Draw sprite (centered if needed or matching bounding box)
            g.drawImage(sprite, drawX - 12, drawY - (crouching ? 6 : 6), 64, 64, null);
        } else {
            // Fallback rendering
            g.setColor(new Color(220, 50, 50));
            g.fillRect(drawX, drawY, width, height);
        }
    }

    @Override
    protected void onDeath() {
        SoundManager.getInstance().playGameOver();
    }

    public void addReserveAmmo(int amount) {
        this.reserveAmmo += amount;
    }

    public void addKill() {
        this.kills++;
    }

    // Getters
    public int getCurrentAmmo() { return currentAmmo; }
    public int getMaxMagazine() { return maxMagazine; }
    public int getReserveAmmo() { return reserveAmmo; }
    public boolean isReloading() { return reloading; }
    public double getReloadTimer() { return reloadTimer; }
    public int getKills() { return kills; }
    public double getAimAngle() { return aimAngle; }
}
