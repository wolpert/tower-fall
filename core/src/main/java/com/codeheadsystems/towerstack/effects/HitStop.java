package com.codeheadsystems.towerstack.effects;

import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Hit-stop, the cheapest big win in game feel: on a heavy impact the world all but freezes for
 * a few dozen milliseconds, so the hit lands before anything moves on. Freshly Squeezed only.
 *
 * <p>Rather than gating each system, the screen runs its per-frame updates on the delta this
 * class hands back — {@link #scale(float)} returns a shrunken delta while a stop is active and
 * the real one otherwise. Input is deliberately read on the real clock, so a stop never eats a
 * tap.
 */
public class HitStop {

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;
    private float remaining;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            remaining = 0f;
        }
    }

    /** Freeze for {@code duration} seconds. Overlapping stops take the longer of the two. */
    public void trigger(float duration) {
        if (!level.hasExtras()) {
            return;
        }
        remaining = Math.max(remaining, duration);
    }

    /**
     * Consume a frame and return the delta the world should advance by.
     *
     * @param delta the real frame time
     * @return {@code delta} scaled down while a stop is active, unchanged otherwise
     */
    public float scale(float delta) {
        if (remaining <= 0f) {
            return delta;
        }
        remaining -= delta; // the stop itself is timed on the real clock, so it always ends
        return delta * Tunables.HITSTOP_TIME_SCALE;
    }

    public boolean isActive() {
        return remaining > 0f;
    }

    public void clear() {
        remaining = 0f;
    }
}
