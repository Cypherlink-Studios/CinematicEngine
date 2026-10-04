---
title: Actors and Environmental Effects
description: Actor staging, multi-layer motion and action tracks, poses, animations, particles, and spatial audio in CinematicEngine.
sidebar:
  order: 7
---

# Actors and Environmental Effects

CinematicEngine incorporates a comprehensive **actor staging** subsystem and **environmental effects** (synchronized particles and audio cues) operating in seamless concert with camera tracks.

---

## 🎭 Actor Staging (`actors`)

Starting in CinematicEngine 1.0, scenes can declare an ensemble cast under the `actors:` header in YAML. Actor staging is orchestrated by `SceneActorSession`, which spawns simulated entities purely via clientbound packets (using ProtocolLib) to avoid polluting server world chunks with orphan entities.

```yaml
id: "night_ambush"
duration: 120

actors:
  - id: "hero"
    type: "self_clone"
    initial_position: [120.0, 64.0, 50.0]
    initial_yaw: 0.0
    initial_equipment:
      main_hand: "DIAMOND_SWORD"
      helmet: "IRON_HELMET"

  - id: "assassin"
    type: "virtual"
    skin: "ShadowRogue"
    initial_position: [140.0, 68.0, 65.0]
    initial_yaw: 180.0
    initial_equipment:
      main_hand: "BOW"
```

### Supported Actor Types

| Type (`type`) | Implementation | Description |
| :--- | :--- | :--- |
| `virtual` | `VirtualPlayerActor` | Packet-based virtual player entity. Supports Mojang player skins resolved and cached locally (`SkinCacheService`). |
| `self_clone` | `VirtualPlayerActor` | Dynamically clones the appearance, skin textures, current armor, and held items of the primary viewing player. |
| `persistent` | `NPCActor` / `PlayerActor` | Binds to an existing physical server entity or NPC (by UUID or name) without despawning it when the scene ends. |

---

## 🧩 Capability-Based Contract (`Actor`)

To prevent brittle, monolithic entity hierarchies, actors expose modular capabilities:

- **`Movable` (`asMovable()`)**: Controls continuous teleportation, velocity, orientation (`yaw`/`pitch`), and 3D spatial positioning.
- **`Animatable` (`asAnimatable()`)**: Controls entity poses (`ActorPose`), combat action swings/hits (`ActorAction`), and item usage states (`ItemUsageState`).
- **`Equippable` (`asEquippable()`)**: Controls real-time weapon and armor slot equipment (`EquipmentSlot`).

If a track evaluates an action for a capability an actor does not support (e.g. equipping armor on an entity that is not equippable), the track gracefully skips that modification without crashing or aborting the scene.

---

## 🛤️ Multi-Layer Actor Tracks

To allow characters to walk smoothly while ducking, guarding with shields, or swapping weapons at dramatic moments, CinematicEngine separates actor control into two independent track layers:

### 1. Continuous Motion Track (`actor_motion`)

Evaluates smooth spatial trajectories across keyframes using linear interpolation or Catmull-Rom splines.

```yaml
tracks:
  - type: "actor_motion"
    id: "hero_movement"
    data:
      actor_id: "hero"
      path_mode: "spline"      # "spline" or "linear"
      heading: "tangent"        # "tangent" computes body yaw from the motion velocity tangent
    keyframes:
      - tick: 0
        position: [120.0, 64.0, 50.0]
      - tick: 60
        position: [130.0, 64.0, 58.0]
      - tick: 120
        position: [140.0, 64.0, 65.0]
```

#### `actor_motion` Properties:
- `actor_id` (`string`, required): Actor identifier matching an entry in `actors:`.
- `path_mode` (`string`): Interpolation mode (`spline` for smooth Catmull-Rom curves, `linear` for straight paths).
- `heading` (`string` or `follow_path: true`): When set to `tangent`, entity body yaw automatically aligns with the tangent direction of travel.

---

### 2. Discrete Action & Pose Track (`actor_action`)

Dispatches instantaneous pose modifications, combat animations, item usage stances, and equipment swaps at specified timeline ticks without disrupting the continuous spatial motion path.

