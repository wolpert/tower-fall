package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.effects.SquashStretch;
import com.codeheadsystems.towerstack.effects.TowerSway;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.model.Tower;
import java.util.List;

/**
 * The isometric cosmetic skin (build brief §11): the same one-axis gameplay, but each block is
 * drawn as a 3D cuboid under a 2:1 dimetric projection — a diamond top face plus the two
 * visible side faces, each shaded darker for depth.
 *
 * <p>Only the drawing changes. The block's game footprint spans its width on the slide axis and
 * a fixed {@link Tunables#ISO_DEPTH} into the scene. Projection is centered on the world so a
 * centered tower stays vertical. The vertical (height) term is preserved 1:1, so the existing
 * camera rise still parks the tower correctly.
 */
public class IsoBlockRenderer implements BlockRenderer {

    private final Color shade = new Color();

    @Override
    public void drawTower(ShapeRenderer shapes, Tower tower, SquashStretch squash, TowerSway sway) {
        List<Block> blocks = tower.blocks();
        int topIndex = blocks.size() - 1;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            float offsetX = sway.offsetAt(i, topIndex);
            if (i == topIndex) {
                drawBlock(shapes, block.centerX() + offsetX, block.getBottom(),
                        block.getWidth() * squash.scaleX(), block.getHeight() * squash.scaleY(),
                        block.getColor(), 1f);
            } else {
                drawBlock(shapes, block.centerX() + offsetX, block.getBottom(),
                        block.getWidth(), block.getHeight(), block.getColor(), 1f);
            }
        }
    }

    @Override
    public void drawMoving(ShapeRenderer shapes, Block moving, float offsetX) {
        drawBlock(shapes, moving.centerX() + offsetX, moving.getBottom(),
                moving.getWidth(), moving.getHeight(), moving.getColor(), 1f);
    }

    @Override
    public void drawGhost(ShapeRenderer shapes, float left, float bottom, float width, float height,
                          Color color, float alpha) {
        drawBlock(shapes, left + width / 2f, bottom, width, height, color, alpha);
    }

    @Override
    public Vector2 project(float gameX, float gameY, Vector2 out) {
        float z = Tunables.ISO_DEPTH / 2f; // effects sit at the block's mid-depth
        return out.set(projectX(gameX, z), projectY(gameX, z, gameY));
    }

    private void drawBlock(ShapeRenderer shapes, float centerX, float bottom,
                           float width, float height, Color base, float alpha) {
        float left = centerX - width / 2f;
        float right = centerX + width / 2f;
        float top = bottom + height;
        float depth = Tunables.ISO_DEPTH;

        // Top-face corners, projected. The two side faces are these edges dropped by `height`
        // (projectY adds the height term linearly, so a lower point is simply y - height).
        float ax = projectX(left, 0f);
        float ay = projectY(left, 0f, top);
        float bx = projectX(right, 0f);
        float by = projectY(right, 0f, top);
        float cx = projectX(right, depth);
        float cy = projectY(right, depth, top);
        float dx = projectX(left, depth);
        float dy = projectY(left, depth, top);

        // Front face (depth side): top edge d->c, dropped by height.
        shapes.setColor(shaded(base, Tunables.ISO_SHADE_FRONT, alpha));
        fillQuad(shapes, dx, dy, cx, cy, cx, cy - height, dx, dy - height);

        // Right face (right edge): top edge b->c, dropped by height.
        shapes.setColor(shaded(base, Tunables.ISO_SHADE_SIDE, alpha));
        fillQuad(shapes, bx, by, cx, cy, cx, cy - height, bx, by - height);

        // Top face last, so its edges sit cleanly over the sides.
        shapes.setColor(shaded(base, Tunables.ISO_SHADE_TOP, alpha));
        fillQuad(shapes, ax, ay, bx, by, cx, cy, dx, dy);
    }

    private float projectX(float gameX, float depth) {
        float centered = gameX - Tunables.WORLD_WIDTH / 2f;
        return Tunables.WORLD_WIDTH / 2f + (centered - depth) * Tunables.ISO_SCALE_X;
    }

    private float projectY(float gameX, float depth, float gameY) {
        float centered = gameX - Tunables.WORLD_WIDTH / 2f;
        return gameY + (centered + depth) * Tunables.ISO_SCALE_Y;
    }

    private Color shaded(Color base, float factor, float alpha) {
        return shade.set(base.r * factor, base.g * factor, base.b * factor, alpha);
    }

    private void fillQuad(ShapeRenderer shapes,
                          float x1, float y1, float x2, float y2,
                          float x3, float y3, float x4, float y4) {
        shapes.triangle(x1, y1, x2, y2, x3, y3);
        shapes.triangle(x1, y1, x3, y3, x4, y4);
    }
}
