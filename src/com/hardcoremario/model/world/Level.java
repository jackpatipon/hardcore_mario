package com.hardcoremario.model.world;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.core.Renderable;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.core.Updatable;
import com.hardcoremario.model.entity.Enemy;
import com.hardcoremario.model.entity.Guard;
import com.hardcoremario.model.entity.Player;
import com.hardcoremario.model.item.AmmoPack;
import com.hardcoremario.model.item.HealthPack;
import com.hardcoremario.model.item.Item;
import com.hardcoremario.model.projectile.EnemyBullet;
import com.hardcoremario.model.projectile.PlayerBullet;
import com.hardcoremario.model.projectile.Projectile;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.ParticleSystem;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Level encapsulates all game entities, platforms, combat logic, and win/loss state.
 * Demonstrates Aggregation and High-level System Management in OOP.
 */
public class Level implements Updatable, Renderable {
    private final double width;
    private final double height;

    private Player player;
    private final List<Tile> tiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    private boolean missionComplete = false;
    private boolean gameOver = false;

    public Level(double width, double height) {
        this.width = width;
        this.height = height;
        buildLevelLayout();
    }

    private void buildLevelLayout() {
        tiles.clear();
        enemies.clear();
        projectiles.clear();
        items.clear();
        ParticleSystem.getInstance().clear();
        missionComplete = false;
        gameOver = false;

        // Player spawn in underground lab
        player = new Player(120, height - 160);

        int ts = Constants.TILE_SIZE;
        int cols = (int) (width / ts);

        // 1. Solid Ground along the entire level
        for (int c = 0; c < cols; c++) {
            // Gap / pit around column 26..28 and 48..50 for jumping challenge
            if ((c >= 25 && c <= 27) || (c >= 48 && c <= 50)) {
                // Pit with spikes at the bottom!
                tiles.add(new Tile(c * ts, height - ts, ts, ts, Tile.TileType.SPIKES));
                continue;
            }
            tiles.add(new Tile(c * ts, height - ts, ts, ts, Tile.TileType.GROUND));
            tiles.add(new Tile(c * ts, height - ts * 2, ts, ts, Tile.TileType.GROUND));
        }

        // Left Boundary Wall
        for (int r = 0; r < (height / ts); r++) {
            tiles.add(new Tile(0, r * ts, ts, ts, Tile.TileType.METAL));
        }

        // Section 1: Starting Lab Obstacles & Floating Platforms
        addPlatform(260, height - ts * 5, 4);
        addPlatform(520, height - ts * 7, 5);
        addPlatform(800, height - ts * 4, 3);
        tiles.add(new Tile(440, height - ts * 3, ts, ts, Tile.TileType.BARREL));

        // Guards in Section 1
        enemies.add(new Guard(550, height - ts * 8, 120));
        enemies.add(new Guard(820, height - ts * 5, 80));
        enemies.add(new Guard(680, height - ts * 3, 140));

        // Items in Section 1
        items.add(new HealthPack(320, height - ts * 6));
        items.add(new AmmoPack(580, height - ts * 8));

        // Section 2: Elevated Combat Walkway across Pit 1
        addPlatform(1150, height - ts * 5, 6);
        addPlatform(1480, height - ts * 7, 4);
        addPlatform(1720, height - ts * 4, 5);
        tiles.add(new Tile(1280, height - ts * 6, ts, ts, Tile.TileType.BARREL));
        tiles.add(new Tile(1800, height - ts * 5, ts, ts, Tile.TileType.BARREL));

        // Guards in Section 2
        enemies.add(new Guard(1200, height - ts * 6, 160));
        enemies.add(new Guard(1520, height - ts * 8, 100));
        enemies.add(new Guard(1760, height - ts * 5, 140));
        enemies.add(new Guard(1400, height - ts * 3, 150));

        // Items in Section 2
        items.add(new HealthPack(1500, height - ts * 8));
        items.add(new AmmoPack(1750, height - ts * 5));

        // Section 3: High Security Facility before Exit
        addPlatform(2050, height - ts * 6, 4);
        addPlatform(2300, height - ts * 8, 5);
        addPlatform(2600, height - ts * 5, 6);
        addPlatform(2880, height - ts * 4, 4);

        // Heavy Guard squad
        enemies.add(new Guard(2100, height - ts * 7, 100));
        enemies.add(new Guard(2350, height - ts * 9, 140));
        enemies.add(new Guard(2650, height - ts * 6, 180));
        enemies.add(new Guard(2920, height - ts * 5, 100));
        enemies.add(new Guard(2700, height - ts * 3, 200));

        // Final Items
        items.add(new HealthPack(2340, height - ts * 9));
        items.add(new AmmoPack(2660, height - ts * 6));
        items.add(new HealthPack(2900, height - ts * 5));

        // Extraction Portal (Exit) at the end of the lab
        tiles.add(new Tile(width - 240, height - ts * 4, ts * 2, ts * 2, Tile.TileType.EXIT));

        // Right Boundary Wall
        for (int r = 0; r < (height / ts); r++) {
            tiles.add(new Tile(width - ts, r * ts, ts, ts, Tile.TileType.METAL));
        }
    }

