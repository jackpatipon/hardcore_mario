package com.hardcoremario.model.world;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.core.Renderable;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.core.Updatable;
import com.hardcoremario.model.entity.Enemy;
import com.hardcoremario.model.entity.Guard;
import com.hardcoremario.model.entity.Player;
import com.hardcoremario.model.item.Item;
import com.hardcoremario.model.projectile.EnemyBullet;
import com.hardcoremario.model.projectile.PlayerBullet;
import com.hardcoremario.model.projectile.Projectile;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Level manages all game objects, physics updates, combat, hazards, and stage progression.
 * Loads layout directly from draw.io XML files.
 */
public class Level implements Updatable, Renderable {
    private double minX = 0.0;
    private double minY = 0.0;
    private double maxX = 3600.0;
    private double maxY = 1400.0;
    private double width;
    private double height;
    private int currentStage = 1;
    private boolean stageChanged = true;

    private Player player;
    private final List<Tile> tiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    private boolean missionComplete = false;
    private boolean gameOver = false;
    private String statusMessage = "";

    public Level(int stageNumber) {
        this.currentStage = stageNumber;
        loadStage(stageNumber);
    }

    public void loadStage(int stage) {
        this.currentStage = stage;
        tiles.clear();
        enemies.clear();
        projectiles.clear();
        items.clear();
        ParticleSystem.getInstance().clear();
        missionComplete = false;
        gameOver = false;

        File stageF = DrawioLevelLoader.findStageFile(stage);
        String stageFile = (stageF != null) ? stageF.getPath() : "assets/levels/hardcore_mario_stage" + stage + ".drawio.xml";
        DrawioLevelLoader.LevelData data = DrawioLevelLoader.loadLevel(stageFile);

        this.minX = data.minX;
        this.minY = data.minY;
        this.maxX = data.maxX;
        this.maxY = data.maxY;
        this.width = data.width;
        this.height = data.height;
        this.stageChanged = true;

        // Spawn player from XML coordinates
        this.player = new Player(data.playerSpawnX, data.playerSpawnY);

        this.tiles.addAll(data.tiles);
        this.enemies.addAll(data.enemies);
        this.items.addAll(data.items);

        statusMessage = "STAGE " + currentStage + " START!";
    }

    public void handleInput(InputHandler input, Camera camera, double deltaTime) {
        if (!gameOver && !missionComplete) {
            player.handleInput(input, this, camera.getX(), camera.getY(), deltaTime);
        }
    }

    @Override
    public void update(double deltaTime) {
        if (player.isDead()) {
            gameOver = true;
        }

        // Freeze physics and prevent repetitive sound/trigger loops once level is ended
        if (gameOver || missionComplete) {
            ParticleSystem.getInstance().update(deltaTime);
            return;
        }

        // 1. Update Player Physics and Tile Collisions
        player.update(deltaTime);
        player.updatePhysicsAndCollisions(this, deltaTime);

        // Instant death if fallen out of world bounds
        if (player.getY() > maxY + 150) {
            player.takeDamage(99999);
            gameOver = true;
            return;
        }

        // Check if Player touches Spikes (Instant Death!)
        for (Tile tile : tiles) {
            if (tile.isActive() && tile.isHazard() && player.getHitbox().intersects(tile.getHitbox())) {
                player.takeDamage(99999); // Instant Kill!
                ParticleSystem.getInstance().spawnSparks(player.getCenterX(), player.getCenterY(), Color.RED);
                gameOver = true;
                return;
            }
        }

        // 2. Update Enemies (AI, Gravity, Physics, and Spike Collisions)
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            if (enemy.isDead() || !enemy.isActive()) {
                enemy.dropLoot(this);
                player.addKill();
                enemyIt.remove();
                continue;
            }
            enemy.updateAI(player, this, deltaTime);
            enemy.update(deltaTime);
            if (enemy instanceof Guard) {
                ((Guard) enemy).updatePhysicsAndCollisions(this, deltaTime);
            }
        }

        // 3. Update Items (Gravity, Physics, and Spike Collisions)
        Iterator<Item> itemIt = items.iterator();
        while (itemIt.hasNext()) {
            Item item = itemIt.next();
            item.update(deltaTime);
            item.updatePhysicsAndCollisions(tiles, deltaTime);

            if (!item.isActive()) {
                itemIt.remove();
                continue;
            }

            // Check if player collects item
            if (player.collidesWith(item)) {
                if (item.onPickup(player)) {
                    itemIt.remove();
                }
            }
        }

