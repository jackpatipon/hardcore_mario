package com.hardcoremario.model.entity;

import com.hardcoremario.core.Damageable;

/**
 * Abstract class for entities with health points, damage handling, and ground state.
 * Demonstrates Inheritance and Polymorphism.
 */
public abstract class LivingEntity extends Entity implements Damageable {
    protected int currentHp;
    protected int maxHp;
    protected boolean facingRight = true;
    protected boolean isGrounded = false;
    protected double invulnerableTimer = 0.0;

    public LivingEntity(double x, double y, int width, int height, int maxHp) {
        super(x, y, width, height);
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    @Override
    public void takeDamage(int amount) {
        if (invulnerableTimer > 0 || isDead()) return;
        currentHp = Math.max(0, currentHp - amount);
        invulnerableTimer = 0.2; // 200ms brief invulnerability
        if (currentHp <= 0) {
            onDeath();
        }
    }

    @Override
    public void heal(int amount) {
        if (isDead()) return;
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    @Override
    public boolean isDead() {
        return currentHp <= 0;
    }

    @Override
    public int getHp() { return currentHp; }

    @Override
    public int getMaxHp() { return maxHp; }

    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }

    public boolean isGrounded() { return isGrounded; }
    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }

    public boolean isInvulnerable() { return invulnerableTimer > 0; }

    protected abstract void onDeath();
}
