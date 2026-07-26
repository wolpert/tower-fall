package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.render.BlockRenderer;

/**
 * A fading string of ghosts strung out behind the sliding block, so its speed is legible at a
 * glance and a fast block at the top of a tall tower feels fast. Freshly Squeezed only.
 *
 * <p>Positions are sampled on a fixed interval into a small fixed ring, oldest last, and drawn
 * through the active {@link BlockRenderer} so the ghosts match whichever view is on. At Crushed
 * and Ground the trail runs longer and the ghosts cycle through the spectrum instead of wearing
 * the block's color.
 */
public class MotionTrail {

    private static final class Sample {
        float left;
        float bottom;
        float width;
        float height;
        final Color color = new Color();
    }

    private final Sample[] ring = new Sample[Tunables.TRAIL_SAMPLES_WILD];
    private final Color scratch = new Color();
    private int count;
    private float sinceSample;
    private float elapsed; // drives the hue cycle

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    public MotionTrail() {
        for (int i = 0; i < ring.length; i++) {
            ring[i] = new Sample();
        }
    }

    /** How many ghosts this juice level keeps — the ring is allocated for the longest. */
    private int length() {
        return level.isOverTheTop() ? Tunables.TRAIL_SAMPLES_WILD : Tunables.TRAIL_SAMPLES;
    }

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            clear();
        }
    }

    /** Advance the sample clock, recording the block's current pose when one is due. */
    public void sample(float delta, Block moving) {
        if (!level.hasExtras()) {
            return;
        }
        elapsed += delta;
        sinceSample += delta;
        if (sinceSample < Tunables.TRAIL_INTERVAL) {
            return;
        }
        sinceSample = 0f;
        push(moving);
    }

    /** Shift the ring down one and write the newest pose at the front (no allocation). */
    private void push(Block moving) {
        int length = length();
        for (int i = Math.min(count, length - 1); i > 0; i--) {
            copy(ring[i - 1], ring[i]);
        }
        Sample head = ring[0];
        head.left = moving.getLeft();
        head.bottom = moving.getBottom();
        head.width = moving.getWidth();
        head.height = moving.getHeight();
        head.color.set(moving.getColor());
        count = Math.min(count + 1, length);
    }

    private void copy(Sample from, Sample to) {
        to.left = from.left;
        to.bottom = from.bottom;
        to.width = from.width;
        to.height = from.height;
        to.color.set(from.color);
    }

    /**
     * Draw the ghosts, faintest first.
     *
     * @param offsetX the tower sway offset the live block is being drawn with, so the trail
     *                leans along with it
     */
    public void draw(ShapeRenderer shapes, BlockRenderer renderer, float offsetX) {
        int length = length();
        for (int i = Math.min(count, length) - 1; i >= 0; i--) {
            Sample sample = ring[i];
            float fade = 1f - (float) i / length; // index 0 is the newest, so the brightest
            float alpha = Math.min(1f, Tunables.TRAIL_ALPHA * fade * fade * level.getIntensity());
            renderer.drawGhost(shapes, sample.left + offsetX, sample.bottom,
                    sample.width, sample.height, ghostColor(sample, i), alpha);
        }
    }

    private Color ghostColor(Sample sample, int index) {
        if (!level.isOverTheTop()) {
            return sample.color;
        }
        float hue = (elapsed * Tunables.TRAIL_RAINBOW_SPEED
                + index * Tunables.TRAIL_RAINBOW_STEP) % 360f;
        scratch.set(0f, 0f, 0f, 1f);
        scratch.fromHsv(hue, 0.75f, 1f);
        return scratch;
    }

    public void clear() {
        count = 0;
        sinceSample = 0f;
    }
}
