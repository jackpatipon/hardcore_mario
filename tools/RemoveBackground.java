package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.imageio.ImageIO;

/**
 * Utility tool to remove solid/near-white backgrounds from sprite images
 * using Flood Fill (BFS from borders) so that internal white elements (eyes, gloves) are preserved.
 */
public class RemoveBackground {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java tools.RemoveBackground <imagePath> [tolerance 0-255, default 20]");
            return;
        }

        String filePath = args[0];
        int tolerance = args.length > 1 ? Integer.parseInt(args[1]) : 20;

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return;
        }

        try {
            BufferedImage src = ImageIO.read(file);
            BufferedImage result = removeWhiteBackground(src, tolerance);
            ImageIO.write(result, "PNG", file);
            System.out.println("[SUCCESS] Removed background from: " + file.getPath());
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static BufferedImage removeWhiteBackground(BufferedImage src, int tolerance) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Copy source into ARGB
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, src.getRGB(x, y));
            }
        }

        boolean[][] visited = new boolean[w][h];
        Queue<int[]> queue = new ArrayDeque<>();

        // Helper to check if pixel is "white/near-white"
        java.util.function.BiPredicate<Integer, Integer> isWhite = (x, y) -> {
            int rgb = out.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            return r >= (255 - tolerance) && g >= (255 - tolerance) && b >= (255 - tolerance);
        };

        // Seed with all border pixels that are near-white
        for (int x = 0; x < w; x++) {
            if (isWhite.test(x, 0)) { queue.add(new int[]{x, 0}); visited[x][0] = true; }
            if (isWhite.test(x, h - 1)) { queue.add(new int[]{x, h - 1}); visited[x][h - 1] = true; }
        }
        for (int y = 0; y < h; y++) {
            if (isWhite.test(0, y) && !visited[0][y]) { queue.add(new int[]{0, y}); visited[0][y] = true; }
            if (isWhite.test(w - 1, y) && !visited[w - 1][y]) { queue.add(new int[]{w - 1, y}); visited[w - 1][y] = true; }
        }

        // BFS flood fill
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!queue.isEmpty()) {
            int[] pt = queue.poll();
            int cx = pt[0];
            int cy = pt[1];

            // Make transparent
            out.setRGB(cx, cy, 0x00000000);

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visited[nx][ny]) {
                    if (isWhite.test(nx, ny)) {
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return out;
    }
}
