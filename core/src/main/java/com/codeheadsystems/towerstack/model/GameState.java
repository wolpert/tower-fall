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
    private float currentWidth;

    public GameState() {
        reset();
    }

    /** Begin a fresh run. */
    public void reset() {
        phase = Phase.SLIDING;
        blocksPlaced = 0;
        score = 0;
        currentWidth = Tunables.START_WIDTH;
    }

    /**
     * Record a successful placement; the next block spawns at {@code newWidth}.
     *
     * <p>Score is simply the number of blocks placed for now. The combo bonus that scales
     * this on perfect streaks arrives in increment 3.
     */
    public void recordPlacement(float newWidth) {
        blocksPlaced++;
        score++;
        currentWidth = newWidth;
    }

    public int getScore() {
        return score;
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
