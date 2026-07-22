package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.config.Tunables;
import java.util.Random;

/**
 * Fills the screen with a vertical gradient behind the tower, plus two layers of faint distant
 * tower silhouettes that drift downward as the camera rises (build brief §6/§7 and the §11
 * "background parallax that reacts to height" stretch). Each layer scrolls at a fraction of the
 * camera's speed and repeats every {@link Tunables#PARALLAX_PATTERN_HEIGHT}, giving cheap depth.
 *
 * <p>Lives in its own fixed viewport — it never scrolls with the {@code CameraRig}; the scroll
 * is applied to the silhouettes as a function of camera height. Its colors track the palette,
 * so climbing changes the whole field. Self-contained: owns its ShapeRenderer.
 */
public class BackgroundRenderer implements Disposable {

    /** One distant silhouette bar within a repeating vertical strip. */
    private static final class Bar {
        final float x;
        final float bottom;
        final float width;
        final float height;

        Bar(float x, float bottom, float width, float height) {
            this.x = x;
            this.bottom = bottom;
            this.width = width;
            this.height = height;
        }
    }

    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Bar[] farLayer;
    private final Bar[] nearLayer;
    private final Color tint = new Color();

    public BackgroundRenderer() {
        this.shapes = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);

        // Deterministic layout: same skyline every run (no per-frame randomness).
        Random random = new Random(20260722L);
        this.farLayer = buildLayer(random, 7, 24f, 60f, 90f, 220f);
        this.nearLayer = buildLayer(random, 6, 40f, 90f, 150f, 340f);
    }

    private Bar[] buildLayer(Random random, int count,
                             float minWidth, float maxWidth, float minHeight, float maxHeight) {
        Bar[] bars = new Bar[count];
        for (int i = 0; i < count; i++) {
            float width = range(random, minWidth, maxWidth);
            float x = range(random, -width, Tunables.WORLD_WIDTH);
            float bottom = range(random, 0f, Tunables.PARALLAX_PATTERN_HEIGHT);
            float height = range(random, minHeight, maxHeight);
            bars[i] = new Bar(x, bottom, width, height);
        }
        return bars;
    }

    private float range(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void draw(Color bottom, Color top, Color silhouette, float cameraY) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Gradient: rect(x, y, w, h, bottomLeft, bottomRight, topRight, topLeft).
        shapes.rect(0f, 0f, Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, bottom, bottom, top, top);

        drawLayer(farLayer, silhouette, Tunables.PARALLAX_FAR_FACTOR, Tunables.PARALLAX_FAR_ALPHA, cameraY);
        drawLayer(nearLayer, silhouette, Tunables.PARALLAX_NEAR_FACTOR, Tunables.PARALLAX_NEAR_ALPHA, cameraY);

        shapes.end();
    }

    private void drawLayer(Bar[] bars, Color base, float factor, float alpha, float cameraY) {
        tint.set(base.r, base.g, base.b, alpha);
        shapes.setColor(tint);

        // Scroll offset within the repeating strip, normalized to [0, PATTERN_HEIGHT).
        float pattern = Tunables.PARALLAX_PATTERN_HEIGHT;
        float offset = -(cameraY * factor) % pattern;
        if (offset < 0f) {
            offset += pattern;
        }

        for (Bar bar : bars) {
            // Draw the bar and its wrapped copies so the strip tiles seamlessly.
            drawBarIfVisible(bar, bar.bottom + offset - pattern);
            drawBarIfVisible(bar, bar.bottom + offset);
        }
    }

    private void drawBarIfVisible(Bar bar, float y) {
        if (y > Tunables.WORLD_HEIGHT || y + bar.height < 0f) {
            return;
        }
        shapes.rect(bar.x, y, bar.width, bar.height);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
