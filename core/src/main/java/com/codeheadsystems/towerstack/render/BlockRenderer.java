package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.codeheadsystems.towerstack.effects.SquashStretch;
import com.codeheadsystems.towerstack.effects.TowerSway;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.model.Tower;

/**
 * Draws the tower and moving block. Two implementations — {@link FlatBlockRenderer} and
 * {@link IsoBlockRenderer} — let the same gameplay render as a flat side view or an isometric
 * 3D skin (build brief §11); {@code PlayScreen} picks one from the player's setting.
 *
 * <p>All methods draw into a caller-managed {@link ShapeRenderer} (already begun in
 * {@code Filled} mode with the world projection set), so block, debris and burst rendering
 * share one batch.
 */
public interface BlockRenderer {

    /** Draw the stack, leaning each block by its {@link TowerSway} offset (zero when at rest). */
    void drawTower(ShapeRenderer shapes, Tower tower, SquashStretch squash, TowerSway sway);

    /** Draw the sliding block, nudged by {@code offsetX} so it leans with the tower's crown. */
    void drawMoving(ShapeRenderer shapes, Block moving, float offsetX);

    /**
     * Draw a translucent copy of a block — one frame of the sliding block's motion trail. Takes
     * loose geometry rather than a {@link Block} so the trail can keep its own light samples.
     */
    void drawGhost(ShapeRenderer shapes, float left, float bottom, float width, float height,
                   Color color, float alpha);

    /**
     * Map a game-space point (x on the slide axis, y = height) to the render-space world
     * point where it visually appears, so effects (debris, bursts) can be spawned in the
     * right place in either view. Writes into and returns {@code out}.
     */
    Vector2 project(float gameX, float gameY, Vector2 out);
}
