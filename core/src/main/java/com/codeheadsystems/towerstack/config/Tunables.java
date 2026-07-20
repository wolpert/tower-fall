package com.codeheadsystems.towerstack.config;

/**
 * Every gameplay-feel constant lives here, in one place, so the difficulty and juice
 * passes (build brief §9.4, §9.6) are a matter of editing numbers in a single file.
 *
 * <p>Values are in world units. The world is a fixed-size portrait box (see
 * {@link #WORLD_WIDTH} / {@link #WORLD_HEIGHT}) that the viewport letterboxes to fit
 * whatever window or device screen it is shown on.
 */
public final class Tunables {

    private Tunables() {
        // Constants holder; not instantiable.
    }

    // --- World (portrait) -------------------------------------------------

    public static final float WORLD_WIDTH = 480f;
    public static final float WORLD_HEIGHT = 854f;

    /**
     * Screen height at which the current top of the tower is kept. The camera rises so
     * this band stays fixed as the tower climbs (a snap for now; eased in increment 2).
     */
    public static final float TOP_BAND_Y = WORLD_HEIGHT * 0.60f;

    // --- Block geometry ---------------------------------------------------

    public static final float BLOCK_HEIGHT = 46f;
    public static final float START_WIDTH = 320f;

    // Note: there is deliberately NO minimum-width floor. Per the design decision, the
    // tower may narrow to a true point. A floor would be a single constant added here.

    // --- Moving block speed ----------------------------------------------
    // speed = BASE_SPEED + HEIGHT_FACTOR * blocksPlaced, clamped to SPEED_CEILING.

    public static final float BASE_SPEED = 190f;
    public static final float HEIGHT_FACTOR = 7f;
    public static final float SPEED_CEILING = 520f;

    // --- Perfect placement (rewarded starting in increment 3) -------------

    /** Edge alignment within this many world units counts as a perfect placement. */
    public static final float PERFECT_TOLERANCE = 6f;

    /** Width regained on a perfect placement, capped at {@link #START_WIDTH}. */
    public static final float PERFECT_REGROWTH = 14f;
}
