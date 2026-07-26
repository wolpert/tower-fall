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

    // The edge-alignment tolerance that counts as a perfect lives in Difficulty, since it
    // varies per level.

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
    // A two-layer city skyline plus a star field. On the title screen the city drifts
    // horizontally (left); in a run the horizontal drift stops and the city recedes downward
    // as the camera rises, while stars fade in with height (climbing into "space").

    /** How far apart the city tiles horizontally (wider than the screen so it wraps smoothly). */
    public static final float CITY_PATTERN_WIDTH = WORLD_WIDTH * 1.6f;

    /** World-y the buildings stand on at rest (near the bottom of the screen). */
    public static final float CITY_BASELINE = WORLD_HEIGHT * 0.05f;

    /** Base horizontal drift speed on the title (world units/sec), scaled per layer. */
    public static final float CITY_DRIFT_SPEED = 14f;

    // Per-layer parallax: horizontal drift factor, vertical (height) factor, alpha, brightness.
    public static final float CITY_FAR_HFACTOR = 0.45f;
    public static final float CITY_NEAR_HFACTOR = 1.0f;
    public static final float CITY_FAR_VFACTOR = 0.10f;
    public static final float CITY_NEAR_VFACTOR = 0.20f;
    public static final float CITY_FAR_ALPHA = 0.18f;
    public static final float CITY_NEAR_ALPHA = 0.30f;
    public static final float CITY_FAR_BRIGHTNESS = 0.65f;
    public static final float CITY_NEAR_BRIGHTNESS = 1.0f;

    // Lit windows on the near buildings — a warm light, a touch brighter than the silhouette.
    public static final float WINDOW_SIZE = 4f;
    public static final float WINDOW_ALPHA = 0.55f;

    // Star field: scattered points that fade in as the tower climbs and twinkle gently.
    public static final int STAR_COUNT = 44;
    public static final float STAR_PATTERN_HEIGHT = WORLD_HEIGHT;
    public static final float STAR_FACTOR = 0.08f;      // very distant: barely scrolls
    public static final float STAR_FADE_START = 300f;   // camera height before stars appear
    public static final float STAR_FADE_RANGE = 1500f;  // height over which they reach full
    public static final float STAR_MAX_ALPHA = 0.9f;
    public static final float STAR_TWINKLE_SPEED = 2.5f;
    public static final float STAR_SIZE = 3f;

    // Moon: always in the sky (over the city, and among the stars up high), drifting slowly.
    public static final float MOON_RADIUS = 32f;
    public static final float MOON_START_X = WORLD_WIDTH * 0.70f;
    public static final float MOON_Y = WORLD_HEIGHT * 0.84f;
    public static final float MOON_DRIFT_SPEED = 2.5f; // horizontal world units/sec
    public static final float MOON_VFACTOR = 0.05f;    // very slight recede with height
    public static final float MOON_GLOW_ALPHA = 0.10f;

    // Shooting stars: occasional streaks, more likely once you've climbed into star territory.
    public static final float SHOOTING_STAR_MIN_INTERVAL = 3.5f;
    public static final float SHOOTING_STAR_MAX_INTERVAL = 9f;
    public static final float SHOOTING_STAR_SPEED = 850f;
    public static final float SHOOTING_STAR_LENGTH = 90f;
    public static final float SHOOTING_STAR_LIFE = 0.65f;
    public static final float SHOOTING_STAR_WIDTH = 3f;
    public static final float SHOOTING_STAR_MIN_VISIBILITY = 0.3f;

    // --- Juice: hit-stop (Freshly Squeezed) -------------------------------
    // A brief near-freeze on impact so the hit registers before the world moves on. The world
    // (blocks, effects, camera) runs at HITSTOP_TIME_SCALE while it lasts; input is unaffected.

    public static final float HITSTOP_TIME_SCALE = 0.10f;
    public static final float HITSTOP_PERFECT = 0.055f;
    public static final float HITSTOP_PER_COMBO = 0.012f;
    public static final float HITSTOP_PERFECT_MAX = 0.13f;
    public static final float HITSTOP_MISS = 0.20f;

    // --- Juice: screen flash (Freshly Squeezed) ---------------------------
    // A full-screen wash: the block's own color on a perfect, red on a miss.

    public static final float FLASH_PERFECT_ALPHA = 0.16f;
    public static final float FLASH_PERFECT_PER_COMBO = 0.045f;
    public static final float FLASH_PERFECT_MAX = 0.50f;
    public static final float FLASH_PERFECT_DURATION = 0.20f;
    public static final float FLASH_MISS_ALPHA = 0.34f;
    public static final float FLASH_MISS_DURATION = 0.32f;

    // --- Juice: camera zoom punch (Freshly Squeezed) ----------------------
    // An additive offset on the camera zoom that decays back to 1: a shove in on a perfect,
    // a lurch out on a miss.

    public static final float ZOOM_PUNCH_PERFECT = -0.045f;
    public static final float ZOOM_PUNCH_MISS = 0.075f;
    public static final float ZOOM_RECOVER_RATE = 5.5f;

    // --- Juice: tower sway (Freshly Squeezed) -----------------------------
    // A damped horizontal lean kicked by every landing. Largest at the top and rigid a few
    // blocks down (SWAY_SPAN_BLOCKS), and the planted base never moves (SWAY_ROOT_BLOCKS).

    public static final float SWAY_LAND_KICK = 5f;   // world units of lean at the top
    public static final float SWAY_MISS_KICK = 11f;
    public static final float SWAY_FREQUENCY = 9f;   // rad/sec
    public static final float SWAY_DECAY = 2.6f;
    public static final float SWAY_SPAN_BLOCKS = 9f;
    public static final float SWAY_ROOT_BLOCKS = 2f;

    // --- Juice: motion trail (Freshly Squeezed) ---------------------------
    // Fading ghosts of the sliding block, sampled on a fixed interval.

    public static final int TRAIL_SAMPLES = 7;
    public static final float TRAIL_INTERVAL = 0.022f;
    public static final float TRAIL_ALPHA = 0.30f;

    // --- Juice: landing dust (Freshly Squeezed) ---------------------------
    // A puff kicked sideways out of the seam on every landing, perfect or not.

    public static final int DUST_COUNT = 9;
    public static final float DUST_SPEED = 120f;
    public static final float DUST_RISE = 90f;
    public static final float DUST_GRAVITY = 420f;
    public static final float DUST_LIFE = 0.45f;
    public static final float DUST_SIZE = 7f;
    public static final float DUST_ALPHA = 0.6f;

    /**
     * How far the motes are lightened toward white. Without this they are the block's own color
     * sitting on top of the block, and effectively invisible.
     */
    public static final float DUST_TINT = 0.6f;

    // --- Juice: debris shatter (Freshly Squeezed) -------------------------
    // A shorn slice breaks into several tumbling pieces instead of one.

    public static final int SHATTER_MAX_PIECES = 4;
    public static final float SHATTER_MIN_PIECE = 9f; // don't split below this width

    // --- Juice: score popups (Freshly Squeezed) ---------------------------
    // Floating text that rises from the seam in screen space and fades.

    public static final float POPUP_LIFE = 0.85f;
    public static final float POPUP_RISE = 70f;
    public static final float POPUP_SCALE = 1.0f;
    public static final float POPUP_FADE_START = 0.45f; // fraction of life spent fully opaque

    // --- Juice: HUD pulse (Freshly Squeezed) ------------------------------
    // The score punches up a little whenever it changes.

    public static final float HUD_PULSE_AMOUNT = 0.32f;
    public static final float HUD_PULSE_DECAY = 4.5f;

    // --- Juice: Crushed and Ground ----------------------------------------
    // The over-the-top tier. Everything below is either a louder replacement for a Freshly
    // Squeezed number or an effect that exists only here.

    /** A perfect washes the screen hard and takes half a second to let go. */
    public static final float FLASH_PERFECT_ALPHA_WILD = 0.45f;
    public static final float FLASH_PERFECT_PER_COMBO_WILD = 0.09f;
    /** Stops short of a full whiteout — past about here the tower stops being readable. */
    public static final float FLASH_PERFECT_MAX_WILD = 0.70f;
    public static final float FLASH_PERFECT_DURATION_WILD = 0.50f;
    public static final float FLASH_MISS_ALPHA_WILD = 0.70f;
    public static final float FLASH_MISS_DURATION_WILD = 0.60f;

    /** Impacts hang noticeably longer. */
    public static final float HITSTOP_PERFECT_MAX_WILD = 0.26f;
    public static final float HITSTOP_MISS_WILD = 0.45f;

    // Camera roll: the view tips on an impact and rocks back upright.
    public static final float ROLL_PUNCH_LAND = 1.3f; // degrees
    public static final float ROLL_PUNCH_MISS = 5f;
    public static final float ROLL_RECOVER_RATE = 4.5f;

    // Tower rattle: a shock travelling down the stack, every block shaking (the base excepted).
    // The phase step per block is what makes it a travelling wave rather than a rigid shift.
    public static final float RATTLE_LAND = 3.5f;
    public static final float RATTLE_MISS = 26f;
    public static final float RATTLE_FREQUENCY = 34f;
    public static final float RATTLE_DECAY = 2.2f;
    public static final float RATTLE_PHASE_PER_BLOCK = 0.55f;

    // Fireworks over the skyline: shells launched from the city that arc up and burst.
    // Drawn in the background's fixed screen space, so they hang in the sky rather than
    // scrolling with the tower.
    public static final float FIREWORK_LAUNCH_Y = CITY_BASELINE + 30f;
    public static final float FIREWORK_SHELL_SPEED_MIN = 520f;
    public static final float FIREWORK_SHELL_SPEED_MAX = 730f;
    public static final float FIREWORK_SHELL_DRIFT = 70f;
    public static final float FIREWORK_GRAVITY = 340f;
    public static final float FIREWORK_FUSE_MIN = 0.65f;
    public static final float FIREWORK_FUSE_MAX = 1.05f;
    public static final float FIREWORK_TAIL = 26f;
    public static final int FIREWORK_SPARKS = 48;
    public static final float FIREWORK_SPARK_SPEED = 220f;
    public static final float FIREWORK_SPARK_LIFE = 1.3f;
    public static final float FIREWORK_SPARK_SIZE = 4f;
    public static final float FIREWORK_SPARK_DRAG = 1.2f;
    public static final float FIREWORK_SPARK_GRAVITY = 120f;
    public static final int FIREWORK_MILESTONE_SHELLS = 5;
    public static final int FIREWORK_GAME_OVER_SHELLS = 7;
    public static final float FIREWORK_TITLE_MIN_INTERVAL = 1.2f;
    public static final float FIREWORK_TITLE_MAX_INTERVAL = 3f;

    // Confetti: drawn in front of the tower, at a fixed place on screen, drifting down.
    public static final int CONFETTI_COUNT = 80;
    public static final float CONFETTI_FALL_SPEED = 200f;
    public static final float CONFETTI_SWAY = 70f;
    public static final float CONFETTI_SWAY_RATE = 3.4f;
    public static final float CONFETTI_SPIN = 280f;
    public static final float CONFETTI_SIZE = 9f;
    public static final float CONFETTI_LIFE = 3.4f;
    public static final float CONFETTI_SPAWN_BAND = 300f; // stagger above the top of the view

    // Shockwave rings: concentric rings thrown out of a perfect seam, on top of the
    // horizontal shockwave the lower tiers already draw.
    public static final int RING_COUNT = 3;
    public static final float RING_SPEED = 540f;
    public static final float RING_LIFE = 0.5f;
    public static final float RING_THICKNESS = 5f;
    public static final float RING_STAGGER = 0.075f;
    public static final int RING_SEGMENTS = 40;

    // A longer, hue-cycling motion trail.
    public static final int TRAIL_SAMPLES_WILD = 16;
    public static final float TRAIL_RAINBOW_SPEED = 220f; // hue degrees per second
    public static final float TRAIL_RAINBOW_STEP = 26f;   // hue degrees between ghosts

    /** Every this many perfects in a row sets off the full flare. */
    public static final int COMBO_MILESTONE = 5;

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

    /** Extra shimmer layered over the perfect tone once the streak reaches this length. */
    public static final int SPARKLE_MIN_COMBO = 3;
    public static final float VOLUME_SPARKLE = 0.35f;
}
