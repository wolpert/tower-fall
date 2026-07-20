package com.codeheadsystems.towerstack.render;

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
 * Draws the heads-up display: the live score during play, and the score/best/retry prompt
 * on game over (build brief §5, §8).
 *
 * <p>The HUD lives in its own fixed viewport so it never moves with the {@code CameraRig} —
 * text stays pinned to the screen while the tower scrolls behind it. Uses libGDX's built-in
 * bitmap font (no asset files); a nicer font is a later polish item.
 */
public class HudRenderer implements Disposable {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    public HudRenderer() {
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.6f);
        this.font.setUseIntegerPositions(false);
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /** Live score, parked near the top of the screen. */
    public void renderPlaying(int score) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        drawCentered(Integer.toString(score), Tunables.WORLD_HEIGHT * 0.90f);
        batch.end();
    }

    /** Final score, best score, and the retry prompt. */
    public void renderGameOver(int score, int best, boolean newBest) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.setColor(Color.WHITE);
        drawCentered("GAME OVER", Tunables.WORLD_HEIGHT * 0.62f);
        drawCentered("Score  " + score, Tunables.WORLD_HEIGHT * 0.52f);

        if (newBest) {
            font.setColor(Color.GOLD);
            drawCentered("New Best!", Tunables.WORLD_HEIGHT * 0.44f);
        } else {
            font.setColor(Color.LIGHT_GRAY);
            drawCentered("Best  " + best, Tunables.WORLD_HEIGHT * 0.44f);
        }

        font.setColor(Color.LIGHT_GRAY);
        drawCentered("tap to retry", Tunables.WORLD_HEIGHT * 0.34f);

        batch.end();
    }

    private void drawCentered(String text, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, (Tunables.WORLD_WIDTH - layout.width) / 2f, y);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
