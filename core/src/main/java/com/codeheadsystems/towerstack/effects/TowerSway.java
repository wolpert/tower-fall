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
 */
public class TowerSway {

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    private float time = -1f; // negative = at rest
    private float amplitude;

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

    public void update(float delta) {
        if (time >= 0f) {
            time += delta;
            if (Math.abs(current()) < 0.05f && time > 0.5f) {
                clear(); // settled below anything visible
            }
        }
    }

    /**
     * Horizontal offset for the block at {@code index} in a tower whose top block is
     * {@code topIndex}. The moving block sits at {@code topIndex + 1} and gets the full lean.
     */
    public float offsetAt(int index, int topIndex) {
        if (time < 0f) {
            return 0f;
        }
        float fromTop = Math.max(0f, topIndex - index);
        float span = 1f - Math.min(1f, fromTop / Tunables.SWAY_SPAN_BLOCKS);
        float rooted = Math.min(1f, index / Tunables.SWAY_ROOT_BLOCKS);
        return current() * span * span * rooted;
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
    }
}
