package com.codeheadsystems.towerstack.effects;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.codeheadsystems.towerstack.config.Tunables;

/**
 * Owns the world camera and eases it upward so the top of the tower stays parked at a fixed
 * screen band as the stack climbs (build brief §6, "camera rise" — the effect that does most
 * of the "feels good" work).
 *
 * <p>Isolated and tunable: the only knob is {@link Tunables#CAMERA_RISE_RATE}. The camera's
 * x is fixed at the middle of the world; only y follows the tower.
 *
 * <p>Camera-punch / micro-shake on landing and miss will layer on here in the juice pass.
 */
public class CameraRig {

    private final OrthographicCamera camera;
    private float targetY;

    public CameraRig(OrthographicCamera camera) {
        this.camera = camera;
        this.camera.position.x = Tunables.WORLD_WIDTH / 2f;
    }

    /** Aim the camera so the given world-y (a tower top edge) rests at {@link Tunables#TOP_BAND_Y}. */
    public void followTop(float worldTopY) {
        targetY = worldTopY + Tunables.WORLD_HEIGHT / 2f - Tunables.TOP_BAND_Y;
    }

    /** Jump instantly to the current target (used when starting a run). */
    public void snap() {
        camera.position.y = targetY;
        camera.update();
    }

    /** Ease toward the target height. Frame-rate-independent exponential smoothing. */
    public void update(float delta) {
        float alpha = 1f - (float) Math.exp(-Tunables.CAMERA_RISE_RATE * delta);
        camera.position.y += (targetY - camera.position.y) * alpha;
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
