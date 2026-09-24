package com.hardcoremario.model.projectile;

import com.hardcoremario.core.AssetManager;
import com.hardcoremario.model.entity.Entity;
import com.hardcoremario.model.entity.LivingEntity;
import com.hardcoremario.util.Vector2D;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Abstract class for all projectile objects (bullets, rockets, etc.).
 */
public abstract class Projectile extends Entity {
    protected double dirX;
    protected double dirY;
    protected double speed;
    protected int damage;
    protected LivingEntity shooter;
    protected double lifeTime = 3.5; // Max seconds before despawn

    public Projectile(double x, double y, int width, int height, double dirX, double dirY, double speed, int damage, LivingEntity shooter) {
        super(x, y, width, height);
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
        this.damage = damage;
        this.shooter = shooter;
        this.velocity.set(dirX * speed, dirY * speed);
    }

    public int getDamage() { return damage; }
    public LivingEntity getShooter() { return shooter; }

    @Override
    public void update(double deltaTime) {
        position.add(velocity.getX() * deltaTime, velocity.getY() * deltaTime);
        lifeTime -= deltaTime;
        if (lifeTime <= 0) {
            active = false;
        }
    }
}
