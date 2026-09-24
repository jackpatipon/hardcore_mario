package com.hardcoremario.core;

import java.awt.event.*;

/**
 * Handles all keyboard and mouse inputs.
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    // Keyboard state flags
    private boolean moveLeft;
    private boolean moveRight;
    private boolean jump;
    private boolean crouch;
    private boolean reloadRequested;
    private boolean restartRequested;
    private boolean toggleHelpRequested;
    private boolean pauseRequested;

    // Mouse state
    private int mouseX;
    private int mouseY;
    private boolean mouseLeftPressed;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                moveLeft = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                moveRight = true;
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
                jump = true;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                crouch = true;
                break;
            case KeyEvent.VK_R:
                reloadRequested = true;
                restartRequested = true; // Also used to restart on Game Over
                break;
            case KeyEvent.VK_H:
                toggleHelpRequested = true;
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_P:
                pauseRequested = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                moveLeft = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                moveRight = false;
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
                jump = false;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                crouch = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftPressed = true;
            restartRequested = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftPressed = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    // Getters and consumer methods
    public boolean isMoveLeft() { return moveLeft; }
    public boolean isMoveRight() { return moveRight; }
    public boolean isJump() { return jump; }
    public boolean isCrouch() { return crouch; }
    public boolean isMouseLeftPressed() { return mouseLeftPressed; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    public boolean consumeReload() {
        boolean r = reloadRequested;
        reloadRequested = false;
        return r;
    }

    public boolean consumeRestart() {
        boolean r = restartRequested;
        restartRequested = false;
        return r;
    }

    public boolean consumeToggleHelp() {
        boolean r = toggleHelpRequested;
        toggleHelpRequested = false;
        return r;
    }

    public boolean consumePause() {
        boolean r = pauseRequested;
        pauseRequested = false;
        return r;
    }
}
