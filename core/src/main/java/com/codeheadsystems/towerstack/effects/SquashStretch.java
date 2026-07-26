package com.codeheadsystems.towerstack.effects;

import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Landing squash & stretch (build brief §6). When a block lands it compresses vertically and
 * bulges horizontally, then springs back past its resting size and settles — a damped spring.
 *
 * <p>Tracks a single animation (the most recently landed block). Anchored at the block's
 * bottom-center by whoever draws it: apply {@link #scaleX()} / {@link #scaleY()} about that
 * point. When inactive both scales are 1, so the draw path is unconditional.
 */
public class SquashStretch {

    private float time = -1f; // negative = inactive
    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    /** Scale the squash with the juice setting; None flattens it away entirely. */
    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.isOn()) {
            time = -1f;
        }
    }

    /** Fire the squash from the moment of impact. */
    public void trigger() {
        if (!level.isOn()) {
            return;
        }
        time = 0f;
    }

    public void update(float delta) {
        if (time >= 0f) {
            time += delta;
            if (time >= Tunables.SQUASH_DURATION) {
                time = -1f;
            }
        }
    }

    public float scaleX() {
        return 1f + amplitude() * 0.5f * wave();
    }

    public float scaleY() {
        return 1f - amplitude() * wave();
    }

    private float amplitude() {
        return Tunables.SQUASH_AMPLITUDE * level.getIntensity();
    }

    /** Damped cosine: 1 at impact (fully squashed), oscillating to 0 as it settles. */
    private float wave() {
        if (time < 0f) {
            return 0f;
        }
        return (float) (Math.exp(-Tunables.SQUASH_DECAY * time)
                * Math.cos(Tunables.SQUASH_FREQUENCY * time));
    }
}
