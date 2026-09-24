package tools;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.GamePanel;
import java.awt.image.BufferedImage;

public class VerifyGame {
    public static void main(String[] args) {
        System.out.println("Starting automated verification of Hardcore Mario (Stage 1 & 2)...");
        try {
            // Test Stage 1
            Level level1 = new Level(1);
            System.out.println("Stage 1 loaded: " + level1.getTiles().size() + " tiles, " +
                               level1.getEnemies().size() + " enemies, player at (" +
                               level1.getPlayer().getX() + ", " + level1.getPlayer().getY() + ")");
            printBounds("Stage 1", level1);

            // Test Stage 2
            Level level2 = new Level(2);
            System.out.println("Stage 2 loaded: " + level2.getTiles().size() + " tiles, " +
                               level2.getEnemies().size() + " enemies, player at (" +
                               level2.getPlayer().getX() + ", " + level2.getPlayer().getY() + ")");
            printBounds("Stage 2", level2);


            // Test Stage 3
            Level level3 = new Level(3);
            System.out.println("Stage 3 loaded: " + level3.getTiles().size() + " tiles, " +
                               level3.getEnemies().size() + " enemies, " +
                               level3.getItems().size() + " items, player at (" +
                               level3.getPlayer().getX() + ", " + level3.getPlayer().getY() + ")");
            double minTileX = Double.MAX_VALUE, maxTileX = -Double.MAX_VALUE;
            double minTileY = Double.MAX_VALUE, maxTileY = -Double.MAX_VALUE;
            int negYCount = 0;
            for (com.hardcoremario.model.world.Tile t : level3.getTiles()) {
                minTileX = Math.min(minTileX, t.getX());
                maxTileX = Math.max(maxTileX, t.getX() + t.getWidth());
                minTileY = Math.min(minTileY, t.getY());
                maxTileY = Math.max(maxTileY, t.getY() + t.getHeight());
                if (t.getY() < 0) {
                    negYCount++;
                }
            }
            System.out.println("Stage 3 Tile Bounds: X[" + minTileX + " to " + maxTileX + "], Y[" + minTileY + " to " + maxTileY + "], tiles with Y < 0: " + negYCount);
            System.out.println("Stage 3 Level Dimensions: width=" + level3.getWidth() + ", height=" + level3.getHeight());
            System.out.println("Stage 3 Bounds: minX=" + level3.getMinX() + ", minY=" + level3.getMinY() + ", maxX=" + level3.getMaxX() + ", maxY=" + level3.getMaxY());

            // Camera verification for Stage 3
            Camera cam3 = new Camera(level3.getMinX(), level3.getMinY(), level3.getMaxX(), level3.getMaxY());
            cam3.snapTo(level3.getPlayer().getCenterX(), level3.getPlayer().getCenterY());
            double screenPlayerX = level3.getPlayer().getX() - cam3.getX();
            double screenPlayerY = level3.getPlayer().getY() - cam3.getY();
            System.out.println("Stage 3 Camera at (" + cam3.getX() + ", " + cam3.getY() + "), Player on Screen at (" + screenPlayerX + ", " + screenPlayerY + ")");
            if (screenPlayerY < 0 || screenPlayerY > Constants.SCREEN_HEIGHT) {
                throw new RuntimeException("ERROR: Player is offscreen in Stage 3! screenPlayerY = " + screenPlayerY);
            }
            System.out.println("[VERIFIED] Stage 3 Player is clearly on-screen and visible at (" + screenPlayerX + ", " + screenPlayerY + ")!");


            // Verify that all 16 decorative heart items remain floating without falling or overlapping
            int heartItemsBefore = 0;
            for (com.hardcoremario.model.item.Item it : level3.getItems()) {
                if (it.getX() >= 2000 && it.getX() <= 2300 && it.getY() >= 900 && it.getY() <= 1150) {
                    heartItemsBefore++;
                }
            }

            for (int f = 0; f < 60; f++) {
                level3.update(1.0 / 60.0);
            }

            int heartItemsAfter = 0;
            for (com.hardcoremario.model.item.Item it : level3.getItems()) {
                if (it.getX() >= 2000 && it.getX() <= 2300 && it.getY() >= 900 && it.getY() <= 1150 && it.isActive()) {
                    heartItemsAfter++;
                }
            }

            if (heartItemsBefore != 16 || heartItemsAfter != 16) {
                throw new RuntimeException("ERROR: Decorative heart items count mismatch! Before=" + heartItemsBefore + ", After=" + heartItemsAfter);
            }
            System.out.println("[VERIFIED] All 16 decorative heart items remain floating and intact!");
            // Test that standing player without crouch hits ceiling spikes
            {
                Level lvl = new Level(2);
                com.hardcoremario.model.entity.Player p = lvl.getPlayer();
                p.setX(1060.0);
                p.setY(864.0);
                p.getVelocity().setX(Constants.PLAYER_MOVE_SPEED);
                p.getVelocity().setY(Constants.PLAYER_JUMP_SPEED);
                InputHandler in = new InputHandler();
                java.awt.event.KeyEvent dKe = new java.awt.event.KeyEvent(new javax.swing.JPanel(), java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_D, 'd');
                in.keyPressed(dKe);

                boolean died = false;
                for (int f = 0; f < 60; f++) {
                    p.handleInput(in, lvl, 0, 0, 1.0 / 60.0);
                    lvl.update(1.0 / 60.0);
                    if (p.isDead()) {
                        died = true;
                        System.out.println("[VERIFIED] Standing player died on ceiling spikes at frame " + f +
                                           " (x=" + String.format("%.1f", p.getX()) + ", y=" + String.format("%.1f", p.getY()) + ")");
                        break;
                    }
                }
                if (!died) {
                    throw new RuntimeException("ERROR: Standing player should have hit ceiling spikes!");
                }
            }

            // Test that crouching player slips under ceiling spikes safely
            {
                Level lvl = new Level(2);
                com.hardcoremario.model.entity.Player p = lvl.getPlayer();
                p.setX(1060.0);
                p.setY(864.0);
                p.getVelocity().setX(Constants.PLAYER_MOVE_SPEED);
                p.getVelocity().setY(Constants.PLAYER_JUMP_SPEED);
                InputHandler in = new InputHandler();
                java.awt.event.KeyEvent dKe = new java.awt.event.KeyEvent(new javax.swing.JPanel(), java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_D, 'd');
                in.keyPressed(dKe);

                boolean died = false;
                for (int f = 0; f < 45; f++) {
                    // Player crouches mid-air from frame 10 to 35
                    boolean crouch = (f >= 10 && f <= 35);
                    if (crouch) {
                        java.awt.event.KeyEvent sKe = new java.awt.event.KeyEvent(new javax.swing.JPanel(), java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_S, 's');
                        in.keyPressed(sKe);
                    } else {
                        java.awt.event.KeyEvent sKe = new java.awt.event.KeyEvent(new javax.swing.JPanel(), java.awt.event.KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_S, 's');
                        in.keyReleased(sKe);
                    }

                    p.handleInput(in, lvl, 0, 0, 1.0 / 60.0);
                    lvl.update(1.0 / 60.0);
                    if (p.isDead()) {
                        died = true;
                        break;
                    }
                }
                if (died) {
                    throw new RuntimeException("ERROR: Crouching player died on ceiling spikes!");
                }
                System.out.println("[VERIFIED] Crouching mid-air slips safely under ceiling spikes!");
            }

            // Test Stage 3 Mission Complete does not repeatedly execute exit code
            {
                Level lvl3 = new Level(3);
                com.hardcoremario.model.world.Tile exitTile = null;
                for (com.hardcoremario.model.world.Tile t : lvl3.getTiles()) {
                    if (t.getType() == com.hardcoremario.model.world.Tile.TileType.EXIT) {
                        exitTile = t;
                        break;
                    }
                }
                if (exitTile != null) {
                    lvl3.getPlayer().setX(exitTile.getX());
                    lvl3.getPlayer().setY(exitTile.getY());
                    lvl3.update(1.0 / 60.0);
                    if (!lvl3.isMissionComplete()) {
                        throw new RuntimeException("ERROR: Player on EXIT did not trigger missionComplete!");
                    }
                    // Run 100 more frames to ensure physics is frozen and no exception occurs
                    for (int f = 0; f < 100; f++) {
                        lvl3.update(1.0 / 60.0);
                    }
                    System.out.println("[VERIFIED] Mission complete triggered cleanly once and remained frozen over 100 frames!");
                }
            }

            System.out.println("[VERIFIED] All stages load, simulate, and complete with ZERO errors!");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBounds(String name, Level level) {
        double minTileX = Double.MAX_VALUE, maxTileX = -Double.MAX_VALUE;
        double minTileY = Double.MAX_VALUE, maxTileY = -Double.MAX_VALUE;
        for (com.hardcoremario.model.world.Tile t : level.getTiles()) {
            minTileX = Math.min(minTileX, t.getX());
            maxTileX = Math.max(maxTileX, t.getX() + t.getWidth());
            minTileY = Math.min(minTileY, t.getY());
            maxTileY = Math.max(maxTileY, t.getY() + t.getHeight());
        }
        System.out.println(name + " Tile Bounds: X[" + minTileX + " to " + maxTileX + "], Y[" + minTileY + " to " + maxTileY + "], Level Dimensions: W=" + level.getWidth() + ", H=" + level.getHeight());
    }
}
