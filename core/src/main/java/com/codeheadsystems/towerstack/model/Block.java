package com.codeheadsystems.towerstack.model;

import com.badlogic.gdx.graphics.Color;

/**
 * A single axis-aligned rectangular block, described by its left edge, bottom edge,
 * width and height in world units, plus a fill color.
 *
 * <p>This is plain data. All slicing/overlap arithmetic lives in {@code SliceMath} and
 * operates on raw intervals, so the interesting logic is unit-testable without touching
 * libGDX.
 */
public class Block {

    private float left;
    private float bottom;
    private float width;
    private final float height;
    private final Color color;

    public Block(float left, float bottom, float width, float height, Color color) {
        this.left = left;
        this.bottom = bottom;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getBottom() {
        return bottom;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }

    public float right() {
        return left + width;
    }

    public float topEdge() {
        return bottom + height;
    }

    public float centerX() {
        return left + width / 2f;
    }
}
