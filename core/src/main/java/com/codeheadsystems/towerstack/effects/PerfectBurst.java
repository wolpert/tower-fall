package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * The perfect-placement celebration (build brief §6): a radial particle pop plus an expanding
 * horizontal shockwave line at the seam. Intensity ramps with the combo — more particles, a
 * brighter, wider shockwave — so a streak visibly builds (the "combo feedback ramp").
 */
public class PerfectBurst {

    private static final class Particle {
        float x;
        float y;
        float velocityX;
        float velocityY;
        float life;
        final float maxLife;
        final Color color;

        Particle(float x, float y, float velocityX, float velocityY, float life, Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
            this.maxLife = life;
            this.color = color;
        }
    }

    private final Array<Particle> particles = new Array<>();
    private final Color scratch = new Color();

    private float shockX;
    private float shockY;
    private float shockTime = -1f; // negative = no active shockwave
    private float shockIntensity;

    /**
     * Fire a burst at a seam point.
     *
     * @param combo the current combo length; scales particle count and shockwave brightness
     */
    public void trigger(float x, float y, int combo, Color color) {
        int count = Math.min(
                Tunables.BURST_BASE_PARTICLES + combo * Tunables.BURST_PER_COMBO,
                Tunables.BURST_MAX_PARTICLES);
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.PI2 * i / count + MathUtils.random(-0.15f, 0.15f);
            float speed = Tunables.BURST_PARTICLE_SPEED * MathUtils.random(0.4f, 1f);
            float life = Tunables.BURST_PARTICLE_LIFE * MathUtils.random(0.7f, 1f);
            particles.add(new Particle(
                    x, y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    life, color));
        }

        shockX = x;
        shockY = y;
        shockTime = 0f;
        shockIntensity = Math.min(1f, 0.4f + combo * 0.15f);
    }

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= delta;
            if (p.life <= 0f) {
                particles.removeIndex(i);
                continue;
            }
            p.velocityY -= Tunables.BURST_GRAVITY * delta;
            p.x += p.velocityX * delta;
            p.y += p.velocityY * delta;
        }

        if (shockTime >= 0f) {
            shockTime += delta;
            if (shockTime >= Tunables.SHOCKWAVE_LIFE) {
                shockTime = -1f;
            }
        }
    }

    public void draw(ShapeRenderer shapes) {
        drawShockwave(shapes);
        drawParticles(shapes);
    }

    private void drawParticles(ShapeRenderer shapes) {
        float size = Tunables.BURST_PARTICLE_SIZE;
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            scratch.set(p.color.r, p.color.g, p.color.b, alpha);
            shapes.setColor(scratch);
            shapes.rect(p.x - size / 2f, p.y - size / 2f, size, size);
        }
    }

    private void drawShockwave(ShapeRenderer shapes) {
        if (shockTime < 0f) {
            return;
        }
        float progress = shockTime / Tunables.SHOCKWAVE_LIFE;
        float halfLength = Tunables.SHOCKWAVE_SPEED * shockTime;
        float alpha = (1f - progress) * shockIntensity;
        scratch.set(1f, 1f, 1f, alpha);
        shapes.setColor(scratch);
        shapes.rect(
                shockX - halfLength, shockY - Tunables.SHOCKWAVE_THICKNESS / 2f,
                halfLength * 2f, Tunables.SHOCKWAVE_THICKNESS);
    }

    public void clear() {
        particles.clear();
        shockTime = -1f;
    }
}
