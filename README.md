# Eyes in the Dark

Horror-mod for Minecraft 1.21.1 on NeoForge. It adds no hostile mob or entity: the encounter is a client-side apparition made of glowing eyes and a broken smile. It cannot be killed, has no hitbox, does not deal damage, and never comes close enough to touch the player.

## What it does

- Automatically appears for survival players in dark underground caves.
- Spawns 8-14 blocks away, usually behind the player.
- Watches from peripheral vision, then vanishes, flees, blinks between the edges of the screen, or starts a forced stare sequence.
- Keeps the apparition at least 7.5 blocks away during the stare sequence. When a player gets within 6 blocks, it retreats or disappears.
- Uses synchronized custom OGG sounds for appearing, waiting, staring, running away, and vanishing.
- Adds a VHS effect, vignette, screen tearing, brief camera tremor, and afterimages at the strongest moment of the stare sound.
- Contains an operator-controlled `blink` scare mode that jumps through a player's peripheral vision before fleeing.

Natural appearances are enabled by default. The server checks every 2400 ticks (two minutes) for a survival player below Y=48, without access to the sky, at raw light level 5 or lower. Each valid check has a 20% chance to start an encounter.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.233` or newer within the Minecraft 1.21.1 line
- Install the mod JAR on both the dedicated server and every client that connects to it.

## Installation

1. Put `eyesinthedark-1.0.0.jar` into the `mods` folder on the server and clients.
2. Start the server once to create `config/eyesinthedark-common.toml`.
3. Adjust automatic encounter settings in that configuration file when needed.

## Operator Commands

All commands require permission level 2.

| Command | Description |
| --- | --- |
| `/eyesinthedark spawn [targets] [mode]` | Spawns the apparition behind one or more players. |
| `/eyesinthedark force [targets]` | Starts the full stare sequence immediately. |
| `/eyesinthedark blink [targets]` | Runs the peripheral blink scare and then flees. |
| `/eyesinthedark clear [targets]` | Clears the active apparition effect. |
| `/eyesinthedark chance [0.20..1.0]` | Shows or changes the automatic spawn chance. |
| `/eyesinthedark enabled [true\|false]` | Shows or toggles automatic appearances. |
| `/eyesinthedark interval [200..24000]` | Shows or changes the automatic check interval in ticks. |
| `/eyesinthedark cooldown [200..72000]` | Shows or changes the cooldown after an encounter in ticks. |
| `/eyesinthedark status` | Shows automatic-spawn settings and active encounter count. |

Available modes for `spawn`: `natural`, `idle`, `stare`, `flee`, `vanish`, and `blink`.

## Sound Design

The bundled sounds are OGG Vorbis resources and are timed to prevent overlap:

- `appear.ogg`: 3.74 seconds
- `ambient_one.ogg`: 8.86 seconds
- `ambient_two.ogg`: 6.16 seconds
- `stare.ogg`: 9.27 seconds
- `flee.ogg`: 6.50 seconds
- `vanish.ogg`: 3.27 seconds

## Building

```powershell
.\gradlew.bat build
```

The assembled mod JAR is written to `build/libs/eyesinthedark-1.0.0.jar`.
