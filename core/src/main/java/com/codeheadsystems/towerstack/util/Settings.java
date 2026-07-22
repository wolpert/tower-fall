package com.codeheadsystems.towerstack.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Persisted player options — sound on/off and flat/isometric view — stored locally via
 * libGDX {@link Preferences}, alongside the best score (see {@link ScoreStore}).
 */
public class Settings {

    private static final String PREFS_NAME = "tower-stack";
    private static final String KEY_SOUND = "soundEnabled";
    private static final String KEY_ISOMETRIC = "isometric";

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
}
