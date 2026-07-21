package com.codeheadsystems.towerstack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.TowerStackGame;
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
import com.codeheadsystems.towerstack.render.HudRenderer;
import com.codeheadsystems.towerstack.render.TowerRenderer;
import com.codeheadsystems.towerstack.util.ScoreStore;

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

    // Renderers.
    private final BackgroundRenderer background;
    private final TowerRenderer towerRenderer;
    private final HudRenderer hud;

    // Effects.
    private final CameraRig cameraRig;
    private final SquashStretch squash;
    private final DebrisField debris;
    private final PerfectBurst burst;
    private final ColorGradient colors;

    private final ScoreStore scoreStore;
    private final GameState state;
    private final Tower tower;

    private Block moving;
    private int direction;
    private boolean newBest;

    public PlayScreen(TowerStackGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
        this.shapes = new ShapeRenderer();

        this.background = new BackgroundRenderer();
        this.towerRenderer = new TowerRenderer();
        this.hud = new HudRenderer();

        this.cameraRig = new CameraRig(camera);
        this.squash = new SquashStretch();
        this.debris = new DebrisField();
        this.burst = new PerfectBurst();
        this.colors = new ColorGradient();

        this.scoreStore = new ScoreStore();
        this.state = new GameState();
        this.tower = new Tower();
    }

    @Override
    public void show() {
        startRun();
    }

    /** Reset everything for a fresh run: base block, first moving block, camera, effects. */
    private void startRun() {
        state.reset();
        tower.clear();
        debris.clear();
        burst.clear();
        newBest = false;

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
        if (state.isPlaying()) {
            if (dropRequested()) {
                dropBlock();
            } else {
                moveBlock(delta);
            }
        } else if (dropRequested()) {
            startRun();
        }

        // Effects advance every frame, whatever the phase, so debris keeps falling and the
        // camera settles even on the game-over screen.
        squash.update(delta);
        debris.update(delta, cameraRig.viewBottom());
        burst.update(delta);
        cameraRig.update(delta);
    }

    /** All three inputs — tap, left-click, spacebar — collapse to one "drop" action. */
    private boolean dropRequested() {
        return Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
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
                Tunables.PERFECT_TOLERANCE);

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
            burst.trigger(placed.centerX(), placed.getBottom(), state.getCombo(), placed.getColor());
        } else if (result.getSliceWidth() > 0f) {
            int outward = result.getSliceLeft() < placed.getLeft() ? -1 : +1;
            debris.spawn(result.getSliceLeft(), moving.getBottom(), result.getSliceWidth(),
                    Tunables.BLOCK_HEIGHT, moving.getColor(), outward);
        }

        spawnMovingBlock();
        cameraRig.followTop(tower.top().topEdge());
    }

    /** A total miss: fling the block away as debris, punch the camera, end the run. */
    private void handleMiss(Block top) {
        state.gameOver();
        newBest = scoreStore.submit(state.getScore());
        cameraRig.punch(Tunables.MISS_PUNCH);
        int outward = moving.centerX() < top.centerX() ? -1 : +1;
        debris.spawn(moving.getLeft(), moving.getBottom(), moving.getWidth(),
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

    /** A partial placement narrows the tower to the surviving overlap. */
    private Block buildSlicedBlock(DropResult result) {
        return new Block(
                result.getSurvivingLeft(), moving.getBottom(),
                result.getSurvivingWidth(), Tunables.BLOCK_HEIGHT,
                moving.getColor());
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int height = state.getBlocksPlaced();
        background.draw(colors.backgroundBottom(height), colors.backgroundTop(height));

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        towerRenderer.drawTower(shapes, tower, squash);
        if (state.isPlaying()) {
            towerRenderer.drawMoving(shapes, moving);
        }
        debris.draw(shapes);
        burst.draw(shapes);
        shapes.end();

        if (state.isGameOver()) {
            hud.renderGameOver(state.getScore(), scoreStore.best(), newBest);
        } else {
            hud.renderPlaying(state.getScore(), state.getCombo());
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        background.resize(width, height);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        background.dispose();
        hud.dispose();
    }
}
