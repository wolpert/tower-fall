package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Owns the world camera. Two jobs, both from build brief §6:
 *
 * <ul>
 *   <li><b>Camera rise</b> — eases the view upward so the top of the tower stays parked at a
 *       fixed screen band as the stack climbs (the effect that does most of the "feels good"
 *       work).</li>
 *   <li><b>Camera punch / micro-shake</b> — a small trauma impulse on landing and a bigger
 *       one on a miss, decaying quickly.</li>
 *   <li><b>Zoom punch</b> — a shove in on a perfect and a lurch out on a miss, easing back to
 *       rest (Freshly Squeezed only).</li>
 * </ul>
 *
 * <p>The eased base position is tracked separately from the camera's actual position so the
 * additive shake offset never feeds back into the smoothing.
 */
public class CameraRig {

    private final OrthographicCamera camera;
    private final float centerX;

    private float positionY;  // eased base height, before shake
    private float targetY;
    private float trauma;     // 0..1, decays over time
    private float zoomOffset; // additive on the camera's 1.0 rest zoom, decays to 0

    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;

    public CameraRig(OrthographicCamera camera) {
        this.camera = camera;
        this.centerX = Tunables.WORLD_WIDTH / 2f;
    }

    /** Scale the shake with the juice setting and gate the zoom punch on the top level. */
    public void setLevel(JuiceLevel level) {
        this.level = level;
        if (!level.hasExtras()) {
            zoomOffset = 0f;
        }
        if (!level.isOn()) {
            trauma = 0f;
        }
    }

    /** Aim the camera so the given world-y (a tower top edge) rests at {@link Tunables#TOP_BAND_Y}. */
    public void followTop(float worldTopY) {
        targetY = worldTopY + Tunables.WORLD_HEIGHT / 2f - Tunables.TOP_BAND_Y;
    }

    /** Jump instantly to the current target (used when starting a run). */
    public void snap() {
        positionY = targetY;
        trauma = 0f;
        zoomOffset = 0f;
        apply(0f, 0f);
    }

    /** Add a trauma impulse (e.g. {@link Tunables#LAND_PUNCH} or {@link Tunables#MISS_PUNCH}). */
    public void punch(float amount) {
        if (!level.isOn()) {
            return;
        }
        trauma = Math.min(1f, trauma + amount * level.getIntensity());
    }

    /**
     * Shove the view in or out. Negative pushes in (a perfect), positive pulls back (a miss);
     * the offset eases back to the rest zoom over {@link Tunables#ZOOM_RECOVER_RATE}.
     */
    public void zoomPunch(float amount) {
        if (!level.hasExtras()) {
            return;
        }
        zoomOffset += amount * level.getIntensity();
    }

    /** Ease toward the target height and apply decaying shake and zoom. Frame-rate-independent. */
    public void update(float delta) {
        float alpha = 1f - (float) Math.exp(-Tunables.CAMERA_RISE_RATE * delta);
        positionY += (targetY - positionY) * alpha;

        trauma = Math.max(0f, trauma - Tunables.CAMERA_TRAUMA_DECAY * delta);
        float shake = trauma * trauma; // ease the falloff so small trauma is gentle
        float maxShake = Tunables.CAMERA_MAX_SHAKE * level.getIntensity();
        float offsetX = shake * maxShake * MathUtils.random(-1f, 1f);
        float offsetY = shake * maxShake * MathUtils.random(-1f, 1f);

        zoomOffset *= (float) Math.exp(-Tunables.ZOOM_RECOVER_RATE * delta);
        apply(offsetX, offsetY);
    }

    private void apply(float offsetX, float offsetY) {
        camera.zoom = 1f + zoomOffset;
        camera.position.set(centerX + offsetX, positionY + offsetY, 0f);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    /** World-y of the bottom edge of the view — used to cull debris that has fallen away. */
    public float viewBottom() {
        return positionY - Tunables.WORLD_HEIGHT / 2f;
    }

    /** Eased view-center height (no shake) — drives the background parallax scroll. */
    public float centerY() {
        return positionY;
    }
}
