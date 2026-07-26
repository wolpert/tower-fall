package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * A shower of spinning paper for the combo milestone flare. Crushed and Ground only.
 *
 * <p>Flakes are tracked in screen space — x across the world width, y measured up from the
 * bottom of the view — and drawn with the view's bottom edge added back on, so they rain down
 * the screen at a fixed place instead of scrolling away with the tower. They still ride the
 * camera's shake and roll, which is the point.
 */
public class Confetti {

    private static final Color[] PALETTE = {
        new Color(1f, 0.83f, 0.30f, 1f),
        new Color(1f, 0.38f, 0.52f, 1f),
        new Color(0.40f, 0.83f, 1f, 1f),
        new Color(0.66f, 1f, 0.52f, 1f),
        new Color(1f, 1f, 1f, 1f),
    };

    private static final class Flake {
        float x;
        float y;
        float fallSpeed;
        float swayPhase;
        float rotation;
        float spin;
        float life;
        final float maxLife;
        final float size;
        final Color color;

        Flake(float x, float y, float fallSpeed, float spin, float size, float life, Color color) {
            this.x = x;
            this.y = y;
            this.fallSpeed = fallSpeed;
            this.swayPhase = MathUtils.random(MathUtils.PI2);
            this.rotation = MathUtils.random(360f);
            this.spin = spin;
            this.life = life;
            this.maxLife = life;
            this.size = size;
            this.color = color;
        }
    }

    private final Array<Flake> flakes = new Array<>();
    private final Color scratch = new Color();

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;
    private float elapsed;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.isOverTheTop()) {
            clear();
        }
    }

    /** Drop a load of confetti in from above the top of the view. */
    public void burst() {
        if (!level.isOverTheTop()) {
            return;
        }
        for (int i = 0; i < Tunables.CONFETTI_COUNT; i++) {
            flakes.add(new Flake(
                    MathUtils.random(Tunables.WORLD_WIDTH),
                    Tunables.WORLD_HEIGHT + MathUtils.random(Tunables.CONFETTI_SPAWN_BAND),
                    Tunables.CONFETTI_FALL_SPEED * MathUtils.random(0.7f, 1.4f),
                    Tunables.CONFETTI_SPIN * MathUtils.random(-1.4f, 1.4f),
                    Tunables.CONFETTI_SIZE * MathUtils.random(0.7f, 1.2f),
                    Tunables.CONFETTI_LIFE * MathUtils.random(0.8f, 1.2f),
                    PALETTE[MathUtils.random(PALETTE.length - 1)]));
        }
    }

    public void update(float delta) {
        elapsed += delta;
        for (int i = flakes.size - 1; i >= 0; i--) {
            Flake flake = flakes.get(i);
            flake.life -= delta;
            if (flake.life <= 0f || flake.y < -Tunables.CONFETTI_SIZE * 2f) {
                flakes.removeIndex(i);
                continue;
            }
            flake.y -= flake.fallSpeed * delta;
            flake.x += MathUtils.sin(elapsed * Tunables.CONFETTI_SWAY_RATE + flake.swayPhase)
                    * Tunables.CONFETTI_SWAY * delta;
            flake.rotation += flake.spin * delta;
        }
    }

    /**
     * @param viewBottom world-y of the bottom of the view, added to each flake's screen-space y
     */
    public void draw(ShapeRenderer shapes, float viewBottom) {
        for (Flake flake : flakes) {
            float fade = Math.min(1f, flake.life / (flake.maxLife * 0.3f)); // fade out at the end
            scratch.set(flake.color.r, flake.color.g, flake.color.b, fade);
            shapes.setColor(scratch);
            float width = flake.size;
            float height = flake.size * 0.6f;
            shapes.rect(flake.x - width / 2f, viewBottom + flake.y - height / 2f,
                    width / 2f, height / 2f, width, height, 1f, 1f, flake.rotation);
        }
    }

    public void clear() {
        flakes.clear();
    }
}
