package com.hardcoremario.core;

import java.awt.event.*;

/**
 * Handles all keyboard and mouse inputs.
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    // Keyboard state flags (volatile for thread visibility between EDT and GameLoopThread)
    private volatile boolean moveLeft;
    private volatile boolean moveRight;
    private volatile boolean jump;
    private volatile boolean crouch;
    private volatile boolean reloadRequested;
    private volatile boolean restartRequested;
    private volatile boolean toggleHelpRequested;
    private volatile boolean pauseRequested;
    private volatile boolean enterRequested;
    private volatile boolean cRequested;
    private volatile boolean num1Requested;
    private volatile boolean num2Requested;
    private volatile boolean num3Requested;
    private volatile boolean num4Requested;
    private volatile boolean menuRequested;
    private volatile boolean f1Requested;
    private volatile boolean f2Requested;
    private volatile boolean f3Requested;

    // Mouse state
    private volatile int mouseX;
    private volatile int mouseY;
    private volatile boolean mouseLeftPressed;
    private volatile boolean mouseClicked;

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
            case KeyEvent.VK_ENTER:
                enterRequested = true;
                break;
            case KeyEvent.VK_C:
                cRequested = true;
                break;
            case KeyEvent.VK_1:
            case KeyEvent.VK_NUMPAD1:
                num1Requested = true;
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_NUMPAD2:
                num2Requested = true;
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_NUMPAD3:
                num3Requested = true;
                break;
            case KeyEvent.VK_4:
            case KeyEvent.VK_NUMPAD4:
                num4Requested = true;
                break;
            case KeyEvent.VK_M:
                menuRequested = true;
                break;
            case KeyEvent.VK_F1:
                f1Requested = true;
                break;
            case KeyEvent.VK_F2:
                f2Requested = true;
                break;
            case KeyEvent.VK_F3:
                f3Requested = true;
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
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftPressed = true;
            mouseClicked = true;
            restartRequested = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftPressed = false;
            mouseClicked = true; // Also mark click on release for reliable click detection
        }
    }

    public void setMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseClicked = true;
        }
    }

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

    public boolean consumeMouseClick() {
        boolean c = mouseClicked;
        mouseClicked = false;
        return c;
    }

    public boolean consumeEnter() {
        boolean e = enterRequested;
        enterRequested = false;
        return e;
    }

    public boolean consumeColorCycle() {
        boolean c = cRequested;
        cRequested = false;
        return c;
    }

    public boolean consumeNumber1() {
        boolean n = num1Requested;
        num1Requested = false;
        return n;
    }

    public boolean consumeNumber2() {
        boolean n = num2Requested;
        num2Requested = false;
        return n;
    }

    public boolean consumeNumber3() {
        boolean n = num3Requested;
        num3Requested = false;
        return n;
    }

    public boolean consumeNumber4() {
        boolean n = num4Requested;
        num4Requested = false;
        return n;
    }

    public boolean consumeMenu() {
        boolean m = menuRequested;
        menuRequested = false;
        return m;
    }

    public boolean consumeF1() {
        boolean f = f1Requested;
        f1Requested = false;
        return f;
    }

    public boolean consumeF2() {
        boolean f = f2Requested;
        f2Requested = false;
        return f;
    }

    public boolean consumeF3() {
        boolean f = f3Requested;
        f3Requested = false;
        return f;
    }
}
