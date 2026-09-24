package com.hardcoremario;

import com.hardcoremario.core.Camera;
import com.hardcoremario.core.GameEngine;
import com.hardcoremario.core.InputHandler;
import com.hardcoremario.model.world.Level;
import com.hardcoremario.util.Constants;
import com.hardcoremario.view.GamePanel;
import javax.swing.*;

/**
 * Main entry point for the Hardcore Mario game application.
 * Project: Object-Oriented Programming (OOP)
 * Student: ปฏิพล จันทร์บุญ (6804062612102 ตอน 3)
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(Constants.GAME_TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Initialize Level from Stage 1 (hardcore_mario_stage1.drawio.xml)
            Level level = new Level(1);
            Camera camera = new Camera(level.getWidth(), level.getHeight());
            InputHandler inputHandler = new InputHandler();

            GamePanel gamePanel = new GamePanel(level, camera, inputHandler);
            frame.setContentPane(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);

            // Start the 60 FPS Game Loop
            GameEngine engine = new GameEngine(gamePanel);
            engine.start();

            // Clean shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(engine::stop));
        });
    }
}
