package com.codeheadsystems.towerstack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.TowerStackGame;
import com.codeheadsystems.towerstack.audio.GameAudio;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.effects.CameraRig;
import com.codeheadsystems.towerstack.effects.ColorGradient;
import com.codeheadsystems.towerstack.effects.DebrisField;
import com.codeheadsystems.towerstack.effects.PerfectBurst;
import com.codeheadsystems.towerstack.effects.SquashStretch;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.model.DropResult;
import com.codeheadsystems.towerstack.model.GameState;
import com.codeheadsystems.towerstack.model.SliceMath;
import com.codeheadsystems.towerstack.model.Tower;
import com.codeheadsystems.towerstack.render.BackgroundRenderer;
import com.codeheadsystems.towerstack.render.BlockRenderer;
import com.codeheadsystems.towerstack.render.FlatBlockRenderer;
import com.codeheadsystems.towerstack.render.IsoBlockRenderer;
import com.codeheadsystems.towerstack.ui.ScreenFade;
import com.codeheadsystems.towerstack.ui.TextRenderer;
import com.codeheadsystems.towerstack.util.ScoreStore;
import com.codeheadsystems.towerstack.util.Settings;

/**
 * The playable loop (build brief §3), now with the full juice pass (§6).
 *
 * <p>A block eases side-to-side atop the tower; one tap drops it; the overhang shears off as
 * tumbling debris and the next block inherits the surviving width; a perfect placement snaps
 * flush, regrows, and pops a combo-scaled burst; a total miss flings the block away, punches
 * the camera, and ends the run. The camera rises smoothly, blocks squash on landing, and the
 * palette drifts with height.
 *
 * <p>This screen stays a thin orchestrator: it owns the state machine and hands work to the
 * model (slice math, scoring) and to the isolated renderers and effect components.
 */
public class PlayScreen extends ScreenAdapter {

    /** Rest height of the moving block above the tower top, in world units. */
    private static final float DROP_GAP = 0f;

    private final TowerStackGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;

    // Renderers. Both block renderers exist; blockRenderer points at the active one so the
    // view can be swapped live (e.g. from the game-over screen).
    private final BackgroundRenderer background;
    private final BlockRenderer flatRenderer = new FlatBlockRenderer();
    private final BlockRenderer isoRenderer = new IsoBlockRenderer();
    private BlockRenderer blockRenderer;
    private final TextRenderer text;
    private final ScreenFade fade;

    // Tappable game-over toggles (world coordinates), mirroring the title screen.
    private final Rectangle soundToggle = new Rectangle(
            Tunables.WORLD_WIDTH * 0.15f, Tunables.WORLD_HEIGHT * 0.325f,
            Tunables.WORLD_WIDTH * 0.70f, Tunables.WORLD_HEIGHT * 0.060f);
    private final Rectangle viewToggle = new Rectangle(
            Tunables.WORLD_WIDTH * 0.15f, Tunables.WORLD_HEIGHT * 0.255f,
            Tunables.WORLD_WIDTH * 0.70f, Tunables.WORLD_HEIGHT * 0.060f);
    private final Rectangle difficultyToggle = new Rectangle(
            Tunables.WORLD_WIDTH * 0.15f, Tunables.WORLD_HEIGHT * 0.185f,
            Tunables.WORLD_WIDTH * 0.70f, Tunables.WORLD_HEIGHT * 0.060f);

    // Scratch for mapping game points into render space when spawning effects.
    private final Vector2 renderPoint = new Vector2();

    // Effects.
    private final CameraRig cameraRig;
    private final SquashStretch squash;
    private final DebrisField debris;
    private final PerfectBurst burst;
    private final ColorGradient colors;
    private final GameAudio audio;

    private final ScoreStore scoreStore;
    private final Settings settings;
    private final GameState state;
    private final Tower tower;

    private Block moving;
    private int direction;
    private boolean newBest;
    private float elapsed; // for star twinkle (horizontal city drift is frozen in a run)

    public PlayScreen(TowerStackGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
        this.shapes = new ShapeRenderer();

        this.settings = new Settings();
        this.background = new BackgroundRenderer();
        this.blockRenderer = settings.isIsometric() ? isoRenderer : flatRenderer;
        this.text = new TextRenderer();
        this.fade = new ScreenFade();

        this.cameraRig = new CameraRig(camera);
        this.squash = new SquashStretch();
        this.debris = new DebrisField();
        this.burst = new PerfectBurst();
        this.colors = new ColorGradient();
        this.audio = new GameAudio();

        this.scoreStore = new ScoreStore();
        this.state = new GameState();
        this.tower = new Tower();

        audio.setMuted(!settings.isSoundEnabled());
    }

    @Override
    public void show() {
        startRun();
    }

