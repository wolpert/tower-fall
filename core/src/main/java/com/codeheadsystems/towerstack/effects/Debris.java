package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * One tumbling shard — the shorn-off overhang (or a missed block) flung outward and spinning
 * as it falls off-screen (build brief §6, "slice tumble"). Purely cosmetic: no collision.
 */
public class Debris {

    private float x; // bottom-left corner
    private float y;
    private final float width;
    private final float height;
    private final Color color;

    private float velocityX;
    private float velocityY;
    private float rotation;
    private float angularVelocity;

    public Debris(float x, float y, float width, float height, Color color,
                  float velocityX, float velocityY, float angularVelocity) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.angularVelocity = angularVelocity;
    }

    public void update(float delta) {
        velocityY -= Tunables.DEBRIS_GRAVITY * delta;
        x += velocityX * delta;
        y += velocityY * delta;
        rotation += angularVelocity * delta;
    }

    /** True once the shard has fallen fully below the given world-y (the view bottom). */
    public boolean isBelow(float worldY) {
        return y + height < worldY - Tunables.DEBRIS_CULL_MARGIN;
    }

    public void draw(ShapeRenderer shapes) {
        shapes.setColor(color);
        // Rotate about the shard's center.
        shapes.rect(x, y, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
    }
}
