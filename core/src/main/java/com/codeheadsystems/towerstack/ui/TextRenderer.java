package com.codeheadsystems.towerstack.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Shared text drawing for the HUD and screens. Lives in its own fixed viewport so text stays
 * pinned to the screen (never scrolling with the world camera) and lines up at world-space
 * coordinates regardless of window size. Uses libGDX's built-in bitmap font — no asset files;
 * a nicer font is a later polish item.
 *
 * <p>Wrap draws in {@link #begin()} / {@link #end()}; {@code scale} is relative to the font's
 * natural size.
 */
public class TextRenderer implements Disposable {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    public TextRenderer() {
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setUseIntegerPositions(false);
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void begin() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
    }

    public void end() {
        batch.end();
    }

    /** Draw a horizontally centered line at world-y {@code y}. Call between begin/end. */
    public void drawCentered(String text, float y, Color color, float scale) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (Tunables.WORLD_WIDTH - layout.width) / 2f, y);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
