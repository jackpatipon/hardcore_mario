package tools;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.GamePanel;
import java.awt.image.BufferedImage;

public class VerifyGame {
    public static void main(String[] args) {
        System.out.println("Starting automated verification of Hardcore Mario...");
        try {
            double levelWidth = 3200.0;
            double levelHeight = Constants.SCREEN_HEIGHT;

            Level level = new Level(levelWidth, levelHeight);
            Camera camera = new Camera(levelWidth, levelHeight);
            InputHandler input = new InputHandler();
            GamePanel panel = new GamePanel(level, camera, input);

            // Test 60 simulation frames
            for (int i = 0; i < 60; i++) {
                panel.updateGame(1.0 / 60.0);
            }

            // Test rendering to an offscreen BufferedImage
            BufferedImage testBuffer = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            panel.paint(testBuffer.getGraphics());

            System.out.println("[VERIFIED] Game logic, physics, asset loading, and rendering executed with ZERO errors!");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
