package com.codeheadsystems.towerstack.config;

/**
 * Player-selectable difficulty (TODO #2). Each level scales the moving-block speed, how
 * forgiving a "perfect" is, and an optional minimum-width floor that keeps a run recoverable.
 *
 * <ul>
 *   <li><b>Easy</b> — slower, a wide perfect window, and a width floor so the tower never
 *       narrows to an impossible sliver.</li>
 *   <li><b>Normal</b> — the original tuning: the reference speed, a moderate perfect window,
 *       no floor.</li>
 *   <li><b>Hard</b> — faster, a tight perfect window, no floor.</li>
 * </ul>
 *
 * <p>The scalars multiply the base constants in {@link Tunables}, so global re-tuning still
 * happens in one place. Pure config — no libGDX.
 */
public enum Difficulty {

    EASY("Easy", 0.80f, 40f, 9f),
    NORMAL("Normal", 1.00f, 0f, 6f),
    HARD("Hard", 1.30f, 0f, 4f);

    private final String label;
    private final float speedScale;
    private final float minWidth;         // 0 = no floor
    private final float perfectTolerance; // world units of edge alignment that count as perfect

    Difficulty(String label, float speedScale, float minWidth, float perfectTolerance) {
        this.label = label;
        this.speedScale = speedScale;
        this.minWidth = minWidth;
        this.perfectTolerance = perfectTolerance;
    }

    public String getLabel() {
        return label;
    }

    public float getSpeedScale() {
        return speedScale;
    }

    public float getMinWidth() {
        return minWidth;
    }

    public float getPerfectTolerance() {
        return perfectTolerance;
    }

    /** The next level, wrapping Easy → Normal → Hard → Easy (for a cycling toggle). */
    public Difficulty next() {
        Difficulty[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