    /** Reset everything for a fresh run: base block, first moving block, camera, effects. */
    private void startRun() {
        state.setDifficulty(settings.difficulty()); // apply the chosen difficulty to this run
        state.reset();
        tower.clear();
        debris.clear();
        burst.clear();
        newBest = false;
        fade.start(Tunables.PLAY_FADE_IN); // fade in on first entry and on every retry

        float baseLeft = (Tunables.WORLD_WIDTH - Tunables.START_WIDTH) / 2f;
        tower.add(new Block(baseLeft, 0f, Tunables.START_WIDTH, Tunables.BLOCK_HEIGHT,
                colors.blockColor(0)));

        spawnMovingBlock();
        cameraRig.followTop(tower.top().topEdge());
        cameraRig.snap();
    }

    /** Spawn the next moving block at the left edge, resting on the current tower top. */
    private void spawnMovingBlock() {
        Block top = tower.top();
        float width = state.getCurrentWidth();
        float bottom = top.topEdge() + DROP_GAP;
        moving = new Block(0f, bottom, width, Tunables.BLOCK_HEIGHT, colors.blockColor(tower.size()));
        direction = +1;
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        handleOptionKeys();

        if (state.isPlaying()) {
            if (dropRequested()) {
                dropBlock();
            } else {
                moveBlock(delta);
            }
        } else {
            handleGameOverInput();
        }

        // Effects advance every frame, whatever the phase, so debris keeps falling and the
        // camera settles even on the game-over screen.
        squash.update(delta);
        debris.update(delta, cameraRig.viewBottom());
        burst.update(delta);
        cameraRig.update(delta);
        fade.update(delta);
        elapsed += delta;
    }

