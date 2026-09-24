package com.hardcoremario.core;

/**
 * Interface for entities that possess health points and can be attacked.
 */
public interface Damageable {
    void takeDamage(int amount);
    void heal(int amount);
    boolean isDead();
    int getHp();
    int getMaxHp();
}
