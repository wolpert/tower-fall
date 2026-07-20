package com.codeheadsystems.towerstack.model;

/**
 * The outcome of dropping a block, computed by {@code SliceMath}.
 *
 * <ul>
 *   <li>{@link Type#MISS} — no overlap with the block beneath; the run ends.</li>
 *   <li>{@link Type#PARTIAL} — some overlap; the tower narrows to the surviving interval
 *       and the overhang ({@link #getSliceLeft()} / {@link #getSliceWidth()}) shears off.</li>
 *   <li>{@link Type#PERFECT} — edges aligned within tolerance; the full footprint survives
 *       and there is no overhang. Rewarded (combo + regrowth) starting in increment 3.</li>
 * </ul>
 *
 * <p>The surviving interval is what becomes the new tower top. The slice interval is the
 * shorn-off remainder of the dropped block, kept here so the juice pass can spawn tumbling
 * debris from it later; it is zero-width for a {@code PERFECT} or a dead-center drop.
 */
public class DropResult {

    public enum Type {
        MISS,
        PARTIAL,
        PERFECT
    }

    private final Type type;
    private final float survivingLeft;
    private final float survivingWidth;
    private final float sliceLeft;
    private final float sliceWidth;

    private DropResult(Type type,
                       float survivingLeft,
                       float survivingWidth,
                       float sliceLeft,
                       float sliceWidth) {
        this.type = type;
        this.survivingLeft = survivingLeft;
        this.survivingWidth = survivingWidth;
        this.sliceLeft = sliceLeft;
        this.sliceWidth = sliceWidth;
    }

    public static DropResult miss() {
        return new DropResult(Type.MISS, 0f, 0f, 0f, 0f);
    }

    public static DropResult partial(float survivingLeft,
                                     float survivingWidth,
                                     float sliceLeft,
                                     float sliceWidth) {
        return new DropResult(Type.PARTIAL, survivingLeft, survivingWidth, sliceLeft, sliceWidth);
    }

    public static DropResult perfect(float survivingLeft, float survivingWidth) {
        return new DropResult(Type.PERFECT, survivingLeft, survivingWidth, 0f, 0f);
    }

    public Type getType() {
        return type;
    }

    public boolean isMiss() {
        return type == Type.MISS;
    }

    public float getSurvivingLeft() {
        return survivingLeft;
    }

    public float getSurvivingWidth() {
        return survivingWidth;
    }

    public float getSliceLeft() {
        return sliceLeft;
    }

    public float getSliceWidth() {
        return sliceWidth;
    }
}
