# Tower Stack

A fast, tactile arcade block-stacker where the whole reward is in the feel.

A block slides back and forth across the top of the screen. Tap to drop it onto the tower
below. Any part that overhangs the block beneath shears off and tumbles away, so the tower
narrows toward a point as your timing drifts. Land a block dead-center and you keep your full
width, bank a combo, and the screen celebrates. Miss entirely and it's over.

One input. No text to read. The entire game is timing plus juice.

## Controls

One action — **drop** — mapped to all of:

- **Tap** (touch screen)
- **Left-click** (mouse)
- **Spacebar**

Tap to start on the title screen; tap to retry on game over.

On the title screen — and on the game-over screen — tap the **Sound: On/Off**,
**View: Flat/Iso**, **Difficulty: Easy/Normal/Hard**, and
**Juice: None → Store Bought → Freshly Squeezed → Crushed and Ground** lines to change them
(they persist). Sound, view
and juice take effect immediately; a difficulty change applies to the next run. On desktop,
**M** toggles sound, **V** toggles the view, **D** cycles difficulty, and **J** cycles juice —
all of them mid-game too.

## Gameplay

- **Score** is the number of blocks you place (your height), plus a bonus on perfect
  placements that scales with your combo.
- A **perfect** placement (edges aligned within a small tolerance) keeps your full width,
  regrows it a little, extends your combo, and pops a celebration that grows with the streak.
- A **partial** placement shears off the overhang and narrows the tower.
- A **miss** (no overlap at all) ends the run.
- Speed ramps with height, up to a ceiling. The best score is saved locally.

An optional **isometric view** renders the same game as a 3D block skin, and a subtle
**parallax** skyline drifts behind the tower as you climb.

## Juice

The whole game is timing plus juice, so how much of it you get is a setting:

| Level                | What you get                                                          |
|----------------------|-----------------------------------------------------------------------|
| **None**             | Bare blocks — no shake, squash, debris or particles. Just the stack.   |
| **Store Bought**     | The house tuning: eased slide, landing squash, tumbling slice debris, camera rise and punch, the perfect burst, color drift. |
| **Freshly Squeezed** | All of that turned up, plus hit-stop on impact, screen flashes, a camera zoom punch, a tower that sways, a motion trail behind the sliding block, dust out of every seam, debris that shatters, floating score popups, a pulsing HUD, and a sparkle over deep combos. |
| **Crushed and Ground** | No restraint. Everything louder again, a half-second flash on a perfect, shockwave rings, a camera that rolls on impact, the whole tower rattling on a miss, a rainbow trail, fireworks over the skyline (on the title screen too), an **ON FIRE** flare with confetti every five perfects — and a ringed **Saturn** that slides into the sky once you have climbed high enough for it. |

It's cosmetic, so a change applies immediately — including mid-run.

## Tech

