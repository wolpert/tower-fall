package com.codeheadsystems.towerstack.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * A full-screen colored wash that snaps on and falls away — pale, block-tinted on a perfect
 * (brighter with the combo) and red on a miss. Freshly Squeezed only.
 *
 * <p>The sibling of {@link ScreenFade}: same fixed viewport and overlay quad, but the color is
 * the caller's and the alpha runs down from a peak rather than up from black. Draw it under the
 * fade, on top of everything else.
 */
public class ScreenFlash implements Disposable {

    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Color color = new Color();

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    private float duration;
    private float time = -1f; // negative = nothing flashing
    private float peak;

    public ScreenFlash() {
        this.shapes = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
    }

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            clear();
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /** Wash the screen in {@code tint}, fading from {@code peakAlpha} to nothing. */
    public void flash(Color tint, float peakAlpha, float duration) {
        if (!level.hasExtras()) {
            return;
        }
        this.color.set(tint);
        this.peak = peakAlpha;
        this.duration = duration;
        this.time = 0f;
    }

    public void update(float delta) {
        if (time >= 0f) {
            time += delta;
            if (time >= duration) {
                time = -1f;
            }
        }
    }

    /** Overlay the current flash. No-op when nothing is flashing. */
    public void render() {
        if (time < 0f) {
            return;
        }
        float alpha = peak * (1f - time / duration);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color.r, color.g, color.b, alpha);
        shapes.rect(0f, 0f, Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT);
        shapes.end();
    }

    public void clear() {
        time = -1f;
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
