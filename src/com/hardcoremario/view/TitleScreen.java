package com.hardcoremario.view;

import com.hardcoremario.core.GameSettings;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.core.SoundManager;
import com.hardcoremario.util.Constants;
import java.awt.*;

/**
 * TitleScreen renders the main game menu with:
 * - PLAY button to start the game
 * - Difficulty selector: ROOKIE, VETERAN, PSYCO, GODLIKE
 * - Bullet color accessibility settings for colorblindness and high visibility
 * - Animated interactive buttons, live bullet preview, and keyboard shortcuts
 */
public class TitleScreen {

    // Difficulty buttons layout constants
    public static final int DIFF_CARD_W = 180;
    public static final int DIFF_CARD_H = 50;
    public static final int DIFF_GAP = 16;
    public static final int DIFF_Y = 164;

    // Bullet Color buttons layout constants
    public static final int THEME_BTN_W = 125;
    public static final int THEME_BTN_H = 40;
    public static final int THEME_GAP = 10;
    public static final int THEME_Y = 306;

    // Play button layout constants
    public static final int PLAY_BTN_W = 280;
    public static final int PLAY_BTN_H = 52;
    public static final int PLAY_BTN_Y = 490;
    public static final Rectangle PLAY_BTN_BOUNDS = new Rectangle(
        (Constants.SCREEN_WIDTH - PLAY_BTN_W) / 2, PLAY_BTN_Y, PLAY_BTN_W, PLAY_BTN_H
    );

    private double animTimer = 0.0;
    private boolean startRequested = false;

    public static Rectangle getDifficultyBounds(int index) {
        int count = GameSettings.Difficulty.values().length;
        int totalW = count * DIFF_CARD_W + (count - 1) * DIFF_GAP;
        int startX = (Constants.SCREEN_WIDTH - totalW) / 2;
        return new Rectangle(startX + index * (DIFF_CARD_W + DIFF_GAP), DIFF_Y, DIFF_CARD_W, DIFF_CARD_H);
    }

    public static Rectangle getThemeBounds(int index) {
        int count = GameSettings.BulletColorTheme.values().length;
        int totalW = count * THEME_BTN_W + (count - 1) * THEME_GAP;
        int startX = (Constants.SCREEN_WIDTH - totalW) / 2;
        return new Rectangle(startX + index * (THEME_BTN_W + THEME_GAP), THEME_Y, THEME_BTN_W, THEME_BTN_H);
    }

    public boolean isAnyButtonHovered(int mx, int my) {
        if (PLAY_BTN_BOUNDS.contains(mx, my)) return true;
        for (int i = 0; i < GameSettings.Difficulty.values().length; i++) {
            if (getDifficultyBounds(i).contains(mx, my)) return true;
        }
        for (int i = 0; i < GameSettings.BulletColorTheme.values().length; i++) {
            if (getThemeBounds(i).contains(mx, my)) return true;
        }
        return false;
    }

    public void update(double deltaTime, InputHandler input) {
        animTimer += deltaTime;

        int mx = input.getMouseX();
        int my = input.getMouseY();
        boolean click = input.consumeMouseClick();

        // 1. Keyboard shortcuts
        if (input.consumeEnter()) {
            triggerStart();
            return;
        }
        if (input.consumeColorCycle()) {
            GameSettings.getInstance().cycleNextBulletColor();
            SoundManager.getInstance().playPickup();
        }
        if (input.consumeNumber1()) {
            GameSettings.getInstance().setDifficulty(GameSettings.Difficulty.ROOKIE);
            SoundManager.getInstance().playPickup();
        }
        if (input.consumeNumber2()) {
            GameSettings.getInstance().setDifficulty(GameSettings.Difficulty.VETERAN);
            SoundManager.getInstance().playPickup();
        }
        if (input.consumeNumber3()) {
            GameSettings.getInstance().setDifficulty(GameSettings.Difficulty.PSYCO);
            SoundManager.getInstance().playPickup();
        }
        if (input.consumeNumber4()) {
            GameSettings.getInstance().setDifficulty(GameSettings.Difficulty.GODLIKE);
            SoundManager.getInstance().playPickup();
        }

        // 2. Play Button Click
        if (click && PLAY_BTN_BOUNDS.contains(mx, my)) {
            triggerStart();
            return;
        }

        // 3. Difficulty Buttons Click
        GameSettings.Difficulty[] diffs = GameSettings.Difficulty.values();
        for (int i = 0; i < diffs.length; i++) {
            Rectangle cardRect = getDifficultyBounds(i);
            if (click && cardRect.contains(mx, my)) {
                GameSettings.getInstance().setDifficulty(diffs[i]);
                SoundManager.getInstance().playPickup();
                break;
            }
        }

        // 4. Bullet Color Themes Click
        GameSettings.BulletColorTheme[] themes = GameSettings.BulletColorTheme.values();
        for (int i = 0; i < themes.length; i++) {
            Rectangle themeRect = getThemeBounds(i);
            if (click && themeRect.contains(mx, my)) {
                GameSettings.getInstance().setBulletColorTheme(themes[i]);
                SoundManager.getInstance().playPickup();
                break;
            }
        }
    }

