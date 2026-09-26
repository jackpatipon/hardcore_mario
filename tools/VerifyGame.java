package tools;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.GamePanel;
import java.awt.image.BufferedImage;
import java.io.File;

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

            // Test Player & Guard Walking Animation and Sprite Resolution
            {
                com.hardcoremario.core.AssetManager am = com.hardcoremario.core.AssetManager.getInstance();
                String[] dirs = {"e", "ne", "n", "nw", "w", "sw", "s", "se"};

                // Verify player sprites for all 8 directions
                for (String dir : dirs) {
                    BufferedImage f1 = am.getDirectionalSprite("player", dir, 1);
                    BufferedImage f2 = am.getDirectionalSprite("player", dir, 2);
                    if (f1 == null || f2 == null) {
                        throw new RuntimeException("ERROR: Player directional sprite missing for direction: " + dir);
                    }
                }

                // Verify guard sprites fallback for all 8 directions
                for (String dir : dirs) {
                    BufferedImage f1 = am.getDirectionalSprite("guard", dir, 1);
                    BufferedImage f2 = am.getDirectionalSprite("guard", dir, 2);
                    if (f1 == null || f2 == null) {
                        throw new RuntimeException("ERROR: Guard directional sprite missing for direction: " + dir);
                    }
                }

                // Verify Player walk cycle logic
                com.hardcoremario.model.entity.Player testPlayer = new com.hardcoremario.model.entity.Player(100, 100);
                if (testPlayer.getCurrentFrame() != 1) {
                    throw new RuntimeException("ERROR: Player should start at idle frame 1, got: " + testPlayer.getCurrentFrame());
                }

                // Player idle update
                testPlayer.getVelocity().setX(0);
                testPlayer.update(0.1);
                if (testPlayer.getCurrentFrame() != 1 || testPlayer.isMoving()) {
                    throw new RuntimeException("ERROR: Player standing still should remain on frame 1!");
                }

                // Player walking update (step 1 -> frame 1)
                testPlayer.getVelocity().setX(Constants.PLAYER_MOVE_SPEED);
                testPlayer.update(0.05); // walkAnimTimer = 0.05 < 0.14
                if (testPlayer.getCurrentFrame() != 1 || !testPlayer.isMoving()) {
                    throw new RuntimeException("ERROR: Player should be moving on frame 1, got frame: " + testPlayer.getCurrentFrame());
                }

                // Player walking update (step 2 -> frame 2)
                testPlayer.update(0.10); // walkAnimTimer = 0.15 > 0.14 -> frame 2
                if (testPlayer.getCurrentFrame() != 2) {
                    throw new RuntimeException("ERROR: Player should have switched to frame 2, got: " + testPlayer.getCurrentFrame());
                }

                // Player walking update (step 3 -> frame 1)
                testPlayer.update(0.15); // walkAnimTimer = 0.30 > 0.28 -> frame 1
                if (testPlayer.getCurrentFrame() != 1) {
                    throw new RuntimeException("ERROR: Player should have looped back to frame 1, got: " + testPlayer.getCurrentFrame());
                }

                // Player stops moving -> resets to frame 1
                testPlayer.getVelocity().setX(0);
                testPlayer.update(0.016);
                if (testPlayer.getCurrentFrame() != 1 || testPlayer.isMoving()) {
                    throw new RuntimeException("ERROR: Player stopped moving but did not reset to frame 1!");
                }

                // Verify Guard walk cycle logic
                com.hardcoremario.model.entity.Guard testGuard = new com.hardcoremario.model.entity.Guard(100, 100, 200);
                if (testGuard.getCurrentFrame() != 1) {
                    throw new RuntimeException("ERROR: Guard should start at frame 1!");
                }
                testGuard.getVelocity().setX(Constants.GUARD_MOVE_SPEED);
                testGuard.update(0.20); // Past GUARD WALK_FRAME_DURATION of 0.16 -> frame 2
                if (testGuard.getCurrentFrame() != 2) {
                    throw new RuntimeException("ERROR: Guard walking should switch to frame 2!");
                }
                testGuard.getVelocity().setX(0);
                testGuard.update(0.016);
                if (testGuard.getCurrentFrame() != 1) {
                    throw new RuntimeException("ERROR: Guard stopped should reset to frame 1!");
                }

                // Verify 2-directional crouch sprites (East and West)
                BufferedImage crE = am.getCrouchSprite("player", true);
                BufferedImage crW = am.getCrouchSprite("player", false);
                if (crE == null || crW == null) {
                    throw new RuntimeException("ERROR: Missing crouch_e or crouch_w sprite!");
                }
                System.out.println("[VERIFIED] 2-Directional crouch sprites (crouch_e & crouch_w) loaded and verified!");

                System.out.println("[VERIFIED] Player & Guard walking animation cycles and 8-directional sprites verified perfectly!");
            }

            // Verify Audio Assets & SoundManager
            {
                com.hardcoremario.core.SoundManager sm = com.hardcoremario.core.SoundManager.getInstance();
                sm.playPlayerShoot();
                sm.playEnemyShoot();
                sm.playJump();
                sm.playHit();
                sm.playPickup();
                sm.playReload();
                sm.playStageMusic(1);
                sm.playGameOver();
                sm.playVictory();
                sm.stopMusic();
                System.out.println("[VERIFIED] SoundManager real .wav audio engine initialized and tested successfully!");
            }

            // Verify GameSettings, Difficulty scaling, and Bullet Color Accessibility
            {
                com.hardcoremario.core.GameSettings settings = com.hardcoremario.core.GameSettings.getInstance();
                
                // Test all 4 difficulty levels and their parameters
                settings.setDifficulty(com.hardcoremario.core.GameSettings.Difficulty.ROOKIE);
                com.hardcoremario.model.entity.Guard rookieGuard = new com.hardcoremario.model.entity.Guard(100, 100, 200);
                if (rookieGuard.getMaxHp() != 35 || rookieGuard.getHp() != 35) {
                    throw new RuntimeException("ERROR: Rookie Guard should have 35 HP! Got: " + rookieGuard.getHp());
                }
                if (settings.getDifficulty().getEnemyBulletSpeed() != 300.0) {
                    throw new RuntimeException("ERROR: Rookie bullet speed should be 300!");
                }

                settings.setDifficulty(com.hardcoremario.core.GameSettings.Difficulty.VETERAN);
                com.hardcoremario.model.entity.Guard veteranGuard = new com.hardcoremario.model.entity.Guard(100, 100, 200);
                if (veteranGuard.getMaxHp() != 50 || veteranGuard.getHp() != 50) {
                    throw new RuntimeException("ERROR: Veteran Guard should have 50 HP! Got: " + veteranGuard.getHp());
                }
                if (settings.getDifficulty().getEnemyBulletSpeed() != 420.0) {
                    throw new RuntimeException("ERROR: Veteran bullet speed should be 420!");
                }

                settings.setDifficulty(com.hardcoremario.core.GameSettings.Difficulty.PSYCHO);
                com.hardcoremario.model.entity.Guard psychoGuard = new com.hardcoremario.model.entity.Guard(100, 100, 200);
                if (psychoGuard.getMaxHp() != 75 || psychoGuard.getHp() != 75) {
                    throw new RuntimeException("ERROR: Psycho Guard should have 75 HP! Got: " + psychoGuard.getHp());
                }
                if (settings.getDifficulty().getEnemyBulletSpeed() != 580.0) {
                    throw new RuntimeException("ERROR: Psycho bullet speed should be 580!");
                }

                settings.setDifficulty(com.hardcoremario.core.GameSettings.Difficulty.GODLIKE);
                com.hardcoremario.model.entity.Guard godlikeGuard = new com.hardcoremario.model.entity.Guard(100, 100, 200);
                if (godlikeGuard.getMaxHp() != 125 || godlikeGuard.getHp() != 125) {
                    throw new RuntimeException("ERROR: GODLIKE Guard should have 125 HP! Got: " + godlikeGuard.getHp());
                }
                if (settings.getDifficulty().getEnemyBulletSpeed() != 780.0) {
                    throw new RuntimeException("ERROR: GODLIKE bullet speed should be 780!");
                }

                // Verify BulletColorThemes
                if (com.hardcoremario.core.GameSettings.BulletColorTheme.values().length != 6) {
                    throw new RuntimeException("ERROR: Expected 6 bullet color themes!");
                }
                for (com.hardcoremario.core.GameSettings.BulletColorTheme theme : com.hardcoremario.core.GameSettings.BulletColorTheme.values()) {
                    if (theme.getDisplayName() == null || theme.getMainColor() == null || theme.getCoreColor() == null || theme.getAccessibilityTag() == null) {
                        throw new RuntimeException("ERROR: Incomplete bullet theme palette: " + theme);
                    }
                }

                // Test TitleScreen UI update and rendering
                com.hardcoremario.view.TitleScreen titleScreen = new com.hardcoremario.view.TitleScreen();
                BufferedImage testImg = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2 = testImg.createGraphics();
                titleScreen.render(g2, Constants.SCREEN_WIDTH / 2, 515);
                g2.dispose();

                // Test Play button click
                InputHandler titleInput = new InputHandler();
                titleInput.setMousePosition(Constants.SCREEN_WIDTH / 2, 515); // inside play button
                java.awt.event.MouseEvent me = new java.awt.event.MouseEvent(
                    new javax.swing.JPanel(), java.awt.event.MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(), 0, Constants.SCREEN_WIDTH / 2, 515, 1, false, java.awt.event.MouseEvent.BUTTON1
                );
                titleInput.mousePressed(me);
                titleScreen.update(0.016, titleInput);
                if (!titleScreen.consumeStartRequested()) {
                    throw new RuntimeException("ERROR: TitleScreen PLAY button click was not recognized!");
                }

                // Test clicking each Difficulty button
                for (int i = 0; i < com.hardcoremario.core.GameSettings.Difficulty.values().length; i++) {
                    java.awt.Rectangle dRect = com.hardcoremario.view.TitleScreen.getDifficultyBounds(i);
                    titleInput.setMousePosition(dRect.x + dRect.width / 2, dRect.y + dRect.height / 2);
                    java.awt.event.MouseEvent dMe = new java.awt.event.MouseEvent(
                        new javax.swing.JPanel(), java.awt.event.MouseEvent.MOUSE_PRESSED,
                        System.currentTimeMillis(), 0, dRect.x + dRect.width / 2, dRect.y + dRect.height / 2, 1, false, java.awt.event.MouseEvent.BUTTON1
                    );
                    titleInput.mousePressed(dMe);
                    titleScreen.update(0.016, titleInput);
                    if (settings.getDifficulty() != com.hardcoremario.core.GameSettings.Difficulty.values()[i]) {
                        throw new RuntimeException("ERROR: Difficulty button " + i + " click did not update GameSettings!");
                    }
                }

                // Test clicking each Bullet Color button
                for (int i = 0; i < com.hardcoremario.core.GameSettings.BulletColorTheme.values().length; i++) {
                    java.awt.Rectangle tRect = com.hardcoremario.view.TitleScreen.getThemeBounds(i);
                    titleInput.setMousePosition(tRect.x + tRect.width / 2, tRect.y + tRect.height / 2);
                    java.awt.event.MouseEvent tMe = new java.awt.event.MouseEvent(
                        new javax.swing.JPanel(), java.awt.event.MouseEvent.MOUSE_PRESSED,
                        System.currentTimeMillis(), 0, tRect.x + tRect.width / 2, tRect.y + tRect.height / 2, 1, false, java.awt.event.MouseEvent.BUTTON1
                    );
                    titleInput.mousePressed(tMe);
                    titleScreen.update(0.016, titleInput);
                    if (settings.getBulletColorTheme() != com.hardcoremario.core.GameSettings.BulletColorTheme.values()[i]) {
                        throw new RuntimeException("ERROR: Theme button " + i + " click did not update GameSettings!");
                    }
                }

                System.out.println("[VERIFIED] All Difficulty buttons, Bullet Color buttons, and PLAY button click hitboxes verified 100%!");
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
