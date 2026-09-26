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

    /**
     * กำหนดด่านเริ่มต้นที่ต้องการเล่น / ทดสอบ (1, 2, หรือ 3):
     * - ตั้งเป็น 1 : เข้าหน้า Title Screen ตามปกติ
     * - ตั้งเป็น 2 หรือ 3 : เริ่มที่ด่านนั้นทันทีเพื่อความสะดวกรวดเร็วในการทดสอบ!
     * (หรือขณะเล่นเกม สามารถกดปุ่ม F1, F2, F3 บนคีย์บอร์ดเพื่อวาร์ปข้ามด่านได้ตลอดเวลา)
     */
    public static final int STARTING_STAGE = 1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(Constants.GAME_TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Initialize Level from configured STARTING_STAGE
            Level level = new Level(STARTING_STAGE);
            Camera camera = new Camera(level.getMinX(), level.getMinY(), level.getMaxX(), level.getMaxY());
            InputHandler inputHandler = new InputHandler();

            GamePanel gamePanel = new GamePanel(level, camera, inputHandler);
            frame.setContentPane(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();

            // Start the 60 FPS Game Loop
            GameEngine engine = new GameEngine(gamePanel);
            engine.start();

            // Clean shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(engine::stop));
        });
    }
}
