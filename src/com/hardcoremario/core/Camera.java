package com.hardcoremario.core;

import com.hardcoremario.util.Constants;

/**
 * Camera class that smoothly follows the player and clamps to level boundaries.
 */
public class Camera {
    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private double levelWidth;
    private double levelHeight;

    public Camera(double levelWidth, double levelHeight) {
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        this.x = 0;
        this.y = 0;
    }

    public void setLevelBounds(double width, double height) {
        this.levelWidth = width;
        this.levelHeight = height;
    }

    public void update(double playerCenterX, double playerCenterY, double deltaTime) {
        // Target is centering the player on the screen
        targetX = playerCenterX - Constants.SCREEN_WIDTH / 2.0;
        targetY = playerCenterY - Constants.SCREEN_HEIGHT / 2.0;

        // Clamp target within level bounds
        if (targetX < 0) targetX = 0;
        if (targetX > levelWidth - Constants.SCREEN_WIDTH) targetX = levelWidth - Constants.SCREEN_WIDTH;

        if (targetY < 0) targetY = 0;
        if (targetY > levelHeight - Constants.SCREEN_HEIGHT) targetY = levelHeight - Constants.SCREEN_HEIGHT;

        // Smooth lerp follow (interpolate towards target)
        double lerpFactor = Math.min(1.0, 7.5 * deltaTime);
        x += (targetX - x) * lerpFactor;
        y += (targetY - y) * lerpFactor;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
