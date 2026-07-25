# Tower Stack — TODO / Backlog

Ideas and follow-ups, not yet scheduled. Each note points at where in the code it would land.

## 1. Make the parallax more interesting — DONE (largely)

Replaced the plain bars with a two-layer **city skyline** (varied buildings with warm lit
windows on the near layer) plus a **star field** that fades in with height and twinkles — so a
run carries you from the city up into space. See `render/BackgroundRenderer` and the
`CITY_*` / `STAR_*` / `WINDOW_*` constants in `Tunables`.

Added since: a slow-drifting **moon** (glow + craters) that hangs over the city and among the
stars, and **occasional shooting stars** that streak across once you've climbed into star
territory. See `drawMoon` / `updateShootingStars` / `drawShootingStars` and the `MOON_*` /
`SHOOTING_STAR_*` constants.

Remaining nice-to-haves if we want more later:

- A distant **planet** (variant of the moon), or moon phases.
- Soften/blur the far layer for more depth.
- Tune window density/brightness (currently fairly busy — `WINDOW_ALPHA`, the 0.45 lit chance).

## 2. Easier mode / difficulty options — DONE

A `config/Difficulty` enum now carries the three knobs per level — speed scale, perfect
tolerance, and an optional minimum-width floor:

| Level  | Speed scale | Perfect tolerance | Min width |
| ------ | ----------- | ----------------- | --------- |
| Easy   | 0.80        | 9                 | 40        |
| Normal | 1.00        | 6                 | none      |
| Hard   | 1.30        | 4                 | none      |

`GameState.currentSpeed()` scales both the ramp and the ceiling by the level;
`PlayScreen.dropBlock` reads the tolerance per drop, and `buildSlicedBlock` re-centers a
too-thin slice on the Easy floor. The choice persists in `Settings` (`difficulty()` /
`cycleDifficulty()`, falling back to Normal if a stored value no longer parses) and is applied
to the run in `PlayScreen.startRun`, so a change mid-game-over takes effect on the next retry.
It's a cycling toggle on the title screen and on game over, plus **D** on desktop.

Remaining nice-to-have:

- A gentle ramp-in (first few blocks slower) regardless of mode — not implemented.

## 3. Change sound / view (and difficulty) from the game-over screen — DONE

The game-over overlay now draws tappable **Sound / View / Difficulty** lines under "tap to
retry," using the same rectangle hit-testing as `TitleScreen`; a tap that misses all three
still retries, and space always retries. `PlayScreen` holds both block renderers and points
`blockRenderer` at the active one, so toggling the view swaps the frozen tower live. **M** /
**V** / **D** work in any phase on desktop.

Remaining nice-to-have:

- No "back to title" option — retry is still the only way out of game over.

## 4. Animate the parallax on the title screen — DONE

The title now drifts the city **horizontally** (left) via a time accumulator in
`TitleScreen`; the drift stops in a run (`PlayScreen` passes 0), where height takes over.

## 5. More juice options

Current juice: eased slide, landing squash/stretch, slice debris, camera rise + punch/shake,
perfect burst + shockwave, height color gradient, combo ramp, procedural audio. Ideas to explore
— each as its own small effect class, following the existing `effects/` pattern, triggered from
`PlayScreen.dropBlock` / `handleMiss`, with knobs in `Tunables`:

- Screen-edge flash / vignette pulse on a perfect, and on a miss.
- Trail / ghost or motion streak on the moving block.
- Score/combo **number pop** — the HUD text scales up briefly when it changes.
- Combo **milestone** effects (e.g. every 5 perfects: bigger burst, brief slow-mo, color flare).
- Neighbor wobble — nearby blocks jiggle on a heavy landing.
- Near-miss emphasis — extra shake/whoosh when a surviving slice is very thin.
- Background reactive pulse — parallax layers nudge on landings.
- Android **haptics** — vibrate on land / perfect / miss.
- An optional **juice intensity** setting for players who want more or less.

_Where:_ new classes in `effects/`, wired from `screens/PlayScreen`, tunables in `config/Tunables`.
