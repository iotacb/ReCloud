# Aether Swarm

Aether Swarm is a top-down arena-survival demo for Cloud 3. Its Java sources live
under `src/demos/arena_survivor`, alongside the repository's other demos, while
its runtime assets live under `demo_assets/arena_survivor`.

## Game loop

- Survive finite enemy waves in a 2200 × 1400 arena while the camera follows you.
- Automatic weapons acquire targets in range. Every owned weapon occupies one of
  eight visible slots around the player.
- Defeated enemies drop scrap directly into the run economy.
- Clearing a wave opens the Aether Exchange. Buy weapons, permanent stat upgrades,
  healing, or reroll the three offers before deploying the next wave.
- Every fifth wave ends with a Rift Baron boss.

The enemy roster includes basic Gloops, fast Razor Rats, ranged Hex Slingers,
spiraling Wisps, telegraphed charging Iron Husks, and the volley-firing Rift Baron.
The weapon roster includes the Pulse Carbine, Scatter Wand, Rail Needle, Frost
Orb, and short-range Rift Blade.

## Controls

| Input | Action |
| --- | --- |
| `WASD` or arrow keys | Move |
| `Space` | Phase dash with brief invulnerability |
| `Esc` or `P` | Pause/resume |
| `Enter` | Deploy from the shop / restart after defeat |
| `R` | Restart after defeat |
| Mouse | Buy, reroll, deploy, or restart through the UI |

Weapons aim and fire automatically.

## Build and run

Requirements: JDK 25, Maven, and the matching LWJGL native architecture. Build
from the repository root; the root Maven configuration already adds `src/demos`
to the source path:

```sh
mvn clean package
```

Run `arena_survivor.Main` from the repository root. On macOS, add
`-XstartOnFirstThread`; on current JDKs, also add
`--enable-native-access=ALL-UNNAMED`. The Maven profiles select the matching LWJGL
native classifier. Keeping the repository root as the working directory lets the
demo resolve `demo_assets/arena_survivor`.

## Presentation

The demo uses Cloud's retained Canvas UI, batched sprites, pooled particle systems,
bloom, color grading, vignette, engine-native 2D lighting, bounded camera follow,
and three custom fullscreen shader passes. Dynamic player, weapon, projectile, and
enemy aura lights illuminate the arena while enemy occluders cast moving soft
shadows. The persistent atmosphere pass adds reactive aether flow, subtle
scanlines, film grain, chromatic edge separation, a player aura, boss coloration,
and low-health danger pulsing. The kinetic pass supplies directional dash smears,
damage glitches, rift vortices, and wave refraction. The impact pass adds radial
shockwave distortion and emissive rings. World-space shock rings, camera trauma,
hit flashes, dash trails, and randomized-pitch audio layer on top of them.

## Assets and licenses

- Tiny Dungeon sprites by Kenney, CC0 1.0:
  https://kenney.nl/assets/tiny-dungeon
- Digital Audio by Kenney, CC0 1.0:
  https://kenney.nl/assets/digital-audio
- Impact Sounds by Kenney, CC0 1.0:
  https://kenney.nl/assets/impact-sounds
- UI Audio by Kenney, CC0 1.0:
  https://kenney.nl/assets/ui-audio
- Press Start 2P from Google Fonts, SIL Open Font License 1.1:
  https://github.com/google/fonts/tree/main/ofl/pressstart2p

The original license texts are bundled under
`demo_assets/arena_survivor/licenses/`.
