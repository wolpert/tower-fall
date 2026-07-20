package com.codeheadsystems.towerstack.model;

/**
 * The heart of the game: given where a block was dropped and the footprint it landed on,
 * work out what survives and what shears away.
 *
 * <p>Pure functions over raw intervals — no libGDX, no state — so this can be exercised
 * directly by unit tests. A block is described by a left edge and a width; its right edge
 * is {@code left + width}.
 */
public final class SliceMath {

    private SliceMath() {
        // Static utility.
    }

    /**
     * Slice a dropped block against the block beneath it.
     *
     * @param dropLeft   left edge of the dropped block
     * @param dropWidth  width of the dropped block
     * @param topLeft    left edge of the block beneath (the current tower top)
     * @param topWidth   width of the block beneath
     * @param perfectTolerance edge alignment within this distance counts as a perfect drop
     * @return the {@link DropResult}: miss, partial (with overhang), or perfect
     */
    public static DropResult slice(float dropLeft,
                                   float dropWidth,
                                   float topLeft,
                                   float topWidth,
                                   float perfectTolerance) {
        float dropRight = dropLeft + dropWidth;
        float topRight = topLeft + topWidth;

        float overlapLeft = Math.max(dropLeft, topLeft);
        float overlapRight = Math.min(dropRight, topRight);
        float overlapWidth = overlapRight - overlapLeft;

        // No overlap at all -> the block missed the tower entirely.
        if (overlapWidth <= 0f) {
            return DropResult.miss();
        }

        // Both edges land within tolerance of the footprint's edges -> perfect. Snap flush
        // to the block beneath so no sliver is lost to rounding.
        boolean leftAligned = Math.abs(dropLeft - topLeft) <= perfectTolerance;
        boolean rightAligned = Math.abs(dropRight - topRight) <= perfectTolerance;
        if (leftAligned && rightAligned) {
            return DropResult.perfect(topLeft, topWidth);
        }

        // Partial overlap: the tower narrows to the overlap, and the part of the dropped
        // block sticking out past the footprint becomes the shorn slice. With equal widths
        // the overhang is on exactly one side, but we handle either to stay robust.
        float sliceLeft;
        float sliceWidth;
        if (dropLeft < topLeft) {
            // Overhang on the left.
            sliceLeft = dropLeft;
            sliceWidth = overlapLeft - dropLeft;
        } else {
            // Overhang on the right.
            sliceLeft = overlapRight;
            sliceWidth = dropRight - overlapRight;
        }

        return DropResult.partial(overlapLeft, overlapWidth, sliceLeft, sliceWidth);
    }
}
