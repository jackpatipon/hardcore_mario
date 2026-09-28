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
    private double minY = -400.0;
    private double maxX = 3600;
    private double maxY = 1400;

    private double zoom = 1.0;
    private double targetZoom = 1.0;

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
        this.minY = Math.min(minY, -400.0);
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void snapTo(double playerCenterX, double playerCenterY) {
        this.zoom = 1.0;
        this.targetZoom = 1.0;
        double halfVisibleW = (Constants.SCREEN_WIDTH / 2.0) / zoom;
        double halfVisibleH = (Constants.SCREEN_HEIGHT / 2.0) / zoom;
        targetX = playerCenterX - halfVisibleW;
        targetY = (playerCenterY - 40.0) - halfVisibleH;
        clampTarget();
        this.x = targetX;
        this.y = targetY;
    }

    private void clampTarget() {
        double visibleW = Constants.SCREEN_WIDTH / zoom;
        double visibleH = Constants.SCREEN_HEIGHT / zoom;

        if (maxX - minX >= visibleW) {
            if (targetX < minX) targetX = minX;
            if (targetX > maxX - visibleW) targetX = maxX - visibleW;
        } else {
            targetX = minX - (visibleW - (maxX - minX)) / 2.0;
        }

        if (maxY - minY >= visibleH) {
            if (targetY < minY) targetY = minY;
            if (targetY > maxY - visibleH) targetY = maxY - visibleH;
        } else {
            targetY = minY - (visibleH - (maxY - minY)) / 2.0;
        }
    }

    public void update(double playerCenterX, double playerCenterY, double deltaTime) {
        update(playerCenterX, playerCenterY, 0, 0, false, deltaTime);
    }

    public void update(double playerCenterX, double playerCenterY, double bossCenterX, double bossCenterY, boolean bossFramingActive, double deltaTime) {
        double targetCenterX;
        double targetCenterY;

        if (bossFramingActive) {
            // Horizontal framing margin
            double marginX = 360.0;
            double spanX = Math.abs(playerCenterX - bossCenterX) + marginX;
            double neededZoomX = Constants.SCREEN_WIDTH / spanX;

            // Vertical framing: generous top margin so player/boss is never obscured by
            // the Boss HP Bar, Stage Title, or difficulty badge at the top of the screen
            double marginTop = 340.0;
            double marginBottom = 160.0;
            double dy = Math.abs(playerCenterY - bossCenterY);
            double spanY = dy + marginTop + marginBottom;
            double neededZoomY = Constants.SCREEN_HEIGHT / spanY;

            double neededZoom = Math.min(neededZoomX, neededZoomY);

            // Zoom bounds: closest is normal view (1.0), minimum zoom limit is 0.45
            targetZoom = Math.max(0.45, Math.min(1.0, neededZoom));

            targetCenterX = (playerCenterX + bossCenterX) / 2.0;

            // Offset vertical focus upward to give ample headroom above the higher entity
            double minEntityY = Math.min(playerCenterY, bossCenterY);
            double maxEntityY = Math.max(playerCenterY, bossCenterY);
            targetCenterY = (minEntityY - marginTop + maxEntityY + marginBottom) / 2.0;
        } else {
            targetZoom = 1.0;
            targetCenterX = playerCenterX;
            targetCenterY = playerCenterY - 40.0;
        }

        // Smooth zoom transition
        double zoomLerp = Math.min(1.0, 4.0 * deltaTime);
        zoom += (targetZoom - zoom) * zoomLerp;

        // Viewport bounds in world units
        double halfVisibleW = (Constants.SCREEN_WIDTH / 2.0) / zoom;
        double halfVisibleH = (Constants.SCREEN_HEIGHT / 2.0) / zoom;
        targetX = targetCenterX - halfVisibleW;
        targetY = targetCenterY - halfVisibleH;

        clampTarget();

        // Smooth position follow
        double lerpFactor = Math.min(1.0, 6.5 * deltaTime);
        x += (targetX - x) * lerpFactor;
        y += (targetY - y) * lerpFactor;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZoom() { return zoom; }
    public double getTargetZoom() { return targetZoom; }
    public void setZoom(double zoom) { this.zoom = zoom; this.targetZoom = zoom; }
    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
}