    private void triggerStart() {
        startRequested = true;
        SoundManager.getInstance().playPlayerShoot();
    }

    public boolean consumeStartRequested() {
        boolean r = startRequested;
        startRequested = false;
        return r;
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

    public void render(Graphics2D g, int mouseX, int mouseY) {
        // Enable anti-aliasing for text and vector shapes
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Dark Vignette Glassmorphic Backdrop
        g.setColor(new Color(8, 10, 16, 235));
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Cyberpunk decorative background scanlines
        g.setColor(new Color(255, 255, 255, 4));
        for (int y = 0; y < Constants.SCREEN_HEIGHT; y += 4) {
            g.drawLine(0, y, Constants.SCREEN_WIDTH, y);
        }

        // Ambient cyber accents
        double pulse = (Math.sin(animTimer * 2.5) + 1.0) / 2.0;

        // 2. Title & Logo
        renderHeader(g, pulse);

        // 3. Difficulty Selector
        renderDifficultySection(g, mouseX, mouseY);

        // 4. Bullet Color / Accessibility Section
        renderBulletColorSection(g, mouseX, mouseY, pulse);

        // 5. Play Button
        renderPlayButton(g, mouseX, mouseY, pulse);

        // 6. Bottom Controls Guide & Student Credits
        renderFooter(g);
    }

    private void renderHeader(Graphics2D g, double pulse) {
        int centerX = Constants.SCREEN_WIDTH / 2;

        // Glowing Title Shadow
        g.setFont(new Font("Impact", Font.BOLD, 54));
        String title = "HARDCORE MARIO";
        FontMetrics fm = g.getFontMetrics();
        int titleW = fm.stringWidth(title);

        g.setColor(new Color(255, 60, 20, (int) (60 + pulse * 40)));
        g.drawString(title, centerX - titleW / 2 - 2, 78);
        g.drawString(title, centerX - titleW / 2 + 2, 82);

        // Main Title
        g.setColor(new Color(255, 200, 40));
        g.drawString(title, centerX - titleW / 2, 80);

        // Subtitle
        g.setFont(new Font(THAI_FONT, Font.BOLD, 14));
        String subtitle = "APEX SYNDICATE ESCAPE • ภารกิจฝ่าด่านนรก";
        FontMetrics fmSub = g.getFontMetrics();
        g.setColor(new Color(0, 240, 255));
        g.drawString(subtitle, centerX - fmSub.stringWidth(subtitle) / 2, 108);

        // Top decorative neon separator line
        int sepW = 440;
        GradientPaint grad = new GradientPaint(
            centerX - sepW / 2, 118, new Color(0, 240, 255, 0),
            centerX, 118, new Color(0, 240, 255, 180), true
        );
        g.setPaint(grad);
        g.fillRect(centerX - sepW / 2, 118, sepW, 2);
    }

    private void renderDifficultySection(Graphics2D g, int mouseX, int mouseY) {
        int centerX = Constants.SCREEN_WIDTH / 2;

        // Section Title
        g.setFont(new Font(THAI_FONT, Font.BOLD, 13));
        g.setColor(new Color(240, 240, 255));
        String secTitle = "DIFFICULTY SELECTOR (ระดับความยาก - มีผลต่อความไวกระสุนและความอึดของศัตรู)";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(secTitle, centerX - fm.stringWidth(secTitle) / 2, 148);

        GameSettings.Difficulty current = GameSettings.getInstance().getDifficulty();
        GameSettings.Difficulty[] diffs = GameSettings.Difficulty.values();

        for (int i = 0; i < diffs.length; i++) {
            GameSettings.Difficulty d = diffs[i];
            Rectangle rect = getDifficultyBounds(i);
            int x = rect.x;
            int cardY = rect.y;
            int cardW = rect.width;
            int cardH = rect.height;
            boolean isHovered = rect.contains(mouseX, mouseY);
            boolean isSelected = (d == current);

            // Card Background
            if (isSelected) {
                g.setColor(new Color(d.getBadgeColor().getRed(), d.getBadgeColor().getGreen(), d.getBadgeColor().getBlue(), 75));
            } else if (isHovered) {
                g.setColor(new Color(40, 45, 60, 200));
            } else {
                g.setColor(new Color(20, 24, 32, 200));
            }
            g.fillRoundRect(x, cardY, cardW, cardH, 12, 12);

            // Card Border
            if (isSelected) {
                g.setColor(d.getBadgeColor());
                g.setStroke(new BasicStroke(2.5f));
            } else if (isHovered) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.5f));
            } else {
                g.setColor(new Color(70, 75, 90));
                g.setStroke(new BasicStroke(1.0f));
            }
            g.drawRoundRect(x, cardY, cardW, cardH, 12, 12);
            g.setStroke(new BasicStroke(1.0f));

