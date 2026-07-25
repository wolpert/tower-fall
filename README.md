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
**View: Flat/Iso**, and **Difficulty: Easy/Normal/Hard** lines to change them (they persist).
Sound and view take effect immediately; a difficulty change applies to the next run. On
desktop, **M** toggles sound, **V** toggles the view, and **D** cycles difficulty — all of
them mid-game too.

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

## Tech

- **Language:** Java 17
- **Framework:** [libGDX](https://libgdx.com/) 1.13.1
- **Targets:** Linux desktop (LWJGL3) and Android
- No networking, no accounts, no backend — local high score only.
- No art or audio assets are shipped: blocks are solid rectangles, and the sound effects are
  synthesized procedurally at runtime.

## Building & running

Requires a JDK (17+). The Gradle wrapper (`./gradlew`) fetches everything else.

### Desktop

```bash
./gradlew :lwjgl3:run
```

The window opens in portrait. To build a runnable jar:

```bash
./gradlew :lwjgl3:jar   # output under lwjgl3/build/libs/
```

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
| `config`    | `Tunables` — every gameplay-feel constant in one place; `Difficulty` — the Easy/Normal/Hard knobs |
| `model`     | Pure, libGDX-free game logic: `Block`, `Tower`, `SliceMath`, `DropResult`, `GameState` |
| `screens`   | `TitleScreen`, `PlayScreen` (play + game-over phase)                          |
| `render`    | `BackgroundRenderer` (gradient + parallax); `BlockRenderer` with `FlatBlockRenderer` / `IsoBlockRenderer` |
| `effects`   | Isolated, individually tunable juice: `CameraRig`, `SquashStretch`, `Debris`/`DebrisField`, `PerfectBurst`, `ColorGradient` |
| `audio`     | `ToneSynth` (synthesis + WAV encoding), `GameAudio` (the four sound effects)  |
| `ui`        | `TextRenderer`, `ScreenFade`                                                  |
| `util`      | `ScoreStore` (best score), `Settings` (sound, view, difficulty options)       |

### Tuning

All the numbers that shape how the game feels — speeds, the difficulty curve, perfect
tolerance, width regrowth, squash amount, camera shake, particle counts, colors, volumes —
live as constants in [`core/.../config/Tunables.java`](core/src/main/java/com/codeheadsystems/towerstack/config/Tunables.java).
Each juice effect is its own small class so it can be dialed in independently.

## License

BSD 3-Clause. See [LICENSE](LICENSE).
