package com.hardcoremario.core;

import java.awt.Graphics2D;

/**
 * Interface for any game object that can be rendered to the screen.
 */
public interface Renderable {
    void render(Graphics2D g, double offsetX, double offsetY);
}
