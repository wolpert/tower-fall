package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * The little puff of dust squeezed sideways out of the seam every time a block lands — perfect
 * or not, which is the point: it gives the ordinary placement (previously just a click and a
 * squash) something of its own. Freshly Squeezed only.
 *
 * <p>Particles are seeded across the width of the seam, kicked outward from its center, and
 * pulled down by a gentle gravity while shrinking and fading.
 */
public class DustPuff {

    private static final class Mote {
        float x;
        float y;
        float velocityX;
        float velocityY;
        float life;
        final float maxLife;
        final Color color;

        Mote(float x, float y, float velocityX, float velocityY, float life, Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
            this.maxLife = life;
            this.color = color;
        }
    }

    private final Array<Mote> motes = new Array<>();
    private final Color scratch = new Color();

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            clear();
        }
    }

    /**
     * Puff along a seam.
     *
     * @param centerX seam center in render space
     * @param y       seam height in render space
     * @param width   how wide the landing footprint is (motes spread across it)
     */
    public void puff(float centerX, float y, float width, Color color) {
        if (!level.hasExtras()) {
            return;
        }
        int count = Math.max(1, Math.round(Tunables.DUST_COUNT * level.getIntensity()));
        // One lightened copy shared by this puff's motes: the block's own color would vanish
        // against the block the dust is drawn over.
        Color tinted = new Color(color).lerp(Color.WHITE, Tunables.DUST_TINT);
        for (int i = 0; i < count; i++) {
            float offset = MathUtils.random(-0.5f, 0.5f) * width;
            float outward = Math.signum(offset == 0f ? MathUtils.randomSign() : offset);
            motes.add(new Mote(
                    centerX + offset, y,
                    outward * Tunables.DUST_SPEED * MathUtils.random(0.4f, 1.1f),
                    Tunables.DUST_RISE * MathUtils.random(0.3f, 1f),
                    Tunables.DUST_LIFE * MathUtils.random(0.6f, 1f),
                    tinted));
        }
    }

    public void update(float delta) {
        for (int i = motes.size - 1; i >= 0; i--) {
            Mote mote = motes.get(i);
            mote.life -= delta;
            if (mote.life <= 0f) {
                motes.removeIndex(i);
                continue;
            }
            mote.velocityY -= Tunables.DUST_GRAVITY * delta;
            mote.x += mote.velocityX * delta;
            mote.y += mote.velocityY * delta;
        }
    }

    public void draw(ShapeRenderer shapes) {
        for (Mote mote : motes) {
            float remaining = mote.life / mote.maxLife;
            float size = Tunables.DUST_SIZE * remaining; // shrink as it fades
            scratch.set(mote.color.r, mote.color.g, mote.color.b, Tunables.DUST_ALPHA * remaining);
            shapes.setColor(scratch);
            shapes.rect(mote.x - size / 2f, mote.y - size / 2f, size, size);
        }
    }

    public void clear() {
        motes.clear();
    }
}
