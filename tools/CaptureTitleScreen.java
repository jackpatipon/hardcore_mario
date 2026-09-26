package tools;

import com.hardcoremario.core.GameSettings;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.TitleScreen;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CaptureTitleScreen {
    public static void main(String[] args) throws Exception {
        TitleScreen ts = new TitleScreen();
        // Advance animTimer a bit for glowing accents
        ts.update(1.2, new com.hardcoremario.core.InputHandler());

        BufferedImage img = new BufferedImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        ts.render(g2, Constants.SCREEN_WIDTH / 2, 515);
        g2.dispose();

        File out = new File("artifacts/title_screen_preview.png");
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("TitleScreen preview saved to: " + out.getAbsolutePath());
    }
}
