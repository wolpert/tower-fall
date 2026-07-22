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
     * this band stays fixed as the tower climbs.
     */
    public static final float TOP_BAND_Y = WORLD_HEIGHT * 0.60f;

    /**
     * How briskly the camera eases toward its target height, as an exponential-smoothing
     * rate (larger = snappier). Applied frame-rate-independently as {@code 1 - e^(-rate*dt)}.
     */
    public static final float CAMERA_RISE_RATE = 9f;

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

    /**
     * Bonus points granted per combo level on a perfect placement, on top of the flat +1
     * for the block. A perfect at combo level {@code n} scores {@code 1 + n * this}, so a
     * streak ramps up (build brief §5, combo scoring = bonus points + width regrowth).
     */
    public static final int COMBO_BONUS_PER_STEP = 1;

    // --- Juice: eased slide -----------------------------------------------

    /**
     * Minimum speed multiplier at the turnarounds (build brief §6, "eased block slide").
     * The block slows toward the edges and runs full speed through the middle; 1.0 disables
     * the easing entirely.
     */
    public static final float SLIDE_EDGE_EASE = 0.35f;

    // --- Juice: landing squash & stretch ----------------------------------
    // A damped spring: the block compresses on impact then springs back past 1.0 and settles.

    public static final float SQUASH_AMPLITUDE = 0.26f;
    public static final float SQUASH_DURATION = 0.45f;
    public static final float SQUASH_DECAY = 9f;      // how fast the spring settles
    public static final float SQUASH_FREQUENCY = 22f; // spring angular frequency (rad/s)

    // --- Juice: slice debris ----------------------------------------------

    public static final float DEBRIS_GRAVITY = 1900f;
    public static final float DEBRIS_OUTWARD_SPEED = 150f;
    public static final float DEBRIS_POP_SPEED = 130f;
    public static final float DEBRIS_SPIN = 300f; // degrees/sec magnitude
    public static final float DEBRIS_CULL_MARGIN = 60f;

    // --- Juice: camera punch / micro-shake --------------------------------
    // Trauma accumulates on impacts and decays; screen offset scales with trauma squared.

    public static final float CAMERA_MAX_SHAKE = 20f;    // world units at full trauma
    public static final float CAMERA_TRAUMA_DECAY = 1.9f; // trauma lost per second
    public static final float LAND_PUNCH = 0.24f;         // trauma added on a landing
    public static final float MISS_PUNCH = 0.65f;         // trauma added on a miss

    // --- Juice: perfect-placement burst -----------------------------------
    // Particle count and shockwave brightness ramp with the combo (build brief §6).

    public static final int BURST_BASE_PARTICLES = 10;
    public static final int BURST_PER_COMBO = 3;
    public static final int BURST_MAX_PARTICLES = 46;
    public static final float BURST_PARTICLE_SPEED = 230f;
    public static final float BURST_PARTICLE_LIFE = 0.55f;
    public static final float BURST_PARTICLE_SIZE = 6f;
    public static final float BURST_GRAVITY = 320f;
    public static final float SHOCKWAVE_SPEED = 900f;
    public static final float SHOCKWAVE_LIFE = 0.30f;
    public static final float SHOCKWAVE_THICKNESS = 4f;

    // --- Juice: color gradient --------------------------------------------
    // Block color drifts with height; the background is a dark tint of the same hue.

    public static final float COLOR_BASE_HUE = 205f;
    public static final float COLOR_HUE_STEP = 7f; // degrees of hue per block placed
    public static final float COLOR_SATURATION = 0.50f;
    public static final float COLOR_VALUE = 0.86f;

    // --- Isometric view (cosmetic skin) -----------------------------------
    // Renders each block as a 3D cuboid under a 2:1 dimetric projection. Gameplay is
    // unchanged; only the drawing differs. Projection is centered on the world so a
    // centered tower stays vertical, with the diamond top spreading symmetrically.

    public static final float ISO_SCALE_X = 0.62f; // horizontal unit
    public static final float ISO_SCALE_Y = 0.31f; // depth-induced vertical skew (~half of X)
    public static final float ISO_DEPTH = 150f;     // fixed block depth into the scene
    public static final float ISO_SHADE_TOP = 1.00f;
    public static final float ISO_SHADE_FRONT = 0.78f;
    public static final float ISO_SHADE_SIDE = 0.62f;

    // --- Parallax background ----------------------------------------------
    // Two layers of faint distant silhouettes that scroll down as the camera rises. Each
    // layer moves at a fraction of the camera's speed (near faster than far) and repeats
    // every PATTERN_HEIGHT world units.

    public static final float PARALLAX_PATTERN_HEIGHT = WORLD_HEIGHT * 1.4f;
    public static final float PARALLAX_FAR_FACTOR = 0.12f;
    public static final float PARALLAX_NEAR_FACTOR = 0.28f;
    public static final float PARALLAX_FAR_ALPHA = 0.06f;
    public static final float PARALLAX_NEAR_ALPHA = 0.10f;

    // --- Screen transitions -----------------------------------------------
    // Fade-in-from-black durations (seconds). Kept short so retries stay snappy.

    public static final float TITLE_FADE_IN = 0.40f;
    public static final float PLAY_FADE_IN = 0.28f;

    // --- Audio ------------------------------------------------------------
    // Playback volumes (0..1). The perfect tone's pitch rises with the combo.

    public static final float VOLUME_LAND = 0.5f;
    public static final float VOLUME_SLICE = 0.4f;
    public static final float VOLUME_PERFECT = 0.55f;
    public static final float VOLUME_GAME_OVER = 0.6f;

    /** Pitch multiplier added per combo level to the perfect tone, capped for sanity. */
    public static final float COMBO_PITCH_STEP = 0.06f;
    public static final float COMBO_PITCH_MAX = 2.0f;
}
