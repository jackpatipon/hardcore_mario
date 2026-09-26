package tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CreateCrouchSprites {
    public static void main(String[] args) {
        try {
            // Sources
            File userCrouchE = new File("assets/characters/player/crouch_e.png");
            File brainDir = new File("C:/Users/patip/.gemini/antigravity-ide/brain/1949128d-e359-4f15-b621-880930aca952");

            BufferedImage crouchE = ImageIO.read(userCrouchE);
            BufferedImage crouchNE = downscale256(ImageIO.read(new File(brainDir, "mario_crouch_ne_1790409522276.jpg")));
            BufferedImage crouchSE = downscale256(ImageIO.read(new File(brainDir, "mario_crouch_se_1790409555787.jpg")));
            BufferedImage crouchN = downscale256(ImageIO.read(new File(brainDir, "mario_crouch_n_1790409582163.jpg")));
            BufferedImage crouchS = downscale256(ImageIO.read(new File(brainDir, "mario_crouch_s_1790409606251.jpg")));

            // Horizontal mirrors
            BufferedImage crouchW = flipHorizontal(crouchE);
            BufferedImage crouchNW = flipHorizontal(crouchNE);
            BufferedImage crouchSW = flipHorizontal(crouchSE);

            // Target directory: assets/characters/player/8directions
            File outDir = new File("assets/characters/player/8directions");
            outDir.mkdirs();

            save(crouchE, new File(outDir, "crouch_e.png"));
            save(crouchNE, new File(outDir, "crouch_ne.png"));
            save(crouchN, new File(outDir, "crouch_n.png"));
            save(crouchNW, new File(outDir, "crouch_nw.png"));
            save(crouchW, new File(outDir, "crouch_w.png"));
            save(crouchSW, new File(outDir, "crouch_sw.png"));
            save(crouchS, new File(outDir, "crouch_s.png"));
            save(crouchSE, new File(outDir, "crouch_se.png"));

            // Also save crouch_w in assets/characters/player/ alongside crouch_e.png
            save(crouchW, new File("assets/characters/player/crouch_w.png"));

            System.out.println("[SUCCESS] Saved all 8 crouching directional sprites (256px resolution) to " + outDir.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage downscale256(BufferedImage src) {
        if (src.getWidth() <= 256 && src.getHeight() <= 256) return src;
        int targetW = 256;
        int targetH = (int) Math.round((double) src.getHeight() * 256.0 / src.getWidth());
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return out;
    }

    private static void save(BufferedImage img, File target) throws Exception {
        ImageIO.write(img, "PNG", target);
        System.out.println("Saved: " + target.getName() + " (" + img.getWidth() + "x" + img.getHeight() + ")");
    }

    public static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, w, 0, -w, h, null);
        g.dispose();
        return out;
    }
}
