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
            Level level = new Level(3);
            Camera camera = new Camera(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            InputHandler input = new InputHandler();
            GamePanel panel = new GamePanel(level, camera, input);
            panel.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            // Step a few frames
            for (int i = 0; i < 10; i++) {
                panel.updateGame(1.0 / 60.0);
            }

            BufferedImage img = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            panel.paint(img.getGraphics());

            File out = new File("preview_stage3.png");
            ImageIO.write(img, "PNG", out);
            System.out.println("Preview saved to " + out.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
