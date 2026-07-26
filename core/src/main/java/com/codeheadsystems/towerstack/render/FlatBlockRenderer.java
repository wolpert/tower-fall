package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.codeheadsystems.towerstack.effects.SquashStretch;
import com.codeheadsystems.towerstack.effects.TowerSway;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.model.Tower;
import java.util.List;

/**
 * The default flat side view: each block is a plain filled rectangle. The most-recently-landed
 * block (the current top) is drawn through the active {@link SquashStretch}, anchored at its
 * bottom-center; all other blocks draw at rest. Every block is nudged sideways by the current
 * {@link TowerSway}, which is zero unless the top juice level is on.
 *
 * <p>In this view game space and render space are identical, so {@link #project} is the identity.
 */
public class FlatBlockRenderer implements BlockRenderer {

    private final Color scratch = new Color();

    @Override
    public void drawTower(ShapeRenderer shapes, Tower tower, SquashStretch squash, TowerSway sway) {
        List<Block> blocks = tower.blocks();
        int topIndex = blocks.size() - 1;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            float offsetX = sway.offsetAt(i, topIndex);
            if (i == topIndex) {
                drawSquashed(shapes, block, squash, offsetX);
            } else {
                drawPlain(shapes, block, offsetX);
            }
        }
    }

    @Override
    public void drawMoving(ShapeRenderer shapes, Block moving, float offsetX) {
        drawPlain(shapes, moving, offsetX);
    }

    @Override
    public void drawGhost(ShapeRenderer shapes, float left, float bottom, float width, float height,
                          Color color, float alpha) {
        scratch.set(color.r, color.g, color.b, alpha);
        shapes.setColor(scratch);
        shapes.rect(left, bottom, width, height);
    }

    @Override
    public Vector2 project(float gameX, float gameY, Vector2 out) {
        return out.set(gameX, gameY);
    }

    private void drawPlain(ShapeRenderer shapes, Block block, float offsetX) {
        shapes.setColor(block.getColor());
        shapes.rect(block.getLeft() + offsetX, block.getBottom(), block.getWidth(), block.getHeight());
    }

    private void drawSquashed(ShapeRenderer shapes, Block block, SquashStretch squash, float offsetX) {
        float width = block.getWidth() * squash.scaleX();
        float height = block.getHeight() * squash.scaleY();
        float left = block.centerX() + offsetX - width / 2f; // anchored at bottom-center
        shapes.setColor(block.getColor());
        shapes.rect(left, block.getBottom(), width, height);
    }
}
