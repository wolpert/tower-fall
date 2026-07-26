# Tower Stack

A fast, tactile arcade block-stacker where the whole reward is in the feel.

A block slides back and forth across the top of the screen. Tap to drop it onto the tower
below. Any part that overhangs the block beneath shears off and tumbles away, so the tower
narrows toward a point as your timing drifts. Land a block dead-center and you keep your full
width, bank a combo, and the screen celebrates. Miss entirely and it's over.

## Running it

You need **Java 17 or newer**. To check what you have:

```
java -version
```

If that fails, or the version is older than 17, install a JDK from
[adoptium.net](https://adoptium.net/).

### Linux and macOS

```
./tower-stack
```

If you get "permission denied", the executable bit was lost in transit — restore it with
`chmod +x tower-stack`.

### Windows

```
java -jar tower-stack.jar
```

Double-clicking `tower-stack.jar` also works if Java is associated with `.jar` files.

Both files contain the same game: `tower-stack` is simply `tower-stack.jar` with a small
launcher script on the front, so use whichever suits your system. Either one runs on Linux,
Windows and macOS — the libraries for all three are already inside.

## Controls

One action — **drop** — mapped to all of:

- **Tap** (touch screen)
- **Left-click** (mouse)
- **Spacebar**

Tap to start on the title screen; tap to retry on game over.

On the title screen — and on the game-over screen — tap the **Sound**, **View**,
**Difficulty** and **Juice** lines to change them. They're remembered between runs. Sound,
view and juice take effect immediately; a difficulty change applies to the next run. On a
keyboard, **M** toggles sound, **V** the view, **D** cycles difficulty and **J** cycles juice,
all of them mid-game too.

**Juice** is how loudly the game reacts to you, from **None** (bare blocks, no shake or
particles — the one to pick if motion bothers you or the machine is slow) through **Store
Bought** and **Freshly Squeezed** up to **Crushed and Ground**, which holds nothing back.

## Where it keeps things

- **Settings and your best score** live in `.prefs/tower-stack` in your home folder.
- **Sound effects** are synthesized when the game starts rather than shipped as files, and
  cached in a `towerstack-audio` folder created in whatever directory you run from.

Nothing else is written, and the game never touches the network.

## Troubleshooting

**"java: command not found"** (or "'java' is not recognized" on Windows) — Java either isn't
installed or isn't on your `PATH`. See *Running it* above.

**macOS says the app "cannot be opened because it is from an unidentified developer"** — the
file isn't code-signed. Either right-click it and choose *Open*, or clear the quarantine flag:

```
xattr -dr com.apple.quarantine tower-stack
```

**Warnings on startup about a "restricted method" or an "Unsupported JNI version"** — harmless
noise from Java 24 and newer. The game runs fine.

**No sound** — check the Sound toggle on the title screen. Audio is best-effort: if no device
is available the game carries on silently rather than refusing to start.