    /** All three inputs — tap, left-click, spacebar — collapse to one "drop" action. */
    private boolean dropRequested() {
        return Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    /** Desktop shortcuts that work in any phase: M sound, V view, D difficulty. */
    private void handleOptionKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            settings.toggleSound();
            applySound();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            settings.toggleIsometric();
            applyView();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            settings.cycleDifficulty();
        }
    }

    /** On game over: a tap on a toggle changes it; a tap anywhere else retries. */
    private void handleGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            startRun();
            return;
        }
        if (!Gdx.input.justTouched()) {
            return;
        }
        Vector2 world = text.unproject(Gdx.input.getX(), Gdx.input.getY());
        if (soundToggle.contains(world.x, world.y)) {
            settings.toggleSound();
            applySound();
        } else if (viewToggle.contains(world.x, world.y)) {
            settings.toggleIsometric();
            applyView();
        } else if (difficultyToggle.contains(world.x, world.y)) {
            settings.cycleDifficulty();
        } else {
            startRun();
        }
    }

    private void applySound() {
        audio.setMuted(!settings.isSoundEnabled());
    }

    /** Point the active renderer at the current view; swaps the frozen tower live on game over. */
    private void applyView() {
        blockRenderer = settings.isIsometric() ? isoRenderer : flatRenderer;
    }

    /**
     * Slide the block horizontally, bouncing off the world edges and easing at the
     * turnarounds so the motion feels alive before it even lands (build brief §6).
     */
    private void moveBlock(float delta) {
        float maxLeft = Tunables.WORLD_WIDTH - moving.getWidth();
        float ease = edgeEase(maxLeft > 0f ? moving.getLeft() / maxLeft : 0.5f);
        float left = moving.getLeft() + direction * state.currentSpeed() * ease * delta;

        if (left <= 0f) {
            left = 0f;
            direction = +1;
        } else if (left >= maxLeft) {
            left = maxLeft;
            direction = -1;
        }
        moving.setLeft(left);
    }

    /** Speed multiplier across the track: slow at the edges (t=0,1), full through the middle. */
    private float edgeEase(float t) {
        float shaped = (float) Math.sin(Math.PI * t);
        return Tunables.SLIDE_EDGE_EASE + (1f - Tunables.SLIDE_EDGE_EASE) * shaped;
    }

    private void dropBlock() {
        Block top = tower.top();
        DropResult result = SliceMath.slice(
                moving.getLeft(), moving.getWidth(),
                top.getLeft(), top.getWidth(),
                state.getDifficulty().getPerfectTolerance());

        if (result.isMiss()) {
            handleMiss(top);
            return;
        }

        boolean perfect = result.getType() == DropResult.Type.PERFECT;
        Block placed = perfect ? buildPerfectBlock(top) : buildSlicedBlock(result);

        tower.add(placed);
        state.recordPlacement(perfect, placed.getWidth());

        squash.trigger();
        cameraRig.punch(Tunables.LAND_PUNCH);
        if (perfect) {
            blockRenderer.project(placed.centerX(), placed.getBottom(), renderPoint);
            burst.trigger(renderPoint.x, renderPoint.y, state.getCombo(), placed.getColor());
            audio.perfect(state.getCombo());
        } else {
            audio.land();
            if (result.getSliceWidth() > 0f) {
                int outward = result.getSliceLeft() < placed.getLeft() ? -1 : +1;
                blockRenderer.project(result.getSliceLeft(), moving.getBottom(), renderPoint);
                debris.spawn(renderPoint.x, renderPoint.y, result.getSliceWidth(),
                        Tunables.BLOCK_HEIGHT, moving.getColor(), outward);
                audio.slice();
            }
        }

        spawnMovingBlock();
        cameraRig.followTop(tower.top().topEdge());
    }

    /** A total miss: fling the block away as debris, punch the camera, end the run. */
    private void handleMiss(Block top) {
        state.gameOver();
        newBest = scoreStore.submit(state.getScore());
        cameraRig.punch(Tunables.MISS_PUNCH);
        audio.gameOver();
        int outward = moving.centerX() < top.centerX() ? -1 : +1;
        blockRenderer.project(moving.getLeft(), moving.getBottom(), renderPoint);
        debris.spawn(renderPoint.x, renderPoint.y, moving.getWidth(),
                Tunables.BLOCK_HEIGHT, moving.getColor(), outward);
    }

    /**
     * A perfect placement keeps its full footprint and regrows a little, rewarding precision
     * by widening the tower back out. The regrown block is centered on the block beneath.
     */
    private Block buildPerfectBlock(Block top) {
        float width = Math.min(Tunables.START_WIDTH, top.getWidth() + Tunables.PERFECT_REGROWTH);
        float left = top.centerX() - width / 2f;
        return new Block(left, moving.getBottom(), width, Tunables.BLOCK_HEIGHT, moving.getColor());
    }

    /**
     * A partial placement narrows the tower to the surviving overlap — but not below the
     * difficulty's minimum-width floor (Easy), keeping runs recoverable. The floored block
     * stays centered on the overlap.
     */
    private Block buildSlicedBlock(DropResult result) {
        float left = result.getSurvivingLeft();
        float width = result.getSurvivingWidth();
        float minWidth = state.getDifficulty().getMinWidth();
        if (minWidth > 0f && width < minWidth) {
            float center = left + width / 2f;
            width = minWidth;
            left = center - width / 2f;
        }
        return new Block(left, moving.getBottom(), width, Tunables.BLOCK_HEIGHT, moving.getColor());
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int height = state.getBlocksPlaced();
        // In a run: no horizontal drift; the city recedes and stars fade in with camera height.
        background.draw(colors.backgroundBottom(height), colors.backgroundTop(height),
                colors.parallax(height), 0f, cameraRig.centerY(), elapsed);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        blockRenderer.drawTower(shapes, tower, squash);
        if (state.isPlaying()) {
            blockRenderer.drawMoving(shapes, moving);
        }
        debris.draw(shapes);
        burst.draw(shapes);
        shapes.end();

        drawHud();
        fade.render();
    }

    private void drawHud() {
        text.begin();
        if (state.isGameOver()) {
            drawGameOverHud();
        } else {
            text.drawCentered(Integer.toString(state.getScore()), Tunables.WORLD_HEIGHT * 0.90f, Color.WHITE, 1.8f);
            if (state.getCombo() >= 2) {
                text.drawCentered("COMBO x" + state.getCombo(), Tunables.WORLD_HEIGHT * 0.84f, Color.GOLD, 1.2f);
            }
        }
        text.end();
    }

    private void drawGameOverHud() {
        text.drawCentered("GAME OVER", Tunables.WORLD_HEIGHT * 0.66f, Color.WHITE, 1.8f);
        text.drawCentered("Score  " + state.getScore(), Tunables.WORLD_HEIGHT * 0.585f, Color.WHITE, 1.2f);
        if (newBest) {
            text.drawCentered("New Best!", Tunables.WORLD_HEIGHT * 0.525f, Color.GOLD, 1.2f);
        } else {
            text.drawCentered("Best  " + scoreStore.best(), Tunables.WORLD_HEIGHT * 0.525f, Color.LIGHT_GRAY, 1.2f);
        }
        text.drawCentered("tap to retry", Tunables.WORLD_HEIGHT * 0.455f, Color.LIGHT_GRAY, 1.1f);

        // Tappable option toggles (same regions as soundToggle / viewToggle / difficultyToggle).
        text.drawCentered("Sound:  " + (settings.isSoundEnabled() ? "On" : "Off"),
                Tunables.WORLD_HEIGHT * 0.350f, Color.LIGHT_GRAY, 1.0f);
        text.drawCentered("View:  " + (settings.isIsometric() ? "Iso" : "Flat"),
                Tunables.WORLD_HEIGHT * 0.280f, Color.LIGHT_GRAY, 1.0f);
        text.drawCentered("Difficulty:  " + settings.difficulty().getLabel(),
                Tunables.WORLD_HEIGHT * 0.210f, Color.LIGHT_GRAY, 1.0f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        background.resize(width, height);
        text.resize(width, height);
        fade.resize(width, height);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        background.dispose();
        text.dispose();
        fade.dispose();
        audio.dispose();
    }
}
