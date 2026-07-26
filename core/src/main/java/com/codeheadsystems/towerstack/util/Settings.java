package com.codeheadsystems.towerstack.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.codeheadsystems.towerstack.config.Difficulty;
import com.codeheadsystems.towerstack.config.JuiceLevel;

/**
 * Persisted player options — sound on/off, flat/isometric view, difficulty, and how much juice
 * to serve — stored locally via libGDX {@link Preferences}, alongside the best score (see
 * {@link ScoreStore}).
 */
public class Settings {

    private static final String PREFS_NAME = "tower-stack";
    private static final String KEY_SOUND = "soundEnabled";
    private static final String KEY_ISOMETRIC = "isometric";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_JUICE = "juice";

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

    public JuiceLevel juice() {
        try {
            return JuiceLevel.valueOf(prefs.getString(KEY_JUICE, JuiceLevel.STORE_BOUGHT.name()));
        } catch (IllegalArgumentException e) {
            return JuiceLevel.STORE_BOUGHT; // stored value no longer valid — fall back
        }
    }

    public void setJuice(JuiceLevel juice) {
        prefs.putString(KEY_JUICE, juice.name());
        prefs.flush();
    }

    /** Advance to the next juice level (None → Store Bought → Freshly Squeezed → None). */
    public void cycleJuice() {
        setJuice(juice().next());
    }
}
