---
title: Actors and Environmental Effects
description: Choreographing players, NPCs, fake entities, particle bursts, and spatial audio in CinematicEngine.
sidebar:
  order: 7
---

# Actors and Environmental Effects

CinematicEngine goes beyond camera movement by providing dedicated tracks for **actors** (characters in the scene) and **environmental effects** (audio-visual cues, particles, and sounds).

---

## 🎭 The Actor System

An **Actor** represents an entity participating in a cinematic sequence whose position, orientation, and animations are driven by an `ActorTrack`.

### Actor Types

| Actor Class | Type | Description |
| :--- | :--- | :--- |
| `PlayerActor` | Real Player | Controls the spectator or third-party players present in the world. |
| `FakeEntityActor` | Packet Entity | Lightweight virtual entity spawned via packets for client-only scenes. |
| `NPCActor` | Server Entity / NPC | Persistent server entity or NPC (compatible with Citizens / custom NPCs). |

### DSL Actor Track Example

```yaml
tracks:
  - type: "actor"
    id: "guard_patrol"
    data:
      actor-id: "guard_patrol"
    keyframes:
      - tick: 0
        position: [100.0, 64.0, 50.0]
        yaw: 0.0
        pitch: 0.0
        interpolation: "linear"
      - tick: 80
        position: [100.0, 64.0, 70.0]
        yaw: 0.0
        pitch: 0.0
        interpolation: "linear"
      - tick: 100
        position: [100.0, 64.0, 70.0]
        yaw: 90.0
        pitch: 10.0
        interpolation: "ease_in_out"
```

---

## ✨ Environmental Effects Track (`EffectTrack`)

The `effect` track executes discrete audio and particle events synchronized to timeline ticks. Unlike continuous tracks, effects trigger at specific tick thresholds.

### 1. Particle Effects (`ParticleEffect`)
Spawns Minecraft particles in the world relative to camera or scene coordinates:

```yaml
tracks:
  - type: "effect"
    id: "magic_sparkles"
    keyframes:
      - tick: 40
        particle:
          type: "ENCHANTMENT_TABLE"
          count: 50
          offset: [1.0, 1.5, 1.0]
          speed: 0.2
      - tick: 120
        particle:
          type: "EXPLOSION_LARGE"
          count: 1
          offset: [0.0, 0.0, 0.0]
```

#### Particle Configuration Parameters:
- `type` (`string`): Bukkit `Particle` enum name (e.g. `FIREWORK`, `FLAME`, `SOUL_FIRE_FLAME`, `CAMPFIRE_SIGNAL_SMOKE`).
- `count` (`integer`): Number of particle particles emitted.
- `offset` (`[dx, dy, dz]`): Spatial dispersion around the spawn center.
- `speed` (`float`, optional): Initial particle emission velocity.

---

### 2. Positional Sound Effects (`SoundEffect`)
Plays audio cues with adjustable pitch and volume to enhance narrative pacing:

```yaml
tracks:
  - type: "effect"
    id: "soundtrack"
    keyframes:
      - tick: 0
        sound:
          name: "music.credits"
          volume: 0.8
          pitch: 1.0
      - tick: 120
        sound:
          name: "entity.generic.explode"
          volume: 1.0
          pitch: 0.9
```

#### Sound Configuration Parameters:
- `name` (`string`): Sound key or resource path (e.g. `entity.ender_dragon.growl`, `ui.toast.challenge_complete`, `music.dragon`).
- `volume` (`float`): Playback volume multiplier (`0.0` to `1.0+`).
- `pitch` (`float`): Pitch frequency (`0.5` deep to `2.0` high).

---

## 🎬 Synchronized Choreography Example

By orchestrating camera, actor, and effect tracks together, you can create cohesive cinematic moments:

```yaml
id: "summoning_ritual"
duration: 100

tracks:
  # Camera circles the altar
  - type: "camera"
    id: "cam"
    data:
      path-mode: "spline"
      look-at:
        mode: "static"
        target: [0.5, 65.0, 0.5]
    keyframes:
      - tick: 0
        position: [10.0, 68.0, 0.0]
      - tick: 50
        position: [0.0, 70.0, 10.0]
      - tick: 100
        position: [-10.0, 68.0, 0.0]

  # Actor performs ritual
  - type: "actor"
    id: "wizard"
    keyframes:
      - tick: 0
        position: [0.5, 64.0, 0.5]
        yaw: 180.0
        pitch: -20.0

  # Climax particle and sound at tick 50
  - type: "effect"
    id: "fx"
    keyframes:
      - tick: 50
        particle:
          type: "DRAGON_BREATH"
          count: 100
          offset: [0.5, 1.0, 0.5]
        sound:
          name: "entity.wither.spawn"
          volume: 1.0
          pitch: 1.2
```
