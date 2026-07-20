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
    private float currentWidth;

    public GameState() {
        reset();
    }

    /** Begin a fresh run. */
    public void reset() {
        phase = Phase.SLIDING;
        blocksPlaced = 0;
        currentWidth = Tunables.START_WIDTH;
    }

    /** Record a successful placement; the next block spawns at {@code newWidth}. */
    public void recordPlacement(float newWidth) {
        blocksPlaced++;
        currentWidth = newWidth;
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
