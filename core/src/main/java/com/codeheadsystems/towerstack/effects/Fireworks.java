package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Fireworks over the city — shells launched from the skyline that arc up on a tail of sparks
 * and burst into a colored shower. Crushed and Ground only.
 *
 * <p>These live in the backdrop's fixed screen space rather than the world, so they hang in the
 * sky behind the tower instead of scrolling away as the camera climbs; {@code BackgroundRenderer}
 * draws them into its own batch, between the stars and the skyline, so the buildings stay in
 * front. The owning screen updates them.
 */
public class Fireworks {

    /** The palette a burst picks from — deliberately not the tower's palette. */
    private static final Color[] PALETTE = {
        new Color(1f, 0.85f, 0.35f, 1f),   // gold
        new Color(1f, 0.42f, 0.58f, 1f),   // pink
        new Color(0.45f, 0.85f, 1f, 1f),   // cyan
        new Color(0.70f, 1f, 0.58f, 1f),   // green
        new Color(1f, 0.62f, 0.30f, 1f),   // orange
        new Color(0.85f, 0.76f, 1f, 1f),   // violet
    };

    private static final class Shell {
        float x;
        float y;
        float velocityX;
        float velocityY;
        float fuse;
        final Color color;

        Shell(float x, float y, float velocityX, float velocityY, float fuse, Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.fuse = fuse;
            this.color = color;
        }
    }

    private static final class Spark {
        float x;
        float y;
        float velocityX;
        float velocityY;
        float life;
        final float maxLife;
        final float phase; // so they don't all flicker in lockstep
        final Color color;

        Spark(float x, float y, float velocityX, float velocityY, float life, Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
            this.maxLife = life;
            this.phase = MathUtils.random(MathUtils.PI2);
            this.color = color;
        }
    }

    private final Array<Shell> shells = new Array<>();
    private final Array<Spark> sparks = new Array<>();
    private final Color scratch = new Color();

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;
    private float untilNextAmbient = -1f;

    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.isOverTheTop()) {
            clear();
        }
    }

    /** Send up {@code count} shells from random points along the skyline. */
    public void launch(int count) {
        if (!level.isOverTheTop()) {
            return;
        }
        for (int i = 0; i < count; i++) {
            Color color = PALETTE[MathUtils.random(PALETTE.length - 1)];
            shells.add(new Shell(
                    MathUtils.random(0.08f, 0.92f) * Tunables.WORLD_WIDTH,
                    Tunables.FIREWORK_LAUNCH_Y,
                    MathUtils.random(-Tunables.FIREWORK_SHELL_DRIFT, Tunables.FIREWORK_SHELL_DRIFT),
                    MathUtils.random(Tunables.FIREWORK_SHELL_SPEED_MIN,
                            Tunables.FIREWORK_SHELL_SPEED_MAX),
                    MathUtils.random(Tunables.FIREWORK_FUSE_MIN, Tunables.FIREWORK_FUSE_MAX),
                    color));
        }
    }

    /**
     * Keep a lazy display going on its own — used by the title screen, so picking this juice
     * level shows you what you are in for before the run starts.
     */
    public void ambient(float delta) {
        if (!level.isOverTheTop()) {
            return;
        }
        untilNextAmbient -= delta;
        if (untilNextAmbient <= 0f) {
            launch(MathUtils.random(1, 2));
            untilNextAmbient = MathUtils.random(
                    Tunables.FIREWORK_TITLE_MIN_INTERVAL, Tunables.FIREWORK_TITLE_MAX_INTERVAL);
        }
    }

    public void update(float delta) {
        for (int i = shells.size - 1; i >= 0; i--) {
            Shell shell = shells.get(i);
            shell.velocityY -= Tunables.FIREWORK_GRAVITY * delta;
            shell.x += shell.velocityX * delta;
            shell.y += shell.velocityY * delta;
            shell.fuse -= delta;
            // Burst on the fuse, or at the top of the arc, whichever comes first.
            if (shell.fuse <= 0f || shell.velocityY <= 0f) {
                burst(shell);
                shells.removeIndex(i);
            }
        }

        for (int i = sparks.size - 1; i >= 0; i--) {
            Spark spark = sparks.get(i);
            spark.life -= delta;
            if (spark.life <= 0f) {
                sparks.removeIndex(i);
                continue;
            }
            float drag = (float) Math.exp(-Tunables.FIREWORK_SPARK_DRAG * delta);
            spark.velocityX *= drag;
            spark.velocityY *= drag;
            spark.velocityY -= Tunables.FIREWORK_SPARK_GRAVITY * delta;
            spark.x += spark.velocityX * delta;
            spark.y += spark.velocityY * delta;
        }
    }

    private void burst(Shell shell) {
        for (int i = 0; i < Tunables.FIREWORK_SPARKS; i++) {
            float angle = MathUtils.PI2 * i / Tunables.FIREWORK_SPARKS
                    + MathUtils.random(-0.08f, 0.08f);
            // Bias toward the rim so the burst reads as a shell, not a filled blob.
            float speed = Tunables.FIREWORK_SPARK_SPEED * MathUtils.random(0.65f, 1f);
            sparks.add(new Spark(
                    shell.x, shell.y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    Tunables.FIREWORK_SPARK_LIFE * MathUtils.random(0.7f, 1f),
                    shell.color));
        }
    }

    /** Draw into a batch the caller has already begun (the backdrop's). */
    public void draw(ShapeRenderer shapes) {
        for (Shell shell : shells) {
            // A bright head with a tail trailing back down its flight path.
            float speed = (float) Math.sqrt(
                    shell.velocityX * shell.velocityX + shell.velocityY * shell.velocityY);
            float tailX = speed > 0f ? -shell.velocityX / speed * Tunables.FIREWORK_TAIL : 0f;
            float tailY = speed > 0f ? -shell.velocityY / speed * Tunables.FIREWORK_TAIL : 0f;
            scratch.set(shell.color.r, shell.color.g, shell.color.b, 0f);
            shapes.triangle(
                    shell.x - 2f, shell.y, shell.x + 2f, shell.y,
                    shell.x + tailX, shell.y + tailY,
                    shell.color, shell.color, scratch);
            shapes.setColor(1f, 1f, 0.9f, 1f);
            shapes.circle(shell.x, shell.y, 3f, 8);
        }

        for (Spark spark : sparks) {
            float remaining = spark.life / spark.maxLife;
            // Flicker harder as it dies, the way a real ember does.
            float flicker = 0.65f + 0.35f * MathUtils.sin(spark.phase + (1f - remaining) * 40f);
            float size = Tunables.FIREWORK_SPARK_SIZE * (0.4f + 0.6f * remaining);
            scratch.set(spark.color.r, spark.color.g, spark.color.b, remaining * flicker);
            shapes.setColor(scratch);
            shapes.rect(spark.x - size / 2f, spark.y - size / 2f, size, size);
        }
    }

    public void clear() {
        shells.clear();
        sparks.clear();
        untilNextAmbient = -1f;
    }
}
