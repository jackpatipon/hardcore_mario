package tools;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.GamePanel;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PreviewRender {
    public static void main(String[] args) {
        try {
            // 1. Standing Idle Preview (Frame 1)
            Level level = new Level(1);
            Camera camera = new Camera(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            InputHandler input = new InputHandler();
            GamePanel panel = new GamePanel(level, camera, input);
            panel.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            // Step 10 frames with no input (idle standing)
            for (int i = 0; i < 10; i++) {
                panel.updateGame(1.0 / 60.0);
            }

            System.out.println("Idle state -> Player currentFrame: " + level.getPlayer().getCurrentFrame() +
                               ", isMoving: " + level.getPlayer().isMoving());

            BufferedImage imgStanding = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            panel.paint(imgStanding.getGraphics());
            File outStanding = new File("preview_standing.png");
            ImageIO.write(imgStanding, "PNG", outStanding);
            System.out.println("Standing preview saved to " + outStanding.getAbsolutePath());

            // 2. Walking Preview (Frame 2)
            // Move player to the right for 0.20s (past WALK_FRAME_DURATION of 0.14s)
            level.getPlayer().getVelocity().setX(Constants.PLAYER_MOVE_SPEED);
            for (int i = 0; i < 15; i++) {
                // Update player with velocity moving
                level.getPlayer().getVelocity().setX(Constants.PLAYER_MOVE_SPEED);
                level.getPlayer().update(1.0 / 60.0);
            }

            System.out.println("Walking state -> Player currentFrame: " + level.getPlayer().getCurrentFrame() +
                               ", isMoving: " + level.getPlayer().isMoving() +
                               ", walkAnimTimer: " + level.getPlayer().getWalkAnimTimer());

            BufferedImage imgWalking = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            panel.paint(imgWalking.getGraphics());
            File outWalking = new File("preview_walking.png");
            ImageIO.write(imgWalking, "PNG", outWalking);
            System.out.println("Walking preview saved to " + outWalking.getAbsolutePath());

            // 3. Crouching Preview (East and North-East)
            Level crouchLevel = new Level(1);
            Camera crouchCam = new Camera(crouchLevel.getMinX(), crouchLevel.getMinY(), crouchLevel.getMaxX(), crouchLevel.getMaxY());
            InputHandler crouchInput = new InputHandler();
            GamePanel crouchPanel = new GamePanel(crouchLevel, crouchCam, crouchInput);
            crouchPanel.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            // Press S to crouch
            java.awt.Component dummy = new java.awt.Canvas();
            crouchInput.keyPressed(new java.awt.event.KeyEvent(dummy, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_S, 's'));

            // Step game loop so player handles crouch input
            for (int i = 0; i < 5; i++) {
                crouchPanel.updateGame(1.0 / 60.0);
            }

            BufferedImage imgCrouch = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            crouchPanel.paint(imgCrouch.getGraphics());
            int cpx = (int) (crouchLevel.getPlayer().getX() - crouchCam.getX() - 15);
            int cpy = (int) (crouchLevel.getPlayer().getY() - crouchCam.getY() - 40);
            BufferedImage closeCrouch = imgCrouch.getSubimage(cpx, cpy, 70, 70);
            ImageIO.write(closeCrouch, "PNG", new File("preview_closeup_crouch_e.png"));
            System.out.println("Crouching preview saved!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
