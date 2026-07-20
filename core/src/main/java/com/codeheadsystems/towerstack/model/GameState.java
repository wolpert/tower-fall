package com.codeheadsystems.towerstack.model;

import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Non-visual game state and the little state machine that drives a run.
 *
 * <p>Increment 1 tracks only what the grey-box loop needs: the current phase, how many
 * blocks have been placed (which drives both the score-to-be and the speed curve), and the
 * width the next moving block should spawn with. Score and combo arrive in later increments.
 */
public class GameState {

    public enum Phase {
        SLIDING,
        GAME_OVER
    }

    private Phase phase;
    private int blocksPlaced;
    private int score;
    private int combo;
    private float currentWidth;

    public GameState() {
        reset();
    }

    /** Begin a fresh run. */
    public void reset() {
        phase = Phase.SLIDING;
        blocksPlaced = 0;
        score = 0;
        combo = 0;
        currentWidth = Tunables.START_WIDTH;
    }

    /**
     * Record a successful placement; the next block spawns at {@code newWidth}.
     *
     * <p>A perfect placement extends the combo and scores a bonus that scales with the
     * streak length; any imperfect placement banks a flat point and breaks the combo.
     *
     * @param perfect    whether the drop was a perfect (edge-aligned) placement
     * @param newWidth   width the tower top now has (and the next block spawns with)
     */
    public void recordPlacement(boolean perfect, float newWidth) {
        blocksPlaced++;
        if (perfect) {
            combo++;
            score += 1 + combo * Tunables.COMBO_BONUS_PER_STEP;
        } else {
            combo = 0;
            score += 1;
        }
        currentWidth = newWidth;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public void gameOver() {
        phase = Phase.GAME_OVER;
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isPlaying() {
        return phase == Phase.SLIDING;
    }

    public boolean isGameOver() {
        return phase == Phase.GAME_OVER;
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public float getCurrentWidth() {
        return currentWidth;
    }

    /** Current horizontal speed of the moving block, ramping with height to a ceiling. */
    public float currentSpeed() {
        float speed = Tunables.BASE_SPEED + Tunables.HEIGHT_FACTOR * blocksPlaced;
        return Math.min(speed, Tunables.SPEED_CEILING);
    }
}
