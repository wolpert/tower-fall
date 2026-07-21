package com.codeheadsystems.towerstack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.codeheadsystems.towerstack.TowerStackGame;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.effects.ColorGradient;
import com.codeheadsystems.towerstack.render.BackgroundRenderer;
import com.codeheadsystems.towerstack.ui.ScreenFade;
import com.codeheadsystems.towerstack.ui.TextRenderer;
import com.codeheadsystems.towerstack.util.ScoreStore;

/**
 * The idle/title screen (build brief §8): game name, a "tap to start" prompt, and the best
 * score, over the same height-driven gradient the game uses. One tap / click / spacebar drops
 * straight into a run.
 *
 * <p>Transient — a fresh instance is created each time we return here, so it disposes its own
 * resources in {@link #hide()}.
 */
public class TitleScreen extends ScreenAdapter {

    private final TowerStackGame game;
    private final BackgroundRenderer background;
    private final ColorGradient colors;
    private final TextRenderer text;
    private final ScreenFade fade;
    private final ScoreStore scoreStore;

    public TitleScreen(TowerStackGame game) {
        this.game = game;
        this.background = new BackgroundRenderer();
        this.colors = new ColorGradient();
        this.text = new TextRenderer();
        this.fade = new ScreenFade();
        this.scoreStore = new ScoreStore();
    }

    @Override
    public void show() {
        fade.start(Tunables.TITLE_FADE_IN);
    }

    @Override
    public void render(float delta) {
        if (startRequested()) {
            game.setScreen(new PlayScreen(game));
            return; // our resources are torn down in hide(); don't draw after handing off
        }

        fade.update(delta);
        draw();
    }

    private boolean startRequested() {
        return Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        background.draw(colors.backgroundBottom(0), colors.backgroundTop(0));

        text.begin();
        text.drawCentered("TOWER STACK", Tunables.WORLD_HEIGHT * 0.62f, Color.WHITE, 2.4f);
        text.drawCentered("tap to start", Tunables.WORLD_HEIGHT * 0.46f, Color.LIGHT_GRAY, 1.2f);
        text.drawCentered("Best  " + scoreStore.best(), Tunables.WORLD_HEIGHT * 0.38f, Color.GOLD, 1.2f);
        text.end();

        fade.render();
    }

    @Override
    public void resize(int width, int height) {
        background.resize(width, height);
        text.resize(width, height);
        fade.resize(width, height);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        background.dispose();
        text.dispose();
        fade.dispose();
    }
}
