package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Manages all live {@link Debris} shards: spawns them from a sliced overhang (or a missed
 * block), advances them under gravity, and drops them once they fall out of view.
 *
 * <p>At the top juice level a shorn piece {@linkplain #shatter shatters} into several shards
 * that tumble apart instead of sailing off as one slab; at None nothing is spawned at all.
 */
public class DebrisField {

    private final Array<Debris> shards = new Array<>();

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.isOn()) {
            clear();
        }
    }

    /**
     * Spawn a shard flung away from the tower.
     *
     * @param direction {@code -1} to fly left, {@code +1} to fly right
     */
    public void spawn(float left, float bottom, float width, float height, Color color, int direction) {
        if (!level.isOn()) {
            return;
        }
        float velocityX = direction * Tunables.DEBRIS_OUTWARD_SPEED * MathUtils.random(0.7f, 1.2f);
        float velocityY = Tunables.DEBRIS_POP_SPEED * MathUtils.random(0.6f, 1.1f);
        float angularVelocity = -direction * Tunables.DEBRIS_SPIN * MathUtils.random(0.6f, 1.4f);
        shards.add(new Debris(left, bottom, width, height, color, velocityX, velocityY, angularVelocity));
    }

    /**
     * Break a piece into several shards, each flung and spun on its own — the pieces further out
     * fly harder, so the slab visibly comes apart. Falls back to a single {@link #spawn} when
     * the extras are off or the piece is too narrow to split.
     */
    public void shatter(float left, float bottom, float width, float height, Color color, int direction) {
        int pieces = (int) Math.min(Tunables.SHATTER_MAX_PIECES, width / Tunables.SHATTER_MIN_PIECE);
        if (!level.hasExtras() || pieces < 2) {
            spawn(left, bottom, width, height, color, direction);
            return;
        }
        float pieceWidth = width / pieces;
        for (int i = 0; i < pieces; i++) {
            // 0 at the inner edge, 1 at the outer: the outermost piece is thrown hardest.
            float outwardness = direction > 0
                    ? (i + 0.5f) / pieces
                    : 1f - (i + 0.5f) / pieces;
            float velocityX = direction * Tunables.DEBRIS_OUTWARD_SPEED
                    * (0.5f + outwardness) * MathUtils.random(0.8f, 1.3f);
            float velocityY = Tunables.DEBRIS_POP_SPEED * MathUtils.random(0.5f, 1.4f);
            float angularVelocity = -direction * Tunables.DEBRIS_SPIN * MathUtils.random(0.5f, 1.8f);
            shards.add(new Debris(left + i * pieceWidth, bottom, pieceWidth, height, color,
                    velocityX, velocityY, angularVelocity));
        }
    }

    public void update(float delta, float viewBottom) {
        for (int i = shards.size - 1; i >= 0; i--) {
            Debris shard = shards.get(i);
            shard.update(delta);
            if (shard.isBelow(viewBottom)) {
                shards.removeIndex(i);
            }
        }
    }

    public void draw(ShapeRenderer shapes) {
        for (Debris shard : shards) {
            shard.draw(shapes);
        }
    }

    public void clear() {
        shards.clear();
    }
}
