package com.codeheadsystems.towerstack.effects;

import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * A damped horizontal lean through the top of the stack, kicked every time a block lands (and
 * kicked hard on a miss). Freshly Squeezed only.
 *
 * <p>The tower reads as a tall, slightly springy structure rather than a rigid column: the
 * displacement is a decaying sine, largest at the crown and falling to nothing
 * {@link Tunables#SWAY_SPAN_BLOCKS} blocks down, and the planted base never moves. Purely
 * cosmetic — slice math still runs on the true, unswayed positions.
 *
 * <p>At Crushed and Ground a second, angrier component joins it: a {@linkplain #rattle rattle}
 * that shakes every block at once, phase-shifted per block so the shock visibly travels down
 * the stack. A miss sets the whole tower shuddering.
 */
public class TowerSway {

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    private float time = -1f; // negative = at rest
    private float amplitude;

    private float rattleTime = -1f;
    private float rattleAmplitude;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            clear();
        }
    }

    /**
     * Set the stack swinging.
     *
     * @param strength peak lean at the crown, in world units (before the juice intensity scale)
     * @param direction {@code -1} to lean left first, {@code +1} to lean right
     */
    public void kick(float strength, int direction) {
        if (!level.hasExtras()) {
            return;
        }
        time = 0f;
        amplitude = strength * direction * level.getIntensity();
    }

    /**
     * Shake the whole stack, not just the crown — the over-the-top answer to a miss.
     *
     * @param strength peak shake in world units (before the juice intensity scale)
     */
    public void rattle(float strength) {
        if (!level.isOverTheTop()) {
            return;
        }
        rattleTime = 0f;
        rattleAmplitude = strength * level.getIntensity();
    }

    public void update(float delta) {
        if (time >= 0f) {
            time += delta;
            if (Math.abs(current()) < 0.05f && time > 0.5f) {
                time = -1f; // settled below anything visible (the rattle keeps its own clock)
                amplitude = 0f;
            }
        }
        if (rattleTime >= 0f) {
            rattleTime += delta;
            if (rattleAmplitude * Math.exp(-Tunables.RATTLE_DECAY * rattleTime) < 0.05f) {
                rattleTime = -1f;
            }
        }
    }

    /**
     * Horizontal offset for the block at {@code index} in a tower whose top block is
     * {@code topIndex}. The moving block sits at {@code topIndex + 1} and gets the full lean.
     */
    public float offsetAt(int index, int topIndex) {
        float rooted = Math.min(1f, index / Tunables.SWAY_ROOT_BLOCKS);
        if (rooted <= 0f) {
            return 0f; // the base is planted, whatever is happening above it
        }

        float offset = 0f;
        if (time >= 0f) {
            float fromTop = Math.max(0f, topIndex - index);
            float span = 1f - Math.min(1f, fromTop / Tunables.SWAY_SPAN_BLOCKS);
            offset += current() * span * span;
        }
        if (rattleTime >= 0f) {
            // No falloff with height — the whole stack shakes — but each block a little behind
            // the one below it, so the shock reads as travelling upward.
            offset += rattleAmplitude
                    * (float) Math.exp(-Tunables.RATTLE_DECAY * rattleTime)
                    * (float) Math.sin(Tunables.RATTLE_FREQUENCY * rattleTime
                            - index * Tunables.RATTLE_PHASE_PER_BLOCK);
        }
        return offset * rooted;
    }

    /** Damped sine: 0 at the moment of impact, swinging out and settling back. */
    private float current() {
        if (time < 0f) {
            return 0f;
        }
        return (float) (amplitude
                * Math.exp(-Tunables.SWAY_DECAY * time)
                * Math.sin(Tunables.SWAY_FREQUENCY * time));
    }

    public void clear() {
        time = -1f;
        amplitude = 0f;
        rattleTime = -1f;
        rattleAmplitude = 0f;
    }
}
