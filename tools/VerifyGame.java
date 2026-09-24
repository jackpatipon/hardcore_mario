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

            // Test Stage 2
            Level level2 = new Level(2);
            System.out.println("Stage 2 loaded: " + level2.getTiles().size() + " tiles, " +
                               level2.getEnemies().size() + " enemies, player at (" +
                               level2.getPlayer().getX() + ", " + level2.getPlayer().getY() + ")");
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

            System.out.println("[VERIFIED] Both Stage 1 and Stage 2 load and simulate with ZERO errors!");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
