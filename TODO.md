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

## 2. Easier mode / difficulty options

Today there's a single curve: `BASE_SPEED + HEIGHT_FACTOR * blocksPlaced` capped at
`SPEED_CEILING`, no width floor, fixed `PERFECT_TOLERANCE`.

- Add an Easy/Normal (maybe Hard) setting: scale base speed / height factor, widen the perfect
  tolerance, optionally re-introduce a small `minWidth` floor on Easy so runs stay recoverable.
- Persist the choice in `Settings`; surface it as a title toggle alongside Sound/View.
- Consider a gentle ramp-in (first few blocks slower) regardless of mode.

_Where:_ `config/Tunables` (group the difficulty-driven values, e.g. a small enum/struct),
`model/GameState.currentSpeed`, `util/Settings`, `screens/TitleScreen` toggle.

## 3. Change sound / view (and difficulty) from the game-over screen

Today settings only change on the title; game over just says "tap to retry," and `PlayScreen`
reads settings once at construction.

- Add toggles to the game-over overlay (Sound / View), or a small "back to title" option.
- Sound can already flip live (the `M` key does it). View is chosen at `PlayScreen`
  construction, so switching mid-screen means swapping `blockRenderer` on toggle (a small factory
  or just rebuild the two impls) — cleanest is to swap it immediately when toggled on game over.

_Where:_ `screens/PlayScreen` (game-over input handling + HUD); reuse the tappable-toggle
hit-testing pattern from `TitleScreen`.

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
