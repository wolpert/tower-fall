package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.codeheadsystems.towerstack.effects.SquashStretch;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.model.Tower;
import java.util.List;

/**
 * The default flat side view: each block is a plain filled rectangle. The most-recently-landed
 * block (the current top) is drawn through the active {@link SquashStretch}, anchored at its
 * bottom-center; all other blocks draw at rest.
 *
 * <p>In this view game space and render space are identical, so {@link #project} is the identity.
 */
public class FlatBlockRenderer implements BlockRenderer {

    @Override
    public void drawTower(ShapeRenderer shapes, Tower tower, SquashStretch squash) {
        List<Block> blocks = tower.blocks();
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (i == blocks.size() - 1) {
                drawSquashed(shapes, block, squash);
            } else {
                drawPlain(shapes, block);
            }
        }
    }

    @Override
    public void drawMoving(ShapeRenderer shapes, Block moving) {
        drawPlain(shapes, moving);
    }

    @Override
    public Vector2 project(float gameX, float gameY, Vector2 out) {
        return out.set(gameX, gameY);
    }

    private void drawPlain(ShapeRenderer shapes, Block block) {
        shapes.setColor(block.getColor());
        shapes.rect(block.getLeft(), block.getBottom(), block.getWidth(), block.getHeight());
    }

    private void drawSquashed(ShapeRenderer shapes, Block block, SquashStretch squash) {
        float width = block.getWidth() * squash.scaleX();
        float height = block.getHeight() * squash.scaleY();
        float left = block.centerX() - width / 2f; // anchored at bottom-center
        shapes.setColor(block.getColor());
        shapes.rect(left, block.getBottom(), width, height);
    }
}
