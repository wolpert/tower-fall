package com.codeheadsystems.towerstack.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codeheadsystems.towerstack.config.Tunables;
import com.codeheadsystems.towerstack.effects.Fireworks;
import java.util.Random;

/**
 * The living backdrop (build brief §6/§7 and TODO #1/#4): a vertical gradient, a two-layer city
 * skyline, and a star field.
 *
 * <ul>
 *   <li><b>Title</b> — the city drifts horizontally (left) while the camera sits still, so the
 *       idle screen feels alive.</li>
 *   <li><b>In a run</b> — the horizontal drift stops; the city recedes downward as the camera
 *       rises (each layer at its own fraction of camera speed), and stars fade in with height,
 *       so climbing carries you from the city up into space.</li>
 * </ul>
 *
 * <p>Lives in its own fixed viewport — it never scrolls with the {@code CameraRig}; the scroll
 * is applied to its contents as a function of drift, camera height and time. Colors track the
 * palette. Layout is deterministic (seeded). Self-contained: owns its ShapeRenderer.
 */
public class BackgroundRenderer implements Disposable {

    /** One building: a column with an optional grid of lit windows (near layer only). */
    private static final class Building {
        final float x;
        final float width;
        final float height;
        final float[] windowX; // local x of each lit window (null on the far layer)
        final float[] windowY;

        Building(float x, float width, float height, float[] windowX, float[] windowY) {
            this.x = x;
            this.width = width;
            this.height = height;
            this.windowX = windowX;
            this.windowY = windowY;
        }
    }

    /** One star: a point that scrolls slowly with height and twinkles. */
    private static final class Star {
        final float x;
        final float y;
        final float phase;
        final float speed;

        Star(float x, float y, float phase, float speed) {
            this.x = x;
            this.y = y;
            this.phase = phase;
            this.speed = speed;
        }
    }

    /** A transient streak across the sky. */
    private static final class ShootingStar {
        float x;
        float y;
        final float velocityX;
        final float velocityY;
        float life;
        final float maxLife;