        // 4. Update Projectiles & Check Collisions
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update(deltaTime);

            if (!p.isActive()) {
                projIt.remove();
                continue;
            }

            // A. Projectile vs Tiles
            boolean hitTile = false;
            for (Tile tile : tiles) {
                if (!tile.isActive()) continue;

                if (tile.isSolid() && p.collidesWith(tile)) {
                    hitTile = true;

                    // If it's a PlayerBullet hitting a Breakable Block, damage and potentially destroy it!
                    if (p instanceof PlayerBullet && tile.isBreakable()) {
                        tile.takeDamage(p.getDamage());
                        SoundManager.getInstance().playHit();
                    } else {
                        ParticleSystem.getInstance().spawnSparks(p.getX(), p.getY(), Color.YELLOW);
                    }
                    break;
                }
            }
            if (hitTile) {
                p.setActive(false);
                projIt.remove();
                continue;
            }

            // B. PlayerBullet vs Enemies
            if (p instanceof PlayerBullet) {
                boolean hitEnemy = false;
                for (Enemy enemy : enemies) {
                    if (!enemy.isDead() && p.collidesWith(enemy)) {
                        enemy.takeDamage(p.getDamage());
                        ParticleSystem.getInstance().spawnSparks(p.getX(), p.getY(), Color.RED);
                        SoundManager.getInstance().playHit();
                        hitEnemy = true;
                        break;
                    }
                }
                if (hitEnemy) {
                    p.setActive(false);
                    projIt.remove();
                    continue;
                }
            }

            // C. EnemyBullet vs Player
            if (p instanceof EnemyBullet) {
                if (!player.isDead() && p.collidesWith(player)) {
                    player.takeDamage(p.getDamage());
                    ParticleSystem.getInstance().spawnSparks(player.getCenterX(), player.getCenterY(), Color.RED);
                    SoundManager.getInstance().playHit();
                    p.setActive(false);
                    projIt.remove();
                    continue;
                }
            }
        }

        // Remove destroyed tiles
        tiles.removeIf(t -> !t.isActive());

        // 5. Check Level Exit / Stage Progression
        if (!missionComplete) {
            for (Tile tile : tiles) {
                if (tile.getType() == Tile.TileType.EXIT && player.collidesWith(tile)) {
                    // Check if next stage exists
                    int nextStage = currentStage + 1;
                    File nextFile = DrawioLevelLoader.findStageFile(nextStage);
                    if (nextFile != null && nextFile.exists()) {
                        SoundManager.getInstance().playVictory();
                        loadStage(nextStage);
                    } else {
                        // All stages completed!
                        missionComplete = true;
                        SoundManager.getInstance().playVictory();
                        ParticleSystem.getInstance().spawnSparks(player.getCenterX(), player.getCenterY(), Color.YELLOW);
                    }
                    break;
                }
            }
        }

        // 6. Update Particle System
        ParticleSystem.getInstance().update(deltaTime);
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        // Render tiles
        for (Tile tile : tiles) {
            if (!tile.isActive()) continue;
            // Cull offscreen tiles
            if (tile.getX() + tile.getWidth() >= offsetX && tile.getX() <= offsetX + Constants.SCREEN_WIDTH &&
                tile.getY() + tile.getHeight() >= offsetY && tile.getY() <= offsetY + Constants.SCREEN_HEIGHT) {
                tile.render(g, offsetX, offsetY);
            }
        }

        // Render items
        for (Item item : items) {
            if (item.isActive()) {
                item.render(g, offsetX, offsetY);
            }
        }

        // Render enemies
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.render(g, offsetX, offsetY);
            }
        }

        // Render player
        player.render(g, offsetX, offsetY);

        // Render projectiles
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.render(g, offsetX, offsetY);
            }
        }

        // Render particles
        ParticleSystem.getInstance().render(g, offsetX, offsetY);
    }

    public void reset() {
        loadStage(currentStage);
    }

    public void addProjectile(Projectile p) {
        projectiles.add(p);
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public Player getPlayer() { return player; }
    public List<Tile> getTiles() { return tiles; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Item> getItems() { return items; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
    public boolean consumeStageChanged() {
        if (stageChanged) {
            stageChanged = false;
            return true;
        }
        return false;
    }
    public boolean isMissionComplete() { return missionComplete; }
    public boolean isGameOver() { return gameOver; }
    public int getCurrentStage() { return currentStage; }
}
