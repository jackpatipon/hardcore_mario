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
    private boolean debugMode = false;

    private double aimAngle = 0.0; // In degrees
    private int kills = 0;

    // Walk animation: 2-frame walking cycle (alternates frames 1 and 2 while walking, locks to frame 1 when idle)
    private double walkAnimTimer = 0.0;
    private int currentFrame = 1;
    public static final double WALK_FRAME_DURATION = 0.14; // Seconds per frame (~7 steps/second)

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

        // Debug Mode: Noclip & Free 8-Directional Flight + Unlimited Ammo
        if (debugMode) {
            double flySpeed = Constants.PLAYER_MOVE_SPEED * 1.6;
            double targetVx = 0.0;
            double targetVy = 0.0;
            if (input.isMoveLeft()) targetVx -= flySpeed;
            if (input.isMoveRight()) targetVx += flySpeed;
            if (input.isJump()) targetVy -= flySpeed;   // W, Space, Up = Fly Up
            if (input.isCrouch()) targetVy += flySpeed; // S, Down = Fly Down

            velocity.setX(targetVx);
            velocity.setY(targetVy);
            isGrounded = false;
            crouching = false;

            // Infinite ammo & continuous shooting without reload delay
            currentAmmo = maxMagazine;
            reserveAmmo = 999;
            reloading = false;

            if (input.isMouseLeftPressed() && shootTimer <= 0) {
                shoot(mouseWorldX, mouseWorldY, level);
            }
            return;
        }

        // 2. Crouch handling (lowers head hitbox downwards towards feet)
        boolean wantsCrouch = input.isCrouch();
        if (wantsCrouch && !crouching) {
            crouching = true;
            int oldHeight = this.height;
            this.height = Constants.PLAYER_CROUCH_HEIGHT;
            // Shift Y downwards so the head ducks down while feet stay grounded / aligned
            position.setY(position.getY() + (oldHeight - this.height));
        } else if (!wantsCrouch && crouching) {
            // Only stand up if there is sufficient headroom (no solid block overhead)
            if (canStandUp(level)) {
                crouching = false;
                int oldHeight = this.height;
                this.height = Constants.PLAYER_HEIGHT;
                // Shift Y upwards so the head raises while feet stay grounded / aligned
                position.setY(position.getY() - (this.height - oldHeight));
            }
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
                // If crouching on ground and player presses jump, uncrouch and jump only if clear overhead
                if (!canStandUp(level)) {
                    return; // Low ceiling blocks jump
                }
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
        if (debugMode) {
            currentAmmo = maxMagazine;
            reserveAmmo = 999;
        } else {
            currentAmmo--;
        }
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

        // Walk animation: alternates frames 1 and 2 while walking, locks to frame 1 when idle
        boolean isMoving = Math.abs(velocity.getX()) > 10.0;
        if (isMoving) {
            walkAnimTimer += deltaTime;
            currentFrame = ((int) (walkAnimTimer / WALK_FRAME_DURATION) % 2) + 1;
        } else {
            walkAnimTimer = 0.0;
            currentFrame = 1;
        }

        // Apply gravity (disabled in debug noclip mode)
        if (!debugMode) {
            velocity.setY(Math.min(velocity.getY() + Constants.GRAVITY * deltaTime, Constants.MAX_FALL_SPEED));
        }
    }

    /**
     * Checks if there is sufficient headroom to stand up from crouching.
     * Prevents clipping or warping into ceiling blocks when releasing crouch under low structures.
     */
    public boolean canStandUp(Level level) {
        if (!crouching) return true;
        double standY = position.getY() - (Constants.PLAYER_HEIGHT - this.height);
        Rectangle2D.Double standBox = new Rectangle2D.Double(position.getX(), standY, width, Constants.PLAYER_HEIGHT);
        for (Tile tile : level.getTiles()) {
            if (!tile.isSolid() || !tile.isActive()) continue;
            if (standBox.intersects(tile.getHitbox())) {
                return false;
            }
        }
        return true;
    }

    public void updatePhysicsAndCollisions(Level level, double deltaTime) {
        if (debugMode) {
            // Noclip: pass freely through all solid blocks and boundaries
            position.setX(position.getX() + velocity.getX() * deltaTime);
            position.setY(position.getY() + velocity.getY() * deltaTime);
            isGrounded = false;
            return;
        }

        // Move X and resolve solid tile collisions
        position.setX(position.getX() + velocity.getX() * deltaTime);
        checkTileCollisionsX(level);

        // Move Y and resolve solid tile collisions
        double prevY = position.getY();
        position.setY(position.getY() + velocity.getY() * deltaTime);
        isGrounded = false;
        checkTileCollisionsY(level, prevY);
    }

    private void checkTileCollisionsX(Level level) {
        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isSolid() || !tile.isActive()) continue;
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

    private void checkTileCollisionsY(Level level, double prevY) {
        Rectangle2D.Double hb = getHitbox();
        for (Tile tile : level.getTiles()) {
            if (!tile.isSolid() || !tile.isActive()) continue;
            if (hb.intersects(tile.getHitbox())) {
                double tileTop = tile.getY();
                double tileBottom = tile.getY() + tile.getHeight();
                double prevBottom = prevY + height;

                // 1. Landing on top of tile:
                // Must be falling downwards or stationary AND feet were previously at or above the tile surface
                if (velocity.getY() >= 0 && prevBottom <= tileTop + 14) {
                    position.setY(tileTop - height);
                    velocity.setY(0);
                    isGrounded = true;
                }
                // 2. Head bumping into ceiling from below:
                else if (velocity.getY() <= 0 || prevY >= tileBottom - 14) {
                    position.setY(tileBottom);
                    velocity.setY(0);
                }
                // 3. Fallback based on shallowest penetration:
                else {
                    double overlapTop = (position.getY() + height) - tileTop;
                    double overlapBottom = tileBottom - position.getY();
                    if (overlapTop < overlapBottom && velocity.getY() >= 0) {
                        position.setY(tileTop - height);
                        velocity.setY(0);
                        isGrounded = true;
                    } else {
                        position.setY(tileBottom);
                        velocity.setY(0);
                    }
                }
                hb = getHitbox();
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (debugMode) return; // Completely immune to damage (อมตะ)
        super.takeDamage(amount);
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        int drawX = (int) (getX() - offsetX);
        int drawY = (int) (getY() - offsetY);

        // Flash opacity when invulnerable
        if (isInvulnerable() && ((int) (System.currentTimeMillis() / 60) % 2 == 0)) {
            return; // Skip rendering frame for blink effect
        }

        Composite originalComp = g.getComposite();
        if (debugMode) {
            // Ethereal cyan aura & ghost translucency for noclip
            g.setColor(new Color(0, 255, 255, 65));
            g.fillOval(drawX - 14, drawY - 10, 68, 68);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        }

        // Select sprite
        BufferedImage sprite;
        AssetManager am = AssetManager.getInstance();

        if (crouching) {
            // 2-directional crouch: crouch_e (right) vs crouch_w (left)
            sprite = am.getCrouchSprite("player", facingRight);

            if (sprite != null) {
                // Adaptive aspect ratio: supports both 64x40 (ratio ~1.6) and 1:1 square (like other directional sprites)
                double aspect = (double) sprite.getWidth() / sprite.getHeight();
                int drawW = 64;
                int drawH = (int) Math.round(64.0 / aspect);
                if (drawH < 32) drawH = 32;
                if (drawH > 64) drawH = 64;
                int feetY = drawY + height;
                int spriteY = feetY - drawH + 2;

                g.drawImage(sprite, drawX - 12, spriteY, drawW, drawH, null);
            } else {
                g.setColor(new Color(220, 50, 50));
                g.fillRect(drawX, drawY, width, height);
            }
        } else {
            // 8-directional aiming sprite based on aimAngle and animation frame
            String dirKey = am.get8DirectionKey(aimAngle);
            int frame = (!isGrounded && Math.abs(velocity.getX()) <= 10.0) ? 2 : currentFrame;
            sprite = am.getDirectionalSprite("player", dirKey, frame);

            if (sprite != null) {
                g.drawImage(sprite, drawX - 12, drawY - 6, 64, 64, null);
            } else {
                g.setColor(new Color(220, 50, 50));
                g.fillRect(drawX, drawY, width, height);
            }
        }

        if (debugMode) {
            g.setComposite(originalComp);
        }
    }


    /**
     * Applies damage from Boss attacks.
     * Uses a brief 0.04s invulnerability window so that all bullets in a rapid trailing stream
     * (spaced 0.08s apart) can register damage (e.g. 6 damage x 3 bullets = 18 total damage),
     * while still preventing multi-collision within the same physics subframe.
     */
    public void takeBossDamage(int amount) {
        if (debugMode || isDead()) return;
        if (invulnerableTimer > 0) return;
        currentHp = Math.max(0, currentHp - amount);
        invulnerableTimer = 0.04;
        if (currentHp <= 0) {
            onDeath();
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
    public int getCurrentFrame() { return currentFrame; }
    public double getWalkAnimTimer() { return walkAnimTimer; }
    public boolean isMoving() { return Math.abs(velocity.getX()) > 10.0; }
    public boolean isCrouching() { return crouching; }
    public boolean isDebugMode() { return debugMode; }

    public void toggleDebugMode() {
        this.debugMode = !this.debugMode;
        if (this.debugMode) {
            this.currentHp = this.maxHp;
            this.currentAmmo = this.maxMagazine;
            this.reserveAmmo = 999;
            this.reloading = false;
            this.velocity.setY(0);
        }
    }
}
