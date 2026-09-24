package com.hardcoremario.core;

import com.hardcoremario.util.Constants;
import com.hardcoremario.view.GamePanel;

/**
 * GameEngine runs the main game loop thread targeting smooth 60 FPS with delta time calculation.
 */
public class GameEngine implements Runnable {
    private final GamePanel gamePanel;
    private Thread gameThread;
    private volatile boolean running = false;

    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameLoopThread");
        gameThread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerSecond = 1_000_000_000.0;
        final double targetDelta = 1.0 / Constants.TARGET_FPS;

        while (running) {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastTime) / nsPerSecond;
            lastTime = now;

            // Cap delta time to prevent spiral of death during lag spikes
            if (elapsedSeconds > 0.1) {
                elapsedSeconds = 0.1;
            }

            // Update game physics and logic
            gamePanel.updateGame(elapsedSeconds);

            // Repaint screen
            gamePanel.repaint();

            // Calculate sleep time to maintain 60 FPS
            long frameEndTime = System.nanoTime();
            double frameDurationSeconds = (frameEndTime - now) / nsPerSecond;
            double remainingSeconds = targetDelta - frameDurationSeconds;

            if (remainingSeconds > 0) {
                long sleepMs = (long) (remainingSeconds * 1000.0);
                int sleepNs = (int) ((remainingSeconds * 1000.0 - sleepMs) * 1_000_000);
                try {
                    Thread.sleep(sleepMs, sleepNs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
