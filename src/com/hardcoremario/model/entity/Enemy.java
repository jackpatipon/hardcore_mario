package com.hardcoremario.model.entity;

import com.hardcoremario.model.item.AmmoPack;
import com.hardcoremario.model.item.HealthPack;
import com.hardcoremario.model.world.Level;
import java.util.Random;

/**
 * Abstract class representing all enemies in the game.
 * Demonstrates Inheritance and Polymorphism.
 */
public abstract class Enemy extends LivingEntity {
    protected double sightRange;
    protected double attackCooldown;
    protected double attackTimer = 0.0;
    protected Random random = new Random();

    public Enemy(double x, double y, int width, int height, int maxHp, double sightRange, double attackCooldown) {
        super(x, y, width, height, maxHp);
        this.sightRange = sightRange;
        this.attackCooldown = attackCooldown;
    }

    public abstract void updateAI(Player player, Level level, double deltaTime);

    @Override
    protected void onDeath() {
        // Handled in Level to drop items and add player kill count
    }

    /**
     * Drops either a HealthPack or an AmmoPack with probability upon death.
     */
    public void dropLoot(Level level) {
        double roll = random.nextDouble();
        if (roll < 0.35) {
            level.addItem(new HealthPack(getCenterX() - 16, getY() + height - 32));
        } else if (roll < 0.70) {
            level.addItem(new AmmoPack(getCenterX() - 16, getY() + height - 32));
        }
    }
}
