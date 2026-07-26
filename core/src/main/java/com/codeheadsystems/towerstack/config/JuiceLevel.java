package com.codeheadsystems.towerstack.config;

/**
 * How much juice the game serves. A player-selectable presentation setting that sits alongside
 * {@link Difficulty} but changes nothing about the rules — only how loudly the game reacts.
 *
 * <ul>
 *   <li><b>None</b> — a clean, still presentation: no shake, no squash, no particles, no
 *       debris. Useful if motion is unwelcome or the device is slow.</li>
 *   <li><b>Store Bought</b> — the original juice pass (build brief §6): camera rise and punch,
 *       landing squash, tumbling slice debris, the perfect burst, color drift.</li>
 *   <li><b>Freshly Squeezed</b> — everything above, turned up by {@link #getIntensity()}, plus
 *       the extras gated on {@link #hasExtras()}: hit-stop on impact, screen flashes, a camera
 *       zoom punch, a swaying tower, a motion trail behind the sliding block, landing dust,
 *       shattering debris, floating score popups, a pulsing HUD and a combo sparkle.</li>
 * </ul>
 *
 * <p>Pure config — no libGDX. Effects read the level rather than the screen branching on it,
 * so each effect stays independently tunable.
 */
public enum JuiceLevel {

    NONE("None", 0f, false),
    STORE_BOUGHT("Store Bought", 1f, false),
    FRESHLY_SQUEEZED("Freshly Squeezed", 1.4f, true);

    private final String label;
    private final float intensity; // multiplies the shared effect magnitudes; 0 disables them
    private final boolean extras;  // gates the effects that exist only at the top level

    JuiceLevel(String label, float intensity, boolean extras) {
        this.label = label;
        this.intensity = intensity;
        this.extras = extras;
    }

    public String getLabel() {
        return label;
    }

    /** Scale applied to shake, squash, particle counts and the like. */
    public float getIntensity() {
        return intensity;
    }

    /** Whether the Freshly Squeezed-only effects run. */
    public boolean hasExtras() {
        return extras;
    }

    /** Whether any juice at all is served (false only for {@link #NONE}). */
    public boolean isOn() {
        return intensity > 0f;
    }

    /** The next level, wrapping None → Store Bought → Freshly Squeezed → None. */
    public JuiceLevel next() {
        JuiceLevel[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
