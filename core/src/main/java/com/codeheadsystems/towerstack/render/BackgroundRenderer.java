package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Fills the screen with a vertical gradient behind the tower (build brief §6/§7). It lives in
 * its own fixed viewport — never scrolling with the {@code CameraRig} — while its colors shift
 * with height, so climbing changes the whole field. Self-contained: owns its ShapeRenderer.
 */
public class BackgroundRenderer implements Disposable {

    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    public BackgroundRenderer() {
        this.shapes = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void draw(Color bottom, Color top) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // rect(x, y, w, h, bottomLeft, bottomRight, topRight, topLeft)
        shapes.rect(0f, 0f, Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, bottom, bottom, top, top);
        shapes.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