- **Language:** Java 17
- **Framework:** [libGDX](https://libgdx.com/) 1.13.1
- **Targets:** desktop (LWJGL3 — Linux, Windows, macOS) and Android
- No networking, no accounts, no backend — local high score only.
- No art or audio assets are shipped: blocks are solid rectangles, and the sound effects are
  synthesized procedurally at runtime.

## Building & running

Requires a JDK (17+). The Gradle wrapper (`./gradlew`) fetches everything else.

### Desktop

```bash
./gradlew :lwjgl3:run
```

The window opens in portrait.

### Shipping it

One file, every desktop OS:

```bash
./gradlew :lwjgl3:dist         # lwjgl3/build/dist/tower-stack.jar   (~14 MB)
./gradlew :lwjgl3:executable   # lwjgl3/build/dist/tower-stack       (the same, self-running)
./gradlew :lwjgl3:releaseZip   # lwjgl3/build/dist/tower-stack.zip   (both, plus a README)
```

`dist` bundles the game and every dependency — including the native libraries for Linux
(x64/arm32/arm64/riscv64), Windows (x64/x86) and macOS (x64/arm64) — into a single jar. LWJGL
and libGDX each unpack the right one for the host at startup, so the same file runs anywhere:

```bash
java -jar tower-stack.jar
```

`executable` prepends a shell stub to that jar. Because a zip's index is read from the end of
the file, the result is *both* a shell script and a valid jar — `./tower-stack` on Linux and
macOS, `java -jar tower-stack` on Windows.

`releaseZip` is the one to hand to a player: it packs both of those and a player-facing README
(kept at [`lwjgl3/src/release/README.md`](lwjgl3/src/release/README.md)) into a
`tower-stack/` folder inside the archive, preserving the executable bit so `./tower-stack`
works straight out of the unzipped directory. Note that `distZip` and `distTar` are the
`application` plugin's own tasks and bundle the *thin* jar with generated start scripts —
a different thing entirely.

The only requirement on the player's machine is **Java 17 or newer**. If you would rather they
didn't need it, the jar is also the natural input to `jpackage`, which produces a native
installer with a bundled runtime — but that has to be built on each OS, so it wants a CI matrix.

On macOS the launcher relaunches itself once with `-XstartOnFirstThread`, which GLFW requires
there; see `StartOnFirstThreadHelper`.

### Android

Requires the Android SDK. Point Gradle at it with a `local.properties` file in the project
root (this file is machine-specific and git-ignored):

```properties
sdk.dir=/path/to/Android/Sdk
```

Then build and install:

```bash
./gradlew :android:assembleDebug                 # APK under android/build/outputs/apk/debug/
./gradlew :android:installDebug                   # install on a connected device/emulator
adb shell am start -n com.codeheadsystems.towerstack/.android.AndroidLauncher
```

- `minSdk` 21, `targetSdk`/`compileSdk` 34.
- Native libraries for all four ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) are
  extracted from the libGDX platform artifacts and packaged automatically.

## Project structure

A standard libGDX Gradle multi-module layout:

```
core/     Platform-agnostic game code (all the logic and rendering)
lwjgl3/   Desktop launcher (LWJGL3 backend)
android/  Android launcher, manifest, resources
```

Inside `core` (package `com.codeheadsystems.towerstack`):

| Package     | Contents                                                                     |
|-------------|------------------------------------------------------------------------------|
| `config`    | `Tunables` — every gameplay-feel constant in one place; `Difficulty` — the Easy/Normal/Hard knobs; `JuiceLevel` — how much juice to serve |
| `model`     | Pure, libGDX-free game logic: `Block`, `Tower`, `SliceMath`, `DropResult`, `GameState` |
| `screens`   | `TitleScreen`, `PlayScreen` (play + game-over phase)                          |
| `render`    | `BackgroundRenderer` (gradient + parallax); `BlockRenderer` with `FlatBlockRenderer` / `IsoBlockRenderer` |
| `effects`   | Isolated, individually tunable juice: `CameraRig`, `SquashStretch`, `Debris`/`DebrisField`, `PerfectBurst`, `ColorGradient`, `HitStop`, `TowerSway`, `MotionTrail`, `DustPuff`, `Fireworks`, `Confetti` |
| `audio`     | `ToneSynth` (synthesis + WAV encoding), `GameAudio` (the sound effects)       |
| `ui`        | `TextRenderer`, `ScreenFade`, `ScreenFlash`, `ScorePopups`                     |
| `util`      | `ScoreStore` (best score), `Settings` (sound, view, difficulty, juice options) |

### Tuning

All the numbers that shape how the game feels — speeds, the difficulty curve, perfect
tolerance, width regrowth, squash amount, camera shake, hit-stop lengths, sway, particle
counts, colors, volumes — live as constants in [`core/.../config/Tunables.java`](core/src/main/java/com/codeheadsystems/towerstack/config/Tunables.java).
Each juice effect is its own small class so it can be dialed in independently.

## License

BSD 3-Clause. See [LICENSE](LICENSE).
