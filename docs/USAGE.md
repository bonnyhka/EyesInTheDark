# Eyes in the Dark

NeoForge mod for Minecraft 1.21.1. It creates a server-controlled horror apparition for one player at a time. The apparition is not a mob, has no hitbox, cannot be killed, cannot kill the player, and is rendered client-side as glowing eyes and a broken smile in the dark.

## Commands

All commands require permission level 2.

```mcfunction
/eyesinthedark spawn
/eyesinthedark spawn <targets>
/eyesinthedark spawn <targets> <natural|idle|stare|flee|vanish>
/eyesinthedark force
/eyesinthedark force <targets>
/eyesinthedark blink
/eyesinthedark blink <targets>
/eyesinthedark clear
/eyesinthedark clear <targets>
/eyesinthedark chance
/eyesinthedark chance <0.20..1.0>
/eyesinthedark enabled
/eyesinthedark enabled <true|false>
/eyesinthedark interval
/eyesinthedark interval <200..24000>
/eyesinthedark cooldown
/eyesinthedark cooldown <200..72000>
/eyesinthedark status
```

`spawn` appears behind the selected player. `force` immediately starts the stare approach. `blink` makes the face jump through the edge of a player's vision before fleeing. `clear` removes the client effect. `interval`, `cooldown`, and `status` give an operator live control over automatic spawns.

Natural spawns are enabled by default. The server checks survival players underground every 2400 ticks (two minutes); qualifying dark cave checks have a 20% chance to start an encounter. Administrators can turn this automation on or off with `enabled`, and inspect or change its chance with `chance`. The configuration file is created at `config/eyesinthedark-common.toml` after the server starts. By default the apparition starts 8-14 blocks away and the stare sequence never brings it closer than 7.5 blocks. The default timings are synchronized with the included sound files, so appearance, idle sounds, stare, fleeing, and vanishing do not overlap.

## Custom sounds

Until real files are added, the mod uses vanilla Minecraft placeholder sounds. To replace them, put OGG files here:

```text
src/main/resources/assets/eyesinthedark/sounds/ambient_one.ogg
src/main/resources/assets/eyesinthedark/sounds/ambient_two.ogg
src/main/resources/assets/eyesinthedark/sounds/appear.ogg
src/main/resources/assets/eyesinthedark/sounds/stare.ogg
src/main/resources/assets/eyesinthedark/sounds/vanish.ogg
src/main/resources/assets/eyesinthedark/sounds/flee.ogg
```

The included files map as follows: `появился.mp3` -> `appear.ogg`, `стоит1.mp3` -> `ambient_one.ogg`, `стоит2.mp3` -> `ambient_two.ogg`, `приближается.mp3` -> `stare.ogg`, `пропал.mp3` -> `vanish.ogg`, and `убежал((.mp3` -> `flee.ogg`.
