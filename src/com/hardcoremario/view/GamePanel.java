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

    public enum GameState {
        TITLE,
        PLAYING
    }

    private GameState gameState = GameState.TITLE;
    private final TitleScreen titleScreen;
    private final Level level;
    private final Camera camera;
    private final InputHandler inputHandler;
    private final HUD hud;
    private final Cursor blankCursor;
    private boolean paused = false;

    public GamePanel(Level level, Camera camera, InputHandler inputHandler) {
        this.level = level;
        this.camera = camera;
        this.inputHandler = inputHandler;
        this.hud = new HUD();
        this.titleScreen = new TitleScreen();

        setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        setFocusable(true);
        setBackground(Color.BLACK);

        // Add listeners
        addKeyListener(inputHandler);
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);

        // Initialize blank cursor for in-game crosshair
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        this.blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

        // If testing a specific stage (stage > 1), skip Title Screen and start playing immediately!
        if (level.getInitialStage() > 1) {
            this.gameState = GameState.PLAYING;
            setCursor(blankCursor);
            com.hardcoremario.core.SoundManager.getInstance().playStageMusic(level.getInitialStage());
        } else {
            this.gameState = GameState.TITLE;
            setCursor(Cursor.getDefaultCursor());
            com.hardcoremario.core.SoundManager.getInstance().playStageMusic(1);
        }

        // Snap camera immediately to player spawn within level bounds
        camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
        camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
    }

    public void updateGame(double deltaTime) {
        if (gameState == GameState.TITLE) {
            int mx = inputHandler.getMouseX();
            int my = inputHandler.getMouseY();
            if (titleScreen.isAnyButtonHovered(mx, my)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }

            titleScreen.update(deltaTime, inputHandler);
            if (titleScreen.consumeStartRequested()) {
                // Start gameplay in configured initial stage (defaults to 1, or stage set in Main)
                level.loadStage(level.getInitialStage());
                camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
                camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
                gameState = GameState.PLAYING;
                setCursor(blankCursor); // Hide OS cursor, use in-game crosshair
            }
            return;
        }

        // --- PLAYING STATE ---
        if (inputHandler.consumeToggleHelp()) {
            hud.toggleHelp();
        }

        if (inputHandler.consumePause()) {
            paused = !paused;
            setCursor(paused ? Cursor.getDefaultCursor() : blankCursor);
        }

        // Cycle bullet color at any time with C
        if (inputHandler.consumeColorCycle()) {
            com.hardcoremario.core.GameSettings.getInstance().cycleNextBulletColor();
            com.hardcoremario.core.SoundManager.getInstance().playPickup();
        }

        // Quick stage jump shortcuts for testing (F1: Stage 1, F2: Stage 2, F3: Stage 3)
        if (inputHandler.consumeF1()) {
            level.loadStage(1);
            camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
        } else if (inputHandler.consumeF2()) {
            level.loadStage(2);
            camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
        } else if (inputHandler.consumeF3()) {
            level.loadStage(3);
            camera.setBounds(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            camera.snapTo(level.getPlayer().getCenterX(), level.getPlayer().getCenterY());
        }

        // Return to main menu if M pressed
        if (inputHandler.consumeMenu()) {
            gameState = GameState.TITLE;
            paused = false;
            setCursor(Cursor.getDefaultCursor());
            com.hardcoremario.core.SoundManager.getInstance().playStageMusic(1);
            return;
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

        // Enable smooth rendering & high quality image scaling
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double camX = camera.getX();
        double camY = camera.getY();

        if (gameState == GameState.TITLE) {
            // Render ambient parallax in background behind menu
            renderBackgrounds(g2d, camX, camY);
            // Render Title Screen Menu UI
            titleScreen.render(g2d, inputHandler.getMouseX(), inputHandler.getMouseY());
            return;
        }

        // 1. Render Parallax Backgrounds
        renderBackgrounds(g2d, camX, camY);

        // 2. Render Level (Tiles, Enemies, Player, Bullets, Items, Particles)
        level.render(g2d, camX, camY);

        // 3. Render HUD & UI (HP, Ammo, Crosshair, Overlays)
        hud.render(g2d, level, inputHandler);

        // 4. Render Pause Overlay if paused
        if (paused) {
            renderPauseOverlay(g2d);
        }
    }

    private static final String THAI_FONT = getThaiFont();

    private static String getThaiFont() {
        String[] candidates = {"Tahoma", "Leelawadee UI", "Microsoft Sans Serif"};
        for (String c : candidates) {
            Font f = new Font(c, Font.PLAIN, 12);
            if (f.canDisplay('ก') && f.canDisplay('ภ')) {
                return c;
            }
        }
        return Font.SANS_SERIF;
    }

    private void renderPauseOverlay(Graphics2D g) {
        g.setColor(new Color(8, 10, 16, 210));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Impact", Font.BOLD, 46));
        String pText = "PAUSED (หยุดชั่วคราว)";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(pText, centerX - fm.stringWidth(pText) / 2, centerY - 90);

        // Current Difficulty Badge
        com.hardcoremario.core.GameSettings.Difficulty d = com.hardcoremario.core.GameSettings.getInstance().getDifficulty();
        g.setFont(new Font(THAI_FONT, Font.BOLD, 15));
        g.setColor(d.getBadgeColor());
        String dText = "ระดับความยาก: " + d.getCodeName() + " (" + d.getLabelThai() + ")";
        FontMetrics fmD = g.getFontMetrics();
        g.drawString(dText, centerX - fmD.stringWidth(dText) / 2, centerY - 40);

        // Current Bullet Color
        com.hardcoremario.core.GameSettings.BulletColorTheme theme = com.hardcoremario.core.GameSettings.getInstance().getBulletColorTheme();
        g.setColor(theme.getMainColor());
        String cText = "สีกระสุนศัตรู: " + theme.getDisplayName() + " [" + theme.getAccessibilityTag() + "]  (กด [C] เพื่อเปลี่ยนสี)";
        FontMetrics fmC = g.getFontMetrics();
        g.drawString(cText, centerX - fmC.stringWidth(cText) / 2, centerY - 5);

        // Options
        g.setFont(new Font(THAI_FONT, Font.PLAIN, 15));
        g.setColor(Color.WHITE);
        String opt1 = "[ESC / P] เล่นต่อ (Resume)";
        String opt2 = "[R] เริ่มด่านใหม่ (Restart Stage)";
        String opt3 = "[M] กลับสู่หน้าหลัก (Main Menu)";
        FontMetrics fmO = g.getFontMetrics();
        g.drawString(opt1, centerX - fmO.stringWidth(opt1) / 2, centerY + 50);
        g.drawString(opt2, centerX - fmO.stringWidth(opt2) / 2, centerY + 80);
        g.drawString(opt3, centerX - fmO.stringWidth(opt3) / 2, centerY + 110);
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
