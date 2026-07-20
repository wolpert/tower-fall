package com.codeheadsystems.towerstack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.TowerStackGame;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.model.Block;
import com.codeheadsystems.towerstack.model.DropResult;
import com.codeheadsystems.towerstack.model.GameState;
import com.codeheadsystems.towerstack.model.SliceMath;
import com.codeheadsystems.towerstack.model.Tower;

/**
 * The playable loop (build brief §3), grey-box edition — increment 1.
 *
 * <p>A block slides left/right at the top of the tower; one tap drops it; the overhang is
 * sliced away and the next block inherits the surviving width; a total miss ends the run
 * and the next tap restarts. No juice yet — rectangles and a snap camera. Rendering is
 * inlined here for now; it moves into a dedicated renderer during the render/juice pass.
 */
public class PlayScreen extends ScreenAdapter {

    /** Rest height of the moving block above the tower top, in world units. */
    private static final float DROP_GAP = 0f;

    private final TowerStackGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;

    private final GameState state;
    private final Tower tower;

    private Block moving;
    private int direction;

    public PlayScreen(TowerStackGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);
        this.shapes = new ShapeRenderer();
        this.state = new GameState();
        this.tower = new Tower();
    }

    @Override
    public void show() {
        startRun();
    }

    /** Reset everything for a fresh run: base block, first moving block, camera. */
    private void startRun() {
        state.reset();
        tower.clear();

        float baseLeft = (Tunables.WORLD_WIDTH - Tunables.START_WIDTH) / 2f;
        tower.add(new Block(baseLeft, 0f, Tunables.START_WIDTH, Tunables.BLOCK_HEIGHT,
                colorForHeight(0)));

        spawnMovingBlock();
        snapCameraToTop();
    }

    /** Spawn the next moving block at the left edge, resting on the current tower top. */
    private void spawnMovingBlock() {
        Block top = tower.top();
        float width = state.getCurrentWidth();
        float bottom = top.topEdge() + DROP_GAP;
        moving = new Block(0f, bottom, width, Tunables.BLOCK_HEIGHT, colorForHeight(tower.size()));
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
    }

    /** All three inputs — tap, left-click, spacebar — collapse to one "drop" action. */
    private boolean dropRequested() {
        return Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    /** Slide the block horizontally, bouncing off the world edges. */
    private void moveBlock(float delta) {
        float speed = state.currentSpeed();
        float left = moving.getLeft() + direction * speed * delta;

        float maxLeft = Tunables.WORLD_WIDTH - moving.getWidth();
        if (left <= 0f) {
            left = 0f;
            direction = +1;
        } else if (left >= maxLeft) {
            left = maxLeft;
            direction = -1;
        }
        moving.setLeft(left);
    }

    private void dropBlock() {
        Block top = tower.top();
        DropResult result = SliceMath.slice(
                moving.getLeft(), moving.getWidth(),
                top.getLeft(), top.getWidth(),
                Tunables.PERFECT_TOLERANCE);

        if (result.isMiss()) {
            state.gameOver();
            return;
        }

        // The surviving overlap becomes the new tower top. (The shorn slice in the result
        // is ignored for now; the juice pass will turn it into tumbling debris.)
        Block placed = new Block(
                result.getSurvivingLeft(), moving.getBottom(),
                result.getSurvivingWidth(), Tunables.BLOCK_HEIGHT,
                moving.getColor());
        tower.add(placed);
        state.recordPlacement(result.getSurvivingWidth());

        spawnMovingBlock();
        snapCameraToTop();
    }

    /**
     * Keep the top of the tower parked at {@link Tunables#TOP_BAND_Y} on screen. A hard
     * snap for now; increment 2 replaces this with a smooth eased rise.
     */
    private void snapCameraToTop() {
        float topWorldY = tower.top().topEdge();
        camera.position.x = Tunables.WORLD_WIDTH / 2f;
        camera.position.y = topWorldY + Tunables.WORLD_HEIGHT / 2f - Tunables.TOP_BAND_Y;
        camera.update();
    }

    private void draw() {
        // A faint red wash on game over so the state is unmistakable even without text.
        if (state.isGameOver()) {
            Gdx.gl.glClearColor(0.16f, 0.09f, 0.10f, 1f);
        } else {
            Gdx.gl.glClearColor(0.11f, 0.12f, 0.16f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (Block block : tower.blocks()) {
            drawBlock(block);
        }
        if (state.isPlaying()) {
            drawBlock(moving);
        }

        shapes.end();
    }

    private void drawBlock(Block block) {
        shapes.setColor(block.getColor());
        shapes.rect(block.getLeft(), block.getBottom(), block.getWidth(), block.getHeight());
    }

    /**
     * Placeholder palette for the grey-box: a gentle hue drift with height so the stack is
     * readable. The real height-driven gradient is a dedicated effect in the juice pass.
     */
    private Color colorForHeight(int index) {
        float hue = (index * 12f) % 360f;
        // Start opaque; fromHsv sets r/g/b and leaves alpha untouched.
        Color color = new Color(0f, 0f, 0f, 1f);
        color.fromHsv(hue, 0.45f, 0.85f);
        return color;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