        ShootingStar(float x, float y, float velocityX, float velocityY, float life) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
            this.maxLife = life;
        }
    }

    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Building[] farCity;
    private final Building[] nearCity;
    private final Star[] stars;
    private final Color tint = new Color();
    private final Color streakHead = new Color();
    private final Color streakTail = new Color();

    // Shooting-star state, advanced from the running clock passed to draw().
    private final Array<ShootingStar> shootingStars = new Array<>();
    private float nextShootingStar;
    private float lastTime;

    public BackgroundRenderer() {
        this.shapes = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, camera);

        // Deterministic layout: same city and stars every run.
        Random random = new Random(20260725L);
        this.farCity = buildCity(random, 40f, 90f, 80f, 220f, false);
        this.nearCity = buildCity(random, 55f, 120f, 130f, 340f, true);
        this.stars = buildStars(random);
        this.nextShootingStar = MathUtils.random(
                Tunables.SHOOTING_STAR_MIN_INTERVAL, Tunables.SHOOTING_STAR_MAX_INTERVAL);
    }

    private Building[] buildCity(Random random, float minWidth, float maxWidth,
                                 float minHeight, float maxHeight, boolean windows) {
        // Fill the pattern width edge-to-edge with adjacent buildings of varied size.
        java.util.ArrayList<Building> buildings = new java.util.ArrayList<>();
        float x = 0f;
        while (x < Tunables.CITY_PATTERN_WIDTH) {
            float width = range(random, minWidth, maxWidth);
            float height = range(random, minHeight, maxHeight);
            float[] wx = null;
            float[] wy = null;
            if (windows) {
                float[][] w = buildWindows(random, width, height);
                wx = w[0];
                wy = w[1];
            }
            buildings.add(new Building(x, width, height, wx, wy));
            x += width + range(random, 2f, 14f); // small gaps between buildings
        }
        return buildings.toArray(new Building[0]);
    }

    /** A sparse grid of lit windows inset from the building edges. */
    private float[][] buildWindows(Random random, float width, float height) {
        float margin = 8f;
        float step = 16f;
        java.util.ArrayList<Float> xs = new java.util.ArrayList<>();
        java.util.ArrayList<Float> ys = new java.util.ArrayList<>();
        for (float wy = margin; wy < height - margin; wy += step) {
            for (float wx = margin; wx < width - margin; wx += step) {
                if (random.nextFloat() < 0.45f) {
                    xs.add(wx);
                    ys.add(wy);
                }
            }
        }
        float[] ax = new float[xs.size()];
        float[] ay = new float[ys.size()];
        for (int i = 0; i < ax.length; i++) {
            ax[i] = xs.get(i);
            ay[i] = ys.get(i);
        }
        return new float[][] {ax, ay};
    }

    private Star[] buildStars(Random random) {
        Star[] result = new Star[Tunables.STAR_COUNT];
        for (int i = 0; i < result.length; i++) {
            float x = range(random, 0f, Tunables.WORLD_WIDTH);
            float y = range(random, 0f, Tunables.STAR_PATTERN_HEIGHT);
            float phase = range(random, 0f, 6.283f);
            float speed = range(random, 0.6f, 1.6f) * Tunables.STAR_TWINKLE_SPEED;
            result[i] = new Star(x, y, phase, speed);
        }
        return result;
    }

    private float range(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /**
     * @param silhouette      base color for the buildings (tracks the palette)
     * @param horizontalDrift how far the city has drifted (advances on the title, 0 in a run)
     * @param cameraY         camera height — recedes the city and fades in the stars
     * @param time            seconds, for star twinkle
     * @param fireworks       shells to draw in the sky, behind the skyline; may be null. The
     *                        caller owns and updates them.
     */
    public void draw(Color bottom, Color top, Color silhouette,
                     float horizontalDrift, float cameraY, float time, Fireworks fireworks) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Frame delta from the running clock (clamped so a pause/first frame can't jump).
        float delta = Math.max(0f, Math.min(0.1f, time - lastTime));
        lastTime = time;

        float starVisibility = starVisibility(cameraY);
        updateShootingStars(delta, starVisibility, time);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Gradient: rect(x, y, w, h, bottomLeft, bottomRight, topRight, topLeft).
        shapes.rect(0f, 0f, Tunables.WORLD_WIDTH, Tunables.WORLD_HEIGHT, bottom, bottom, top, top);

        drawStars(starVisibility, cameraY, time);
        drawMoon(time, cameraY);
        drawShootingStars(starVisibility);
        if (fireworks != null) {
            fireworks.draw(shapes); // in the sky, with the skyline drawn in front of it
        }
        drawCity(farCity, silhouette, Tunables.CITY_FAR_HFACTOR, Tunables.CITY_FAR_VFACTOR,
                Tunables.CITY_FAR_ALPHA, Tunables.CITY_FAR_BRIGHTNESS, horizontalDrift, cameraY);
        drawCity(nearCity, silhouette, Tunables.CITY_NEAR_HFACTOR, Tunables.CITY_NEAR_VFACTOR,
                Tunables.CITY_NEAR_ALPHA, Tunables.CITY_NEAR_BRIGHTNESS, horizontalDrift, cameraY);

        shapes.end();
    }

    private float starVisibility(float cameraY) {
        float v = (cameraY - Tunables.STAR_FADE_START) / Tunables.STAR_FADE_RANGE;
        return Math.max(0f, Math.min(1f, v)) * Tunables.STAR_MAX_ALPHA;
    }

    /** A pale glowing disc that drifts slowly across the sky and barely recedes with height. */
    private void drawMoon(float time, float cameraY) {
        float span = Tunables.WORLD_WIDTH + 2f * Tunables.MOON_RADIUS;
        float x = (Tunables.MOON_START_X + time * Tunables.MOON_DRIFT_SPEED + Tunables.MOON_RADIUS) % span;
        if (x < 0f) {
            x += span;
        }
        x -= Tunables.MOON_RADIUS;
        float y = Tunables.MOON_Y - cameraY * Tunables.MOON_VFACTOR;
        float r = Tunables.MOON_RADIUS;

        shapes.setColor(0.90f, 0.93f, 0.80f, Tunables.MOON_GLOW_ALPHA);
        shapes.circle(x, y, r * 1.9f, 48);
        shapes.setColor(0.93f, 0.94f, 0.84f, 1f);
        shapes.circle(x, y, r, 48);
        // A few faint craters for character.
        shapes.setColor(0.85f, 0.86f, 0.76f, 1f);
        shapes.circle(x - r * 0.30f, y + r * 0.28f, r * 0.18f, 16);
        shapes.circle(x + r * 0.34f, y - r * 0.12f, r * 0.12f, 16);
        shapes.circle(x + r * 0.06f, y + r * 0.40f, r * 0.10f, 16);
    }

    private void updateShootingStars(float delta, float visibility, float time) {
        if (time >= nextShootingStar) {
            if (visibility >= Tunables.SHOOTING_STAR_MIN_VISIBILITY) {
                spawnShootingStar();
            }
            nextShootingStar = time + MathUtils.random(
                    Tunables.SHOOTING_STAR_MIN_INTERVAL, Tunables.SHOOTING_STAR_MAX_INTERVAL);
        }
        for (int i = shootingStars.size - 1; i >= 0; i--) {
            ShootingStar s = shootingStars.get(i);
            s.x += s.velocityX * delta;
            s.y += s.velocityY * delta;
            s.life -= delta;
            if (s.life <= 0f) {
                shootingStars.removeIndex(i);
            }
        }
    }

    private void spawnShootingStar() {
        float x = MathUtils.random(0.1f, 0.9f) * Tunables.WORLD_WIDTH;
        float y = MathUtils.random(0.55f, 0.95f) * Tunables.WORLD_HEIGHT;
        // Streak diagonally downward, either to the left or the right.
        float angle = MathUtils.randomBoolean()
                ? MathUtils.random(200f, 235f)
                : MathUtils.random(305f, 340f);
        float vx = MathUtils.cosDeg(angle) * Tunables.SHOOTING_STAR_SPEED;
        float vy = MathUtils.sinDeg(angle) * Tunables.SHOOTING_STAR_SPEED;
        shootingStars.add(new ShootingStar(x, y, vx, vy, Tunables.SHOOTING_STAR_LIFE));
    }

    private void drawShootingStars(float visibility) {
        if (visibility <= 0.001f) {
            return;
        }
        for (ShootingStar s : shootingStars) {
            float speed = (float) Math.sqrt(s.velocityX * s.velocityX + s.velocityY * s.velocityY);
            float dirX = s.velocityX / speed;
            float dirY = s.velocityY / speed;
            float tailX = s.x - dirX * Tunables.SHOOTING_STAR_LENGTH;
            float tailY = s.y - dirY * Tunables.SHOOTING_STAR_LENGTH;
            float perpX = -dirY * Tunables.SHOOTING_STAR_WIDTH / 2f;
            float perpY = dirX * Tunables.SHOOTING_STAR_WIDTH / 2f;

            float alpha = visibility * Math.min(1f, s.life / s.maxLife * 1.6f);
            streakHead.set(1f, 1f, 1f, alpha);
            streakTail.set(1f, 1f, 1f, 0f);

            // A tapering, fading triangle from the bright head to the transparent tail.
            shapes.triangle(
                    s.x + perpX, s.y + perpY,
                    s.x - perpX, s.y - perpY,
                    tailX, tailY,
                    streakHead, streakHead, streakTail);
            shapes.setColor(streakHead);
            shapes.circle(s.x, s.y, 2f, 8);
        }
    }

    private void drawCity(Building[] buildings, Color silhouette, float hFactor, float vFactor,
                          float alpha, float brightness, float horizontalDrift, float cameraY) {
        tint.set(silhouette.r * brightness, silhouette.g * brightness, silhouette.b * brightness, alpha);

        // Horizontal wrap offset (title drift) and vertical recede (camera height).
        float pattern = Tunables.CITY_PATTERN_WIDTH;
        float offsetX = -(horizontalDrift * hFactor) % pattern;
        if (offsetX < 0f) {
            offsetX += pattern;
        }
        float baseY = Tunables.CITY_BASELINE - cameraY * vFactor;

        for (Building building : buildings) {
            // Draw the tile and its neighbors so the skyline wraps seamlessly.
            drawBuilding(building, building.x + offsetX - pattern, baseY);
            drawBuilding(building, building.x + offsetX, baseY);
        }
    }

    private void drawBuilding(Building building, float x, float baseY) {
        if (x > Tunables.WORLD_WIDTH || x + building.width < 0f) {
            return;
        }
        if (baseY + building.height < 0f || baseY > Tunables.WORLD_HEIGHT) {
            return;
        }

        shapes.setColor(tint);
        shapes.rect(x, baseY, building.width, building.height);

        if (building.windowX != null) {
            // Warm lit windows, a little brighter than the silhouette.
            shapes.setColor(1f, 0.85f, 0.5f, Math.min(1f, tint.a + Tunables.WINDOW_ALPHA));
            for (int i = 0; i < building.windowX.length; i++) {
                shapes.rect(x + building.windowX[i], baseY + building.windowY[i],
                        Tunables.WINDOW_SIZE, Tunables.WINDOW_SIZE);
            }
        }
    }

    private void drawStars(float visibility, float cameraY, float time) {
        if (visibility <= 0.001f) {
            return;
        }

        float pattern = Tunables.STAR_PATTERN_HEIGHT;
        float half = Tunables.STAR_SIZE / 2f;
        for (Star star : stars) {
            float y = (star.y - cameraY * Tunables.STAR_FACTOR) % pattern;
            if (y < 0f) {
                y += pattern;
            }
            float twinkle = 0.6f + 0.4f * (float) Math.sin(time * star.speed + star.phase);
            tint.set(1f, 1f, 1f, visibility * twinkle);
            shapes.setColor(tint);
            shapes.rect(star.x - half, y - half, Tunables.STAR_SIZE, Tunables.STAR_SIZE);
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
