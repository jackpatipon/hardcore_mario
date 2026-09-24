package com.hardcoremario.view;

import com.hardcoremario.core.Renderable;
import com.hardcoremario.core.Updatable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Particle system for muzzle flashes, bullet casings, blood splatters, and impact sparks.
 */
public class ParticleSystem implements Updatable, Renderable {
    private static ParticleSystem instance;

    private static class Particle {
        double x, y;
        double vx, vy;
        Color color;
        double size;
        double life;
        double maxLife;
        boolean hasGravity;

        Particle(double x, double y, double vx, double vy, Color color, double size, double maxLife, boolean hasGravity) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.life = maxLife;
            this.maxLife = maxLife;
            this.hasGravity = hasGravity;
        }

        boolean update(double deltaTime) {
            life -= deltaTime;
            x += vx * deltaTime;
            y += vy * deltaTime;
            if (hasGravity) {
                vy += 650.0 * deltaTime;
            }
            return life <= 0;
        }

        void render(Graphics2D g, double offsetX, double offsetY) {
            int drawX = (int) (x - offsetX);
            int drawY = (int) (y - offsetY);
            float alpha = (float) Math.max(0, Math.min(1.0, life / maxLife));
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
            g.setColor(c);
            g.fillOval(drawX, drawY, (int) size, (int) size);
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    private ParticleSystem() {}

    public static synchronized ParticleSystem getInstance() {
        if (instance == null) {
            instance = new ParticleSystem();
        }
        return instance;
    }

    public void clear() {
        particles.clear();
    }

    public void spawnMuzzleFlash(double x, double y) {
        for (int i = 0; i < 5; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double spd = random.nextDouble() * 90.0 + 30.0;
            particles.add(new Particle(
                x, y,
                Math.cos(angle) * spd, Math.sin(angle) * spd,
                random.nextBoolean() ? Color.YELLOW : Color.ORANGE,
                random.nextInt(4) + 4,
                0.08,
                false
            ));
        }
    }

    public void spawnCasing(double x, double y, boolean left) {
        double vx = (left ? -1 : 1) * (random.nextDouble() * 80.0 + 60.0);
        double vy = -(random.nextDouble() * 120.0 + 100.0);
        particles.add(new Particle(
            x, y,
            vx, vy,
            new Color(255, 215, 0),
            4,
            0.5,
            true
        ));
    }

    public void spawnSparks(double x, double y, Color color) {
        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double spd = random.nextDouble() * 160.0 + 40.0;
            particles.add(new Particle(
                x, y,
                Math.cos(angle) * spd, Math.sin(angle) * spd,
                color,
                random.nextInt(3) + 3,
                0.3,
                true
            ));
        }
    }

    @Override
    public void update(double deltaTime) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            if (p.update(deltaTime)) {
                it.remove();
            }
        }
    }

    @Override
    public void render(Graphics2D g, double offsetX, double offsetY) {
        for (Particle p : particles) {
            p.render(g, offsetX, offsetY);
        }
    }
}
