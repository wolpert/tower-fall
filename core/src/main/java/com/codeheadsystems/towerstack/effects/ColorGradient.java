package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Height-driven color (build brief §6, "color gradient drift"). Block hue advances smoothly
 * with height so the tower reads as a vertical gradient, and the background is a dark tint of
 * the current hue so climbing visibly changes the whole palette.
 *
 * <p>Stateless and pure — just maps a height index to colors.
 */
public class ColorGradient {

    /** Fill color for the block at the given height index (0 = base). */
    public Color blockColor(int index) {
        return hsv(hueAt(index), Tunables.COLOR_SATURATION, Tunables.COLOR_VALUE);
    }

    /** Darker tint at the bottom of the background gradient for the current height. */
    public Color backgroundBottom(int height) {
        return hsv(hueAt(height), 0.55f, 0.09f);
    }

    /** Slightly lighter tint at the top of the background gradient for the current height. */
    public Color backgroundTop(int height) {
        return hsv(hueAt(height), 0.40f, 0.20f);
    }

    /**
     * Silhouette color for the parallax skyline — deliberately brighter than the background so
     * the distant towers read against the dark gradient (drawn at low alpha for subtlety).
     */
    public Color parallax(int height) {
        return hsv(hueAt(height), 0.45f, 0.42f);
    }

    private float hueAt(int index) {
        return (Tunables.COLOR_BASE_HUE + index * Tunables.COLOR_HUE_STEP) % 360f;
    }

    private Color hsv(float hue, float saturation, float value) {
        // Start opaque; fromHsv sets r/g/b and leaves alpha untouched.
        Color color = new Color(0f, 0f, 0f, 1f);
        color.fromHsv(hue, saturation, value);
        return color;
    }
}
