# Tower Stack — TODO / Backlog

Ideas and follow-ups, not yet scheduled. Each note points at where in the code it would land.

## 1. Make the parallax more interesting

Today it's two layers of plain rectangular silhouettes that scroll with height — it reads as
"just blocks moving." Explore richer content:

- **Proper city skyline** — varied building shapes (setbacks, antennae, tiny lit windows),
  grouped into a continuous silhouette rather than free-floating bars.
- **Star field / astronomical** — twinkling stars, a slow-drifting moon or planet, the odd
  shooting star. Pairs well with the dark palette.
- **Height-driven transition** — skyline down low, stars/space up high, so climbing changes the
  backdrop and reinforces progression.
- **Depth cues** — more layers, soften/blur the far layer, a little horizontal drift.

_Where:_ `render/BackgroundRenderer` (`Bar` / `drawLayer`), `effects/ColorGradient.parallax`,
`Tunables` parallax section. Consider a `ParallaxLayer` abstraction so a layer's *content*
(bars vs. stars vs. objects) is pluggable.

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

## 4. Animate the parallax on the title screen

The parallax is drawn on the title but sits **static**, because nothing moves the camera there
(`cameraY` is passed as 0).

- Give the title a slow, continuous parallax drift so it feels alive.
- Simplest: accumulate elapsed time and pass `elapsed * driftSpeed` as the `cameraY` argument to
  `background.draw`.

_Where:_ `screens/TitleScreen.draw` (and a small time accumulator in `render`).

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
