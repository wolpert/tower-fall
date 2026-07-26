package com.codeheadsystems.towerstack.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.codeheadsystems.towerstack.config.JuiceLevel;
import com.codeheadsystems.towerstack.config.Tunables;
import java.util.Random;

/**
 * The game's sound effects (build brief §7, §11): a landing click, a slice whoosh, a perfect
 * tone whose pitch rises with the combo, and a game-over thud. All four are synthesized at
 * startup by {@link ToneSynth} — no asset files — written to temp WAVs and loaded as libGDX
 * {@link Sound}s.
 *
 * <p>Audio is best-effort: if a device or codec is unavailable, synthesis fails softly and
 * every play call becomes a no-op, so the game is never blocked by sound.
 */
public class GameAudio implements Disposable {

    private boolean available; // whether an audio device/codec loaded successfully
    private boolean muted;     // player's sound on/off choice
    private JuiceLevel level = JuiceLevel.STORE_BOUGHT;
    private Sound land;
    private Sound slice;
    private Sound perfect;
    private Sound gameOver;
    private Sound sparkle;

    public GameAudio() {
        try {
            land = load("land", buildLand());
            slice = load("slice", buildSlice());
            perfect = load("perfect", buildPerfect());
            gameOver = load("gameover", buildGameOver());
            sparkle = load("sparkle", buildSparkle());
            available = true;
        } catch (Exception e) {
            available = false;
            Gdx.app.error("GameAudio", "sound disabled: " + e.getMessage());
        }
    }

    /** Enable/disable playback per the player's preference (does not free resources). */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /**
     * The juice setting only adds to the mix — the four core effects always play (silence is
     * the Sound toggle's job); the sparkle layer is Freshly Squeezed only.
     */
    public void setLevel(JuiceLevel level) {
        this.level = level;
    }

    private boolean canPlay() {
        return available && !muted;
    }

    /** Soft click when any block lands (an imperfect placement). */
    public void land() {
        if (canPlay()) {
            land.play(Tunables.VOLUME_LAND);
        }
    }

    /** Whoosh when an overhang shears off. */
    public void slice() {
        if (canPlay()) {
            slice.play(Tunables.VOLUME_SLICE);
        }
    }

    /** Bright tone on a perfect placement, pitched up with the combo. */
    public void perfect(int combo) {
        if (canPlay()) {
            perfect.play(Tunables.VOLUME_PERFECT, pitchFor(combo), 0f);
        }
    }

    /**
     * A shimmer layered over the perfect tone once a streak is really going — the top juice
     * level's reward for a run of perfects. No-op below {@link Tunables#SPARKLE_MIN_COMBO}.
     */
    public void sparkle(int combo) {
        if (canPlay() && level.hasExtras() && combo >= Tunables.SPARKLE_MIN_COMBO) {
            sparkle.play(Tunables.VOLUME_SPARKLE, pitchFor(combo), 0f);
        }
    }

    /** Low thud on a miss. */
    public void gameOver() {
        if (canPlay()) {
            gameOver.play(Tunables.VOLUME_GAME_OVER);
        }
    }

    private float pitchFor(int combo) {
        float pitch = 1f + Math.max(0, combo - 1) * Tunables.COMBO_PITCH_STEP;
        return Math.min(Tunables.COMBO_PITCH_MAX, pitch);
    }

    // --- Synthesis --------------------------------------------------------

    /** A short low sine with a fast decay — a soft "chunk". */
    private float[] buildLand() {
        int n = ToneSynth.samples(0.09f);
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            float t = (float) i / ToneSynth.SAMPLE_RATE;
            float env = ToneSynth.attack(i, 0.003f) * ToneSynth.decay(i, 42f);
            out[i] = env * (float) Math.sin(2 * Math.PI * 190 * t);
        }
        return out;
    }

    /** Decaying white noise — a whoosh for the shorn slice. */
    private float[] buildSlice() {
        int n = ToneSynth.samples(0.14f);
        float[] out = new float[n];
        Random noise = new Random(7);
        for (int i = 0; i < n; i++) {
            float env = ToneSynth.attack(i, 0.01f) * ToneSynth.decay(i, 26f);
            out[i] = env * (noise.nextFloat() * 2f - 1f) * 0.9f;
        }
        return out;
    }

    /** A bright two-partial tone — the perfect celebration, replayed at rising pitch. */
    private float[] buildPerfect() {
        int n = ToneSynth.samples(0.18f);
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            float t = (float) i / ToneSynth.SAMPLE_RATE;
            float env = ToneSynth.attack(i, 0.005f) * ToneSynth.decay(i, 10f);
            float fundamental = (float) Math.sin(2 * Math.PI * 660 * t);
            float harmonic = (float) Math.sin(2 * Math.PI * 990 * t);
            out[i] = env * (0.7f * fundamental + 0.3f * harmonic);
        }
        return out;
    }

    /**
     * Four short high blips climbing a major arpeggio — the shimmer over a deep combo. Each
     * note gets its own quick decay, and they overlap slightly so it reads as one gesture.
     */
    private float[] buildSparkle() {
        float[] notes = {1320f, 1760f, 2200f, 2640f}; // E6-ish major arpeggio, up
        float noteSec = 0.05f;
        int step = ToneSynth.samples(noteSec * 0.7f); // overlap the tails
        int n = step * notes.length + ToneSynth.samples(noteSec);
        float[] out = new float[n];
        for (int note = 0; note < notes.length; note++) {
            int start = note * step;
            int length = ToneSynth.samples(noteSec);
            for (int i = 0; i < length && start + i < n; i++) {
                float t = (float) i / ToneSynth.SAMPLE_RATE;
                float env = ToneSynth.attack(i, 0.002f) * ToneSynth.decay(i, 34f);
                out[start + i] += env * 0.5f * (float) Math.sin(2 * Math.PI * notes[note] * t);
            }
        }
        return out;
    }

    /** A low sine sweeping downward — an ominous thud on the miss. */
    private float[] buildGameOver() {
        int n = ToneSynth.samples(0.45f);
        float[] out = new float[n];
        float phase = 0f;
        for (int i = 0; i < n; i++) {
            float progress = (float) i / n;
            float freq = 140f - 70f * progress; // 140 Hz down to 70 Hz
            phase += 2 * Math.PI * freq / ToneSynth.SAMPLE_RATE;
            float env = ToneSynth.attack(i, 0.005f) * ToneSynth.decay(i, 6f);
            out[i] = env * (float) Math.sin(phase);
        }
        return out;
    }

    private Sound load(String name, float[] samples) {
        // Local storage is writable on every backend (app-internal on Android, working dir on
        // desktop); writeBytes creates the parent directory as needed.
        FileHandle handle = Gdx.files.local("towerstack-audio/" + name + ".wav");
        handle.writeBytes(ToneSynth.toWav(samples), false);
        return Gdx.audio.newSound(handle);
    }

    @Override
    public void dispose() {
        if (land != null) {
            land.dispose();
        }
        if (slice != null) {
            slice.dispose();
        }
        if (perfect != null) {
            perfect.dispose();
        }
        if (gameOver != null) {
            gameOver.dispose();
        }
        if (sparkle != null) {
            sparkle.dispose();
        }
    }
}
