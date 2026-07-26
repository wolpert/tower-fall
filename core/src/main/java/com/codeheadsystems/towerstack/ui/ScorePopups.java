package com.codeheadsystems.towerstack.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Floating text that rises out of the seam and fades — "+4", "PERFECT x3", "MISS" — so the
 * reward lands where the player is already looking instead of only in the HUD counter at the
 * top of the screen. Freshly Squeezed only.
 *
 * <p>Popups live in the fixed HUD space (the caller converts a world point once, at spawn), so
 * they drift up the screen rather than riding the rising camera. Drawn between
 * {@link TextRenderer#begin()} and {@link TextRenderer#end()}.
 */
public class ScorePopups {

    private static final class Popup {
        final String text;
        final float x;
        float y;
        float life;
        final float maxLife;
        final float scale;
        final Color color;

        Popup(String text, float x, float y, float life, float scale, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.life = life;
            this.maxLife = life;
            this.scale = scale;
            this.color = color;
        }
    }

    private final Array<Popup> popups = new Array<>();
    private final Color scratch = new Color();

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            clear();
        }
    }

    /** Spawn a popup centered on {@code x}, in HUD (screen) coordinates. */
    public void add(String text, float x, float y, Color color, float scale) {
        if (!level.hasExtras()) {
            return;
        }
        popups.add(new Popup(text, x, y, Tunables.POPUP_LIFE, scale, new Color(color)));
    }

    public void update(float delta) {
        for (int i = popups.size - 1; i >= 0; i--) {
            Popup popup = popups.get(i);
            popup.life -= delta;
            if (popup.life <= 0f) {
                popups.removeIndex(i);
                continue;
            }
            // Rise quickly at first and ease out, so the pop reads as a pop.
            float progress = 1f - popup.life / popup.maxLife;
            popup.y += Tunables.POPUP_RISE * (1f - progress) * delta;
        }
    }

    public void draw(TextRenderer text) {
        for (Popup popup : popups) {
            float remaining = popup.life / popup.maxLife;
            float alpha = remaining > Tunables.POPUP_FADE_START
                    ? 1f
                    : remaining / Tunables.POPUP_FADE_START;
            scratch.set(popup.color.r, popup.color.g, popup.color.b, alpha);
            text.drawCenteredAt(popup.text, popup.x, popup.y, scratch, popup.scale);
        }
    }

    public void clear() {
        popups.clear();
    }
}
