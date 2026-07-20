package com.codeheadsystems.towerstack.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Local, offline persistence of the best score via libGDX {@link Preferences}. No accounts,
 * no backend (build brief §2, §5) — just a single integer that survives restarts.
 */
public class ScoreStore {

    private static final String PREFS_NAME = "tower-stack";
    private static final String KEY_BEST = "bestScore";

    private final Preferences prefs;

    public ScoreStore() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public int best() {
        return prefs.getInteger(KEY_BEST, 0);
    }

    /**
     * Record {@code score} if it beats the stored best.
     *
     * @return {@code true} if this was a new best
     */
    public boolean submit(int score) {
        if (score > best()) {
            prefs.putInteger(KEY_BEST, score);
            prefs.flush();
            return true;
        }
        return false;
    }
}
