package com.codeheadsystems.towerstack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.codeheadsystems.towerstack.TowerStackGame;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.effects.ColorGradient;
import com.codeheadsystems.towerstack.render.BackgroundRenderer;
import com.codeheadsystems.towerstack.ui.ScreenFade;
import com.codeheadsystems.towerstack.ui.TextRenderer;
import com.codeheadsystems.towerstack.util.ScoreStore;
import com.codeheadsystems.towerstack.util.Settings;

/**
 * The idle/title screen (build brief §8): game name, a "tap to start" prompt, the best score,
 * and a tappable sound on/off toggle. Tapping the toggle flips it; tapping anywhere else (or
 * pressing space) starts a run. On desktop, {@code M} also toggles sound.
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
    private final Settings settings;

    // Tappable regions for the toggles, in world coordinates.
    private final Rectangle soundToggle = new Rectangle(
            Tunables.WORLD_WIDTH * 0.15f, Tunables.WORLD_HEIGHT * 0.245f,
            Tunables.WORLD_WIDTH * 0.70f, Tunables.WORLD_HEIGHT * 0.060f);
    private final Rectangle viewToggle = new Rectangle(
            Tunables.WORLD_WIDTH * 0.15f, Tunables.WORLD_HEIGHT * 0.175f,
            Tunables.WORLD_WIDTH * 0.70f, Tunables.WORLD_HEIGHT * 0.060f);

    public TitleScreen(TowerStackGame game) {
        this.game = game;
        this.background = new BackgroundRenderer();
        this.colors = new ColorGradient();
        this.text = new TextRenderer();
        this.fade = new ScreenFade();
        this.scoreStore = new ScoreStore();
        this.settings = new Settings();
    }

    @Override
    public void show() {
        fade.start(Tunables.TITLE_FADE_IN);
    }

    @Override
    public void render(float delta) {
        if (handleInput()) {
            return; // a run started; our resources are torn down in hide()
        }
        fade.update(delta);
        draw();
    }

    /** @return true if a run was started (caller should stop touching this screen) */
    private boolean handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            settings.toggleSound();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            settings.toggleIsometric();
        }
        if (Gdx.input.justTouched()) {
            Vector2 world = text.unproject(Gdx.input.getX(), Gdx.input.getY());
            if (soundToggle.contains(world.x, world.y)) {
                settings.toggleSound();
                return false;
            }
            if (viewToggle.contains(world.x, world.y)) {
                settings.toggleIsometric();
                return false;
            }
            startGame();
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            startGame();
            return true;
        }
        return false;
    }

    private void startGame() {
        game.setScreen(new PlayScreen(game));
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        background.draw(colors.backgroundBottom(0), colors.backgroundTop(0), colors.parallax(0), 0f);

        text.begin();
        text.drawCentered("TOWER STACK", Tunables.WORLD_HEIGHT * 0.62f, Color.WHITE, 2.4f);
        text.drawCentered("tap to start", Tunables.WORLD_HEIGHT * 0.46f, Color.LIGHT_GRAY, 1.2f);
        text.drawCentered("Best  " + scoreStore.best(), Tunables.WORLD_HEIGHT * 0.38f, Color.GOLD, 1.2f);
        text.drawCentered("Sound:  " + (settings.isSoundEnabled() ? "On" : "Off"),
                Tunables.WORLD_HEIGHT * 0.27f, Color.LIGHT_GRAY, 1.1f);
        text.drawCentered("View:  " + (settings.isIsometric() ? "Iso" : "Flat"),
                Tunables.WORLD_HEIGHT * 0.20f, Color.LIGHT_GRAY, 1.1f);
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
