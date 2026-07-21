package com.codeheadsystems.towerstack.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * A quick fade-in-from-black overlay used on every screen entrance, so state changes are
 * animated rather than hard cuts (build brief §8). Fade-in only — the incoming screen appears
 * from black — which keeps retries snappy (no fade-out delay before the action resumes).
 *
 * <p>Draw it last, on top of everything, after {@link #start(float)} is called from a screen's
 * {@code show()}.
 */
public class ScreenFade implements Disposable {

    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private float duration;
    private float time;

    public ScreenFade() {
        this.shapes = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /** Begin a fade-in over {@code duration} seconds. */
    public void start(float duration) {
        this.duration = duration;
        this.time = 0f;
    }

    public void update(float delta) {
        if (time < duration) {
            time += delta;
        }
    }

    /** Overlay the current fade. No-op once the fade has finished. */
    public void render() {
        if (time >= duration) {
            return;
        }
        float alpha = 1f - time / duration;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, alpha);
        shapes.rect(0f, 0f, Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT);
        shapes.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
