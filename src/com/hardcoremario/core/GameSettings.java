package com.hardcoremario.core;

import java.awt.Color;

/**
 * Global game settings managing difficulty scaling and accessibility options
 * such as high-visibility and colorblind bullet color themes.
 */
public class GameSettings {

    public enum Difficulty {
        ROOKIE("ROOKIE", "โหมดฝึกหัด (ง่าย)", 35, 300.0, new Color(46, 204, 113),
               "กระสุนช้า 300 px/s (-28%) • ศัตรูเลือด 35 HP (ยิง 2 นัดตาย)"),
        VETERAN("VETERAN", "โหมดมาตรฐาน (ปานกลาง)", 50, 420.0, new Color(52, 152, 219),
                "ระดับมาตรฐาน 420 px/s • ศัตรูเลือด 50 HP (ยิง 2-3 นัดตาย)"),
        PSYCO("PSYCO", "โหมดไซโค (ฮาร์ดคอร์)", 75, 580.0, new Color(230, 126, 34),
              "กระสุนไว 580 px/s (+38%) • ศัตรูเลือด 75 HP (+50%) ดุดันขึ้นมาก!"),
        GODLIKE("GODLIKE", "โหมดนรกแตก (พระเจ้า)", 125, 780.0, new Color(231, 76, 60),
                "กระสุนไวระดับแสง 780 px/s (+85%) • ศัตรูถึกทน 125 HP (+150%)!");

        private final String codeName;
        private final String labelThai;
        private final int enemyHp;
        private final double enemyBulletSpeed;
        private final Color badgeColor;
        private final String description;

        Difficulty(String codeName, String labelThai, int enemyHp, double enemyBulletSpeed, Color badgeColor, String description) {
            this.codeName = codeName;
            this.labelThai = labelThai;
            this.enemyHp = enemyHp;
            this.enemyBulletSpeed = enemyBulletSpeed;
            this.badgeColor = badgeColor;
            this.description = description;
        }

        public String getCodeName() { return codeName; }
        public String getLabelThai() { return labelThai; }
        public int getEnemyHp() { return enemyHp; }
        public double getEnemyBulletSpeed() { return enemyBulletSpeed; }
        public Color getBadgeColor() { return badgeColor; }
        public String getDescription() { return description; }
    }

    public enum BulletColorTheme {
        CRIMSON("แดงเพลิง", "Default (คลาสสิก)", new Color(255, 50, 50), new Color(255, 200, 200)),
        NEON_YELLOW("เหลืองนีออน", "High-Contrast (สว่างมาก)", new Color(255, 235, 0), new Color(255, 255, 220)),
        ELECTRIC_CYAN("ฟ้าสว่าง", "Protanopia (ตาบอดสีแดง/เขียว)", new Color(0, 235, 255), new Color(220, 255, 255)),
        HOT_PINK("ชมพูนีออน", "Tritanopia (ตาบอดสีน้ำเงิน)", new Color(255, 40, 160), new Color(255, 210, 240)),
        LIME_GREEN("เขียวนีออน", "Ultra-Bright (ตัดกับฉากมืด)", new Color(50, 255, 70), new Color(210, 255, 210)),
        VIBRANT_ORANGE("ส้มสะท้อน", "Alert Orange (เตือนภัยชัดเจน)", new Color(255, 140, 0), new Color(255, 230, 180));

        private final String displayName;
        private final String accessibilityTag;
        private final Color mainColor;
        private final Color coreColor;

        BulletColorTheme(String displayName, String accessibilityTag, Color mainColor, Color coreColor) {
            this.displayName = displayName;
            this.accessibilityTag = accessibilityTag;
            this.mainColor = mainColor;
            this.coreColor = coreColor;
        }

        public String getDisplayName() { return displayName; }
        public String getAccessibilityTag() { return accessibilityTag; }
        public Color getMainColor() { return mainColor; }
        public Color getCoreColor() { return coreColor; }
    }

    private static GameSettings instance;
    private Difficulty difficulty = Difficulty.VETERAN;
    private BulletColorTheme bulletColorTheme = BulletColorTheme.CRIMSON;

    private GameSettings() {}

    public static synchronized GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    public BulletColorTheme getBulletColorTheme() {
        return bulletColorTheme;
    }

    public void setBulletColorTheme(BulletColorTheme bulletColorTheme) {
        if (bulletColorTheme != null) {
            this.bulletColorTheme = bulletColorTheme;
        }
    }

    public void cycleNextBulletColor() {
        BulletColorTheme[] themes = BulletColorTheme.values();
        int next = (bulletColorTheme.ordinal() + 1) % themes.length;
        setBulletColorTheme(themes[next]);
    }

    public void cycleNextDifficulty() {
        Difficulty[] diffs = Difficulty.values();
        int next = (difficulty.ordinal() + 1) % diffs.length;
        setDifficulty(diffs[next]);
    }
}
