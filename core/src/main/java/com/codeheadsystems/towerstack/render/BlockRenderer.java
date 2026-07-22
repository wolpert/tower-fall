package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.codeheadsystems.towerstack.effects.SquashStretch;
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

    void drawTower(ShapeRenderer shapes, Tower tower, SquashStretch squash);

    void drawMoving(ShapeRenderer shapes, Block moving);

    /**
     * Map a game-space point (x on the slide axis, y = height) to the render-space world
     * point where it visually appears, so effects (debris, bursts) can be spawned in the
     * right place in either view. Writes into and returns {@code out}.
     */
    Vector2 project(float gameX, float gameY, Vector2 out);
}