```yaml
tracks:
  - type: "actor_action"
    id: "hero_actions"
    data:
      actor_id: "hero"
    keyframes:
      - tick: 0
        pose: "STANDING"
      - tick: 45
        pose: "CROUCHING"
        item_usage: "BLOCKING"
      - tick: 70
        action: "HURT"
      - tick: 85
        action: "SWING_MAIN_HAND"
        equipment:
          main_hand: "NETHERITE_SWORD"
```

#### Available States and Animations:

| Category | Property | Supported Values |
| :--- | :--- | :--- |
| **Poses** | `pose:` | `STANDING`, `CROUCHING`, `SWIMMING`, `SLEEPING`, `FALL_FLYING`, `SPIN_ATTACK` |
| **Animations** | `action:` | `SWING_MAIN_HAND`, `SWING_OFF_HAND`, `HURT`, `CRITICAL_HIT`, `MAGIC_CRITICAL_HIT` |
| **Item Usage** | `item_usage:` | `NONE`, `BLOCKING`, `BOW_PULL`, `CROSSBOW_CHARGE`, `EATING`, `DRINKING`, `SPEAR_CHARGE` |
| **Equipment** | `equipment:` | Map with keys: `main_hand`, `off_hand`, `helmet`, `chestplate`, `leggings`, `boots` |

---

## ✨ Environmental Effects Track (`effect`)

The `effect` track triggers discrete audio cues and particle emissions synchronized to timeline ticks:

### 1. Particle Effects
```yaml
tracks:
  - type: "effect"
    id: "magic_particles"
    keyframes:
      - tick: 45
        particle:
          type: "ENCHANTMENT_TABLE"
          count: 50
          offset: [1.0, 1.5, 1.0]
          speed: 0.2
      - tick: 70
        particle:
          type: "EXPLOSION_LARGE"
          count: 1
          offset: [0.0, 0.0, 0.0]
```

### 2. Positional Sound Effects
```yaml
tracks:
  - type: "effect"
    id: "audio_ambience"
    keyframes:
      - tick: 0
        sound:
          name: "music.credits"
          volume: 0.8
          pitch: 1.0
      - tick: 70
        sound:
          name: "entity.player.attack.crit"
          volume: 1.0
          pitch: 1.1
```

---

## 🎬 Integrated Choreography Example

The following scene demonstrates complete synchronization: a self-clone actor travels along a Catmull-Rom spline with automatic tangent heading, the camera dynamically locks onto and tracks the actor (`look-at: mode: actor`), and discrete actions crouch and equip the hero right on cue:

```yaml
id: "cinematic_duel"
duration: 80

actors:
  - id: "warrior"
    type: "self_clone"
    initial_position: [0.0, 64.0, 0.0]
    initial_yaw: 0.0
    initial_equipment:
      main_hand: "IRON_SWORD"

tracks:
  # 1. Camera smoothly orbiting while tracking the warrior
  - type: "camera"
    id: "orbit_cam"
    data:
      path_mode: "spline"
      look_at:
        mode: "actor"
        target_actor: "warrior"
    keyframes:
      - tick: 0
        position: [-10.0, 68.0, 0.0]
      - tick: 40
        position: [-8.0, 67.0, 15.0]
      - tick: 80
        position: [-5.0, 66.0, 30.0]

  # 2. Smooth spline motion with automatic tangent heading
  - type: "actor_motion"
    id: "warrior_path"
    data:
      actor_id: "warrior"
      path_mode: "spline"
      heading: "tangent"
    keyframes:
      - tick: 0
        position: [0.0, 64.0, 0.0]
      - tick: 40
        position: [5.0, 64.0, 15.0]
      - tick: 80
        position: [12.0, 64.0, 30.0]

  # 3. Discrete expressive actions and equipment swap
  - type: "actor_action"
    id: "warrior_actions"
    data:
      actor_id: "warrior"
    keyframes:
      - tick: 0
        pose: "STANDING"
      - tick: 35
        pose: "CROUCHING"
        item_usage: "BLOCKING"
      - tick: 60
        action: "SWING_MAIN_HAND"
        equipment:
          main_hand: "DIAMOND_SWORD"
```
