package com.hardcoremario.view;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.core.Camera;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * GamePanel handles the visual rendering pipeline, parallax backgrounds, and input events.
 */
public class GamePanel extends JPanel {
    private final Level level;
    private final Camera camera;
    private final InputHandler inputHandler;
    private final HUD hud;
    private boolean paused = false;

    public GamePanel(Level level, Camera camera, InputHandler inputHandler) {
        this.level = level;
        this.camera = camera;
        this.inputHandler = inputHandler;
        this.hud = new HUD();

        setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        setFocusable(true);
        setBackground(Color.BLACK);

        // Add listeners
        addKeyListener(inputHandler);
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);

        // Hide default mouse cursor to use in-game crosshair
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);

        // Snap camera immediately to player spawn within level bounds
        camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
        camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
    }

    public void updateGame(double deltaTime) {
        if (inputHandler.consumeToggleHelp()) {
            hud.toggleHelp();
        }

        if (inputHandler.consumePause()) {
            paused = !paused;
        }

        // Restart check
        if ((level.isGameOver() || level.isMissionComplete()) && inputHandler.consumeRestart()) {
            level.reset();
            camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
            return;
        }

        if (!paused) {
            if (level.consumeStageChanged()) {
                camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
                camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
            }

            level.handleInput(inputHandler, camera, deltaTime);
            level.update(deltaTime);
            camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            camera.update(level.getPlayer().getCenterX(), level.getPlayer().getCenterY(), deltaTime);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double camX = camera.getX();
        double camY = camera.getY();

        // 1. Render Parallax Backgrounds
        renderBackgrounds(g2d, camX, camY);

        // 2. Render Level (Tiles, Enemies, Player, Bullets, Items, Particles)
        level.render(g2d, camX, camY);

        // 3. Render HUD & UI (HP, Ammo, Crosshair, Overlays)
        hud.render(g2d, level, inputHandler);

        // 4. Render Pause Overlay if paused
        if (paused) {
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 42));
            String pText = "PAUSED";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(pText, (getWidth() - fm.stringWidth(pText)) / 2, getHeight() / 2);
        }
    }

    private void renderBackgrounds(Graphics2D g, double camX, double camY) {
        AssetManager am = AssetManager.getInstance();
        BufferedImage bgFar = am.getImage("bg_far");
        BufferedImage bgMain = am.getImage("bg_main");

        // Distant background scrolls at 20% speed
        if (bgFar != null) {
            int w = bgFar.getWidth();
            int farOffsetX = (int) (-(camX * 0.2) % w);
            while (farOffsetX > 0) farOffsetX -= w;
            for (int x = farOffsetX; x < getWidth() + w; x += w) {
                g.drawImage(bgFar, x, 0, w, getHeight(), null);
            }
        }

        // Main facility background scrolls at 50% speed
        if (bgMain != null) {
            int w = bgMain.getWidth();
            int mainOffsetX = (int) (-(camX * 0.5) % w);
            while (mainOffsetX > 0) mainOffsetX -= w;
            for (int x = mainOffsetX; x < getWidth() + w; x += w) {
                g.drawImage(bgMain, x, 0, w, getHeight(), null);
            }
        }
    }
}