            // Difficulty Name
            g.setFont(new Font("Impact", Font.PLAIN, 18));
            g.setColor(isSelected ? Color.WHITE : d.getBadgeColor());
            FontMetrics fmD = g.getFontMetrics();
            g.drawString(d.getCodeName(), x + (cardW - fmD.stringWidth(d.getCodeName())) / 2, cardY + 23);

            // Sub-label (Thai)
            g.setFont(new Font(THAI_FONT, Font.PLAIN, 11));
            g.setColor(isSelected ? new Color(240, 240, 240) : new Color(160, 165, 180));
            FontMetrics fmL = g.getFontMetrics();
            g.drawString(d.getLabelThai(), x + (cardW - fmL.stringWidth(d.getLabelThai())) / 2, cardY + 41);

            // Active indicator dot
            if (isSelected) {
                g.setColor(d.getBadgeColor());
                g.fillOval(x + 10, cardY + 10, 8, 8);
                g.setColor(Color.WHITE);
                g.fillOval(x + 12, cardY + 12, 4, 4);
            }
        }

        // Active Difficulty Stats Description Box
        int descBoxW = 768;
        int descBoxH = 34;
        int descBoxX = (Constants.SCREEN_WIDTH - descBoxW) / 2;
        int descBoxY = 226;

        g.setColor(new Color(15, 18, 25, 230));
        g.fillRoundRect(descBoxX, descBoxY, descBoxW, descBoxH, 8, 8);
        g.setColor(new Color(current.getBadgeColor().getRed(), current.getBadgeColor().getGreen(), current.getBadgeColor().getBlue(), 120));
        g.drawRoundRect(descBoxX, descBoxY, descBoxW, descBoxH, 8, 8);

        g.setFont(new Font(THAI_FONT, Font.BOLD, 12));
        g.setColor(current.getBadgeColor());
        String badgeTag = "[" + current.getCodeName() + "] ";
        g.drawString(badgeTag, descBoxX + 16, descBoxY + 22);

        g.setFont(new Font(THAI_FONT, Font.PLAIN, 12));
        g.setColor(Color.WHITE);
        g.drawString(current.getDescription(), descBoxX + 16 + g.getFontMetrics().stringWidth(badgeTag) + 2, descBoxY + 22);
    }

    private void renderBulletColorSection(Graphics2D g, int mouseX, int mouseY, double pulse) {
        int centerX = Constants.SCREEN_WIDTH / 2;

        // Section Title
        g.setFont(new Font(THAI_FONT, Font.BOLD, 13));
        g.setColor(new Color(240, 240, 255));
        String secTitle = "BULLET VISIBILITY / COLORBLIND SETTINGS (ตั้งค่าสีกระสุนคนตาบอดสี/มองไม่ชัด)";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(secTitle, centerX - fm.stringWidth(secTitle) / 2, 288);

        GameSettings.BulletColorTheme currentTheme = GameSettings.getInstance().getBulletColorTheme();
        GameSettings.BulletColorTheme[] themes = GameSettings.BulletColorTheme.values();

        for (int i = 0; i < themes.length; i++) {
            GameSettings.BulletColorTheme t = themes[i];
            Rectangle rect = getThemeBounds(i);
            int x = rect.x;
            int btnY = rect.y;
            int btnW = rect.width;
            int btnH = rect.height;
            boolean isHovered = rect.contains(mouseX, mouseY);
            boolean isSelected = (t == currentTheme);

            // Button background
            if (isSelected) {
                g.setColor(new Color(t.getMainColor().getRed(), t.getMainColor().getGreen(), t.getMainColor().getBlue(), 75));
            } else if (isHovered) {
                g.setColor(new Color(40, 45, 60, 200));
            } else {
                g.setColor(new Color(20, 24, 32, 200));
            }
            g.fillRoundRect(x, btnY, btnW, btnH, 10, 10);

            // Border
            if (isSelected) {
                g.setColor(t.getMainColor());
                g.setStroke(new BasicStroke(2.0f));
            } else if (isHovered) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.2f));
            } else {
                g.setColor(new Color(65, 70, 85));
                g.setStroke(new BasicStroke(1.0f));
            }
            g.drawRoundRect(x, btnY, btnW, btnH, 10, 10);
            g.setStroke(new BasicStroke(1.0f));

            // Color circle icon
            g.setColor(t.getMainColor());
            g.fillOval(x + 10, btnY + 12, 16, 16);
            g.setColor(t.getCoreColor());
            g.fillOval(x + 14, btnY + 16, 8, 8);
            g.setColor(new Color(0, 0, 0, 180));
            g.drawOval(x + 10, btnY + 12, 16, 16);

            // Label
            g.setFont(new Font(THAI_FONT, Font.BOLD, 12));
            g.setColor(isSelected ? Color.WHITE : new Color(200, 205, 220));
            g.drawString(t.getDisplayName(), x + 34, btnY + 25);
        }

        // Live Bullet Preview Bar
        int previewW = 768;
        int previewH = 44;
        int previewX = (Constants.SCREEN_WIDTH - previewW) / 2;
        int previewY = 358;

        g.setColor(new Color(15, 18, 25, 230));
        g.fillRoundRect(previewX, previewY, previewW, previewH, 10, 10);
        g.setColor(new Color(currentTheme.getMainColor().getRed(), currentTheme.getMainColor().getGreen(), currentTheme.getMainColor().getBlue(), 120));
        g.drawRoundRect(previewX, previewY, previewW, previewH, 10, 10);

        // Animated sample bullet preview in motion
        int bulletDrawX = previewX + 38;
        int bulletDrawY = previewY + 22;

        // Animated pulse glow
        int glowSize = (int) (22 + pulse * 6);
        g.setColor(new Color(currentTheme.getMainColor().getRed(), currentTheme.getMainColor().getGreen(), currentTheme.getMainColor().getBlue(), 100));
        g.fillOval(bulletDrawX - glowSize / 2, bulletDrawY - glowSize / 2, glowSize, glowSize);

        // Bullet core
        g.setColor(new Color(10, 10, 15, 220));
        g.fillOval(bulletDrawX - 8, bulletDrawY - 8, 16, 16);
        g.setColor(currentTheme.getMainColor());
        g.fillOval(bulletDrawX - 7, bulletDrawY - 7, 14, 14);
        g.setColor(currentTheme.getCoreColor());
        g.fillOval(bulletDrawX - 3, bulletDrawY - 3, 6, 6);

        // Preview label text
        g.setFont(new Font(THAI_FONT, Font.BOLD, 12));
        g.setColor(currentTheme.getMainColor());
        String previewTitle = "ตัวอย่างกระสุน: " + currentTheme.getDisplayName() + " [" + currentTheme.getAccessibilityTag() + "]";
        g.drawString(previewTitle, previewX + 70, previewY + 27);

        g.setFont(new Font(THAI_FONT, Font.PLAIN, 11));
        g.setColor(new Color(170, 175, 190));
        String tip = "(กดปุ่ม 'C' ระหว่างเล่นเพื่อสลับสีกระสุนได้ตลอดเวลา)";
        g.drawString(tip, previewX + previewW - g.getFontMetrics().stringWidth(tip) - 16, previewY + 27);
    }

    private void renderPlayButton(Graphics2D g, int mouseX, int mouseY, double pulse) {
        Rectangle rect = PLAY_BTN_BOUNDS;
        boolean isHovered = rect.contains(mouseX, mouseY);

        // Glowing outer drop-shadow
        int shadowAlpha = isHovered ? (int) (140 + pulse * 60) : (int) (60 + pulse * 40);
        g.setColor(new Color(0, 240, 255, shadowAlpha));
        g.fillRoundRect(rect.x - 4, rect.y - 4, rect.width + 8, rect.height + 8, 20, 20);

        // Button background
        if (isHovered) {
            GradientPaint gp = new GradientPaint(
                rect.x, rect.y, new Color(0, 255, 230),
                rect.x + rect.width, rect.y + rect.height, new Color(0, 150, 255)
            );
            g.setPaint(gp);
        } else {
            GradientPaint gp = new GradientPaint(
                rect.x, rect.y, new Color(0, 180, 230),
                rect.x + rect.width, rect.y + rect.height, new Color(0, 80, 180)
            );
            g.setPaint(gp);
        }
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);

        // Button Border
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(isHovered ? 2.5f : 1.5f));
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);
        g.setStroke(new BasicStroke(1.0f));

        // Play Button Text & Vector Triangle Icon
        g.setFont(new Font("Impact", Font.PLAIN, 26));
        Color textColor = isHovered ? Color.BLACK : Color.WHITE;
        g.setColor(textColor);
        String playText = "START MISSION (PLAY)";
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(playText);
        int totalContentW = 24 + textW; // icon + spacing + text
        int contentStartX = rect.x + (rect.width - totalContentW) / 2;

        // Draw crisp vector play triangle
        int iconX = contentStartX;
        int iconY = rect.y + rect.height / 2;
        int[] triX = {iconX, iconX + 14, iconX};
        int[] triY = {iconY - 10, iconY, iconY + 10};
        g.fillPolygon(triX, triY, 3);

        // Draw text
        g.drawString(playText, contentStartX + 24, rect.y + 36);
    }

    private void renderFooter(Graphics2D g) {
        int centerX = Constants.SCREEN_WIDTH / 2;

        // Quick Controls
        g.setFont(new Font(THAI_FONT, Font.PLAIN, 12));
        g.setColor(new Color(180, 185, 200));
        String controls = "การควบคุม: [W,A,S,D] เดิน/หมอบ  •  [SPACE] กระโดด  •  [คลิกซ้าย] ยิง  •  [R] รีโหลด  •  [ESC/P] หยุดเกม";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(controls, centerX - fm.stringWidth(controls) / 2, 574);

        // Student Credits & Version
        g.setFont(new Font(THAI_FONT, Font.PLAIN, 11));
        g.setColor(new Color(110, 115, 130));
        String studentInfo = "ผู้พัฒนา: ปฏิพล จันทร์บุญ (6804062612102 ตอน 3) • วิชา Object-Oriented Programming (OOP)";
        FontMetrics fmS = g.getFontMetrics();
        g.drawString(studentInfo, centerX - fmS.stringWidth(studentInfo) / 2, 606);
    }
}