    private void addPlatform(double x, double y, int tileCount) {
        int ts = Constants.TILE_SIZE;
        for (int i = 0; i < tileCount; i++) {
            tiles.add(new Tile(x + i * ts, y, ts, ts, Tile.TileType.PLATFORM));
        }
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
            return;
        }

        // 1. Update player physics
        player.update(deltaTime);
        player.updatePhysicsAndCollisions(this, deltaTime);

        // Check if player fell out of bounds
        if (player.getY() > height + 100) {
            player.takeDamage(100);
            gameOver = true;
            return;
        }

        // 2. Update enemies
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            if (enemy.isDead()) {
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

        // 3. Update projectiles and projectile collisions
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update(deltaTime);

            if (!p.isActive()) {
                projIt.remove();
                continue;
            }

            // Check collision with solid tiles
            boolean hitTile = false;
            for (Tile tile : tiles) {
                if (tile.isSolid() && p.collidesWith(tile)) {
                    hitTile = true;
                    ParticleSystem.getInstance().spawnSparks(p.getX(), p.getY(), Color.YELLOW);
                    break;
                }
            }
            if (hitTile) {
                p.setActive(false);
                projIt.remove();
                continue;
            }

            // Check PlayerBullet vs Enemies
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

            // Check EnemyBullet vs Player
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

        // 4. Update and check item pickups
        Iterator<Item> itemIt = items.iterator();
        while (itemIt.hasNext()) {
            Item item = itemIt.next();
            item.update(deltaTime);
            if (player.collidesWith(item)) {
                if (item.onPickup(player)) {
                    itemIt.remove();
                }
            }
        }

        // 5. Check hazard tiles (spikes) vs Player
        for (Tile tile : tiles) {
            if (tile.isHazard() && player.collidesWith(tile)) {
                player.takeDamage(20);
                player.getVelocity().setY(-350); // Bounce off spikes
            }
            if (tile.getType() == Tile.TileType.EXIT && player.collidesWith(tile)) {
                missionComplete = true;
                SoundManager.getInstance().playVictory();
            }
        }

        // 6. Update particles
        ParticleSystem.getInstance().update(deltaTime);
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        // Render tiles
        for (Tile tile : tiles) {
            // Cull offscreen tiles
            if (tile.getX() + tile.getWidth() >= offsetX && tile.getX() <= offsetX + Constants.SCREEN_WIDTH &&
                tile.getY() + tile.getHeight() >= offsetY && tile.getY() <= offsetY + Constants.SCREEN_HEIGHT) {
                tile.render(g, offsetX, offsetY);
            }
        }

        // Render items
        for (Item item : items) {
            item.render(g, offsetX, offsetY);
        }

        // Render enemies
        for (Enemy enemy : enemies) {
            enemy.render(g, offsetX, offsetY);
        }

        // Render player
        player.render(g, offsetX, offsetY);

        // Render projectiles
        for (Projectile p : projectiles) {
            p.render(g, offsetX, offsetY);
        }

        // Render particles
        ParticleSystem.getInstance().render(g, offsetX, offsetY);
    }

    public void reset() {
        buildLevelLayout();
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
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isMissionComplete() { return missionComplete; }
    public boolean isGameOver() { return gameOver; }
}
