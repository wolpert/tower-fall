package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Manages all live {@link Debris} shards: spawns them from a sliced overhang (or a missed
 * block), advances them under gravity, and drops them once they fall out of view.
 */
public class DebrisField {

    private final Array<Debris> shards = new Array<>();

    /**
     * Spawn a shard flung away from the tower.
     *
     * @param direction {@code -1} to fly left, {@code +1} to fly right
     */
    public void spawn(float left, float bottom, float width, float height, Color color, int direction) {
        float velocityX = direction * Tunables.DEBRIS_OUTWARD_SPEED * MathUtils.random(0.7f, 1.2f);
        float velocityY = Tunables.DEBRIS_POP_SPEED * MathUtils.random(0.6f, 1.1f);
        float angularVelocity = -direction * Tunables.DEBRIS_SPIN * MathUtils.random(0.6f, 1.4f);
        shards.add(new Debris(left, bottom, width, height, color, velocityX, velocityY, angularVelocity));
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
