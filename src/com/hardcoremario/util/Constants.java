package com.hardcoremario.util;

public final class Constants {
    private Constants() {} // Private constructor to prevent instantiation

    // Window & Display
    public static final String GAME_TITLE = "Hardcore Mario (มาริโอ้เถื่อน) - Apex Escape";
    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 640;
    public static final int TARGET_FPS = 60;

    // Physics & Grid
    public static final int TILE_SIZE = 48;
    public static final double GRAVITY = 1100.0;     // Pixels per second squared
    public static final double MAX_FALL_SPEED = 900.0;

    // Player Properties
    public static final int PLAYER_MAX_HP = 100;
    public static final double PLAYER_MOVE_SPEED = 240.0;
    public static final double PLAYER_JUMP_SPEED = -560.0;
    public static final int PLAYER_WIDTH = 40;
    public static final int PLAYER_HEIGHT = 58;
    public static final int PLAYER_CROUCH_HEIGHT = 34;

    // Weapon & Ammo
    public static final int DEFAULT_MAGAZINE_SIZE = 30;
    public static final int DEFAULT_RESERVE_AMMO = 90;
    public static final double FIRE_RATE_INTERVAL = 0.13; // Seconds per shot (~7.6 shots/sec)
    public static final double RELOAD_TIME = 1.6;        // Seconds to reload
    public static final double PLAYER_BULLET_SPEED = 750.0;
    public static final int PLAYER_BULLET_DAMAGE = 25;

    // Enemy (Guard) Properties
    public static final int GUARD_MAX_HP = 50;
    public static final double GUARD_MOVE_SPEED = 90.0;
    public static final double GUARD_SIGHT_RANGE = 520.0;
    public static final double GUARD_FIRE_COOLDOWN = 1.4; // Seconds between enemy shots
    public static final double ENEMY_BULLET_SPEED = 420.0;
    public static final int ENEMY_BULLET_DAMAGE = 15;
}
