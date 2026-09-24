package com.hardcoremario.core;

/**
 * Interface for game objects that need periodic state updates every frame.
 */
public interface Updatable {
    void update(double deltaTime);
}
