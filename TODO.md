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

## 5. More juice options — DONE

A `config/JuiceLevel` enum now carries the presentation setting, with a scalar the shared
effects multiply by and a flag gating the new ones:

| Level              | Intensity | Extras | Over the top |
| ------------------ | --------- | ------ | ------------ |
| None               | 0.0       | no     | no           |
| Store Bought       | 1.0       | no     | no           |
| Freshly Squeezed   | 1.4       | yes    | no           |
| Crushed and Ground | 2.2       | yes    | yes          |

**None** strips every reaction — no shake, squash, debris, burst — leaving bare blocks (the
scenery layers stay; they aren't reactions). **Store Bought** is the original §6 tuning.
**Freshly Squeezed** turns that up and adds:

- **Hit-stop** (`effects/HitStop`) — the world runs at a tenth speed for 55–200 ms on a perfect
  (scaling with combo) or a miss. `PlayScreen.update` runs the world on the delta it returns and
  keeps input, overlays and background on the real clock.
- **Screen flash** (`ui/ScreenFlash`) — block-tinted on a perfect, brightening with the combo;
  red on a miss.
- **Camera zoom punch** (`CameraRig.zoomPunch`) — shoves in on a perfect, lurches out on a miss.
- **Tower sway** (`effects/TowerSway`) — a damped lean kicked by every landing, largest at the
  crown, rigid at the planted base. Both block renderers apply it per block.
- **Motion trail** (`effects/MotionTrail`) — fading ghosts behind the sliding block, drawn
  through the active renderer so it works in either view.
- **Landing dust** (`effects/DustPuff`) — a puff out of the seam on *every* landing, which is
  what the ordinary (non-perfect) placement was missing.
- **Debris shatter** (`DebrisField.shatter`) — the shorn slice breaks into up to four tumbling
  pieces, outermost thrown hardest.
- **Score popups** (`ui/ScorePopups`) — "PERFECT +4" / "+1" / "MISS" rising from the seam.
- **HUD pulse** — score and combo text punch up on a placement (`scorePulse` in `PlayScreen`).
- **Combo sparkle** (`GameAudio.sparkle`) — a synthesized arpeggio layered over the perfect tone
  from combo 3 up.

**Crushed and Ground** takes the lid off:

- **A half-second flash** on a perfect, up to 0.70 alpha and climbing with the combo (stopping
  short of a whiteout, past which the tower stops being readable), and a heavier red on a miss.
- **Tower rattle** (`TowerSway.rattle`) — a miss shakes the whole stack, not just the crown. The
  phase steps per block, so the shock visibly travels up the tower rather than shifting it
  rigidly.
- **Fireworks** (`effects/Fireworks`) — shells launched from the skyline that arc up and burst,
  one or more on every perfect, a volley on a milestone, and a send-off on game over. They live
  in the backdrop's fixed screen space (drawn by `BackgroundRenderer`, behind the city) so they
  hang in the sky instead of scrolling with the tower. The title screen runs them ambiently at
  this level, so picking it previews itself.
- **Camera roll** (`CameraRig.rollPunch`) — the view tips a few degrees on impact and rocks back.
  The backdrop deliberately stays level; the tower tipping against a level sky is what sells it.
- **Shockwave rings** — a stagger of expanding annuli out of the perfect seam.
- **Confetti** (`effects/Confetti`) — screen-space paper, in front of everything.
- **Milestone flare** — every `COMBO_MILESTONE` (5) perfects: an **ON FIRE xN** banner, a
  firework volley, confetti and an extra kick.
- **A longer, hue-cycling motion trail**.

The choice persists in `Settings` (`juice()` / `cycleJuice()`), is a fourth tappable line on the
title and game-over screens, and is **J** on desktop. Unlike difficulty it applies live — it's
cosmetic — and dialing it down clears whatever is still on screen.

Remaining nice-to-haves from the original list:

- Near-miss emphasis — extra shake/whoosh when a surviving slice is very thin.
- Background reactive pulse — parallax layers nudge on landings. (The backdrop follows neither
  the zoom punch nor the roll, which would be the same plumbing.)
- Android **haptics** — vibrate on land / perfect / miss.
- Audio has no over-the-top tier of its own — Crushed and Ground sounds like Freshly Squeezed.
