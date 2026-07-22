package com.codeheadsystems.towerstack.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * A tiny audio synthesis toolkit. The game ships no sound assets (build brief §7), so its
 * effects are built here from oscillators, noise and envelopes, then encoded to 16-bit PCM
 * WAV bytes that libGDX can load as a {@code Sound}.
 *
 * <p>All samples are mono, normalized to roughly [-1, 1]; the WAV encoder clamps on the way
 * out. Pure functions — no libGDX, no state.
 */
public final class ToneSynth {

    public static final int SAMPLE_RATE = 44100;

    private ToneSynth() {
        // Static utility.
    }

    /** Attack ramp: 0 climbing to 1 over {@code attackSec}, then held at 1. Avoids a click-on. */
    public static float attack(int sample, float attackSec) {
        float attackSamples = attackSec * SAMPLE_RATE;
        return sample < attackSamples ? sample / attackSamples : 1f;
    }

    /** Exponential decay to (near) silence, so the tail fades out smoothly. */
    public static float decay(int sample, float decayPerSec) {
        return (float) Math.exp(-decayPerSec * sample / SAMPLE_RATE);
    }

    /** Seconds → sample count. */
    public static int samples(float seconds) {
        return (int) (seconds * SAMPLE_RATE);
    }

    /** Encode mono float samples as a 16-bit PCM WAV file image. */
    public static byte[] toWav(float[] samples) {
        int dataSize = samples.length * 2;
        int byteRate = SAMPLE_RATE * 2; // mono, 2 bytes/sample

        ByteBuffer buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(ascii("RIFF"));
        buffer.putInt(36 + dataSize);
        buffer.put(ascii("WAVE"));
        buffer.put(ascii("fmt "));
        buffer.putInt(16);              // fmt chunk size
        buffer.putShort((short) 1);     // PCM
        buffer.putShort((short) 1);     // mono
        buffer.putInt(SAMPLE_RATE);
        buffer.putInt(byteRate);
        buffer.putShort((short) 2);     // block align
        buffer.putShort((short) 16);    // bits per sample
        buffer.put(ascii("data"));
        buffer.putInt(dataSize);

        for (float sample : samples) {
            float clamped = Math.max(-1f, Math.min(1f, sample));
            buffer.putShort((short) (clamped * Short.MAX_VALUE));
        }
        return buffer.array();
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.US_ASCII);
    }
}
