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
    private double minX = 0;
    private double minY = 0;
    private double maxX = 3600;
    private double maxY = 1400;

    public Camera(double levelWidth, double levelHeight) {
        this(0, 0, levelWidth, levelHeight);
    }

    public Camera(double minX, double minY, double maxX, double maxY) {
        setBounds(minX, minY, maxX, maxY);
        this.x = minX;
        this.y = minY;
    }

    public void setLevelBounds(double width, double height) {
        setBounds(0, 0, width, height);
    }

    public void setBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void snapTo(double playerCenterX, double playerCenterY) {
        targetX = playerCenterX - Constants.SCREEN_WIDTH / 2.0;
        targetY = playerCenterY - Constants.SCREEN_HEIGHT / 2.0;
        clampTarget();
        this.x = targetX;
        this.y = targetY;
    }

    private void clampTarget() {
        if (maxX - minX >= Constants.SCREEN_WIDTH) {
            if (targetX < minX) targetX = minX;
            if (targetX > maxX - Constants.SCREEN_WIDTH) targetX = maxX - Constants.SCREEN_WIDTH;
        } else {
            targetX = minX;
        }

        if (maxY - minY >= Constants.SCREEN_HEIGHT) {
            if (targetY < minY) targetY = minY;
            if (targetY > maxY - Constants.SCREEN_HEIGHT) targetY = maxY - Constants.SCREEN_HEIGHT;
        } else {
            targetY = minY;
        }
    }

    public void update(double playerCenterX, double playerCenterY, double deltaTime) {
        // Target is centering the player on the screen
        targetX = playerCenterX - Constants.SCREEN_WIDTH / 2.0;
        targetY = playerCenterY - Constants.SCREEN_HEIGHT / 2.0;

        clampTarget();

        // Smooth lerp follow (interpolate towards target)
        double lerpFactor = Math.min(1.0, 7.5 * deltaTime);
        x += (targetX - x) * lerpFactor;
        y += (targetY - y) * lerpFactor;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
}
