package com.codeheadsystems.towerstack.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.codeheadsystems.towerstack.config.Difficulty;

/**
 * Persisted player options — sound on/off, flat/isometric view, and difficulty — stored
 * locally via libGDX {@link Preferences}, alongside the best score (see {@link ScoreStore}).
 */
public class Settings {

    private static final String PREFS_NAME = "tower-stack";
    private static final String KEY_SOUND = "soundEnabled";
    private static final String KEY_ISOMETRIC = "isometric";
    private static final String KEY_DIFFICULTY = "difficulty";

    private final Preferences prefs;

    public Settings() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.putBoolean(KEY_SOUND, enabled);
        prefs.flush();
    }

    public void toggleSound() {
        setSoundEnabled(!isSoundEnabled());
    }

    public boolean isIsometric() {
        return prefs.getBoolean(KEY_ISOMETRIC, false);
    }

    public void setIsometric(boolean isometric) {
        prefs.putBoolean(KEY_ISOMETRIC, isometric);
        prefs.flush();
    }

    public void toggleIsometric() {
        setIsometric(!isIsometric());
    }

    public Difficulty difficulty() {
        try {
            return Difficulty.valueOf(prefs.getString(KEY_DIFFICULTY, Difficulty.NORMAL.name()));
        } catch (IllegalArgumentException e) {
            return Difficulty.NORMAL; // stored value no longer valid — fall back
        }
    }

    public void setDifficulty(Difficulty difficulty) {
        prefs.putString(KEY_DIFFICULTY, difficulty.name());
        prefs.flush();
    }

    /** Advance to the next difficulty (Easy → Normal → Hard → Easy), for a cycling toggle. */
    public void cycleDifficulty() {
        setDifficulty(difficulty().next());
    }
}
