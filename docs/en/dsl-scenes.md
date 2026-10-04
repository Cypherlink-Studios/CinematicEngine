---
title: DSL Scene Specification
description: Complete reference for the declarative YAML scene domain-specific language in CinematicEngine.
sidebar:
  order: 4
---

# DSL Scene Specification

CinematicEngine uses a declarative YAML-based **Domain-Specific Language (DSL)** to define cutscenes. The DSL is designed to be human-readable, modular, and strictly validated at load time by `SceneDtoValidator` and `SnakeYamlSceneParser`.

---

## 🏗️ Root Scene Structure

Every scene file represents a unified timeline sequence and can define actor staging, concurrent tracks, and metadata:

```yaml
id: "castle_siege_intro"
duration: 300 # Total sequence length in game ticks (20 ticks = 1s)

metadata:
  title: "Castle Siege Dramatic Intro"
  author: "BuildTeam"
  description: "Epic camera swoop over the castle walls with staged actors."

# Optional actor staging (virtual entities or clones)
actors:
  - id: "hero"
    type: "self_clone"
    initial_position: [120.0, 64.0, 50.0]
    initial_yaw: 0.0
    initial_equipment:
      main_hand: "DIAMOND_SWORD"

tracks:
  - type: "camera"
    # ... camera track configuration ...
  - type: "actor_motion"
    # ... continuous actor motion path ...
  - type: "actor_action"
    # ... poses, combat actions, and equipment ...
  - type: "effect"
    # ... synchronized particles and audio cues ...
```

### Top-Level Properties

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Yes** | Unique identifier for the scene (used in commands such as `/cine play <id>`). |
| `duration` | `integer` | **Yes** | Total duration of the scene in game ticks (must be positive, e.g. `200` = 10s). |
| `actors` | `list` | No | Declaration of staged actors (`virtual`, `self_clone`, `persistent`). |
| `metadata` | `map` | No | Arbitrary key-value metadata (e.g. `title`, `author`, `version`). |
| `tracks` | `list` | **Yes** | Collection of concurrent tracks executing on the unified timeline. |

---

## 🎭 Actor Staging Declaration (`actors`)

When a cinematic requires dynamic characters, they are declared in the `actors:` header:

```yaml
actors:
  - id: "villain"
    type: "virtual"                   # "virtual", "self_clone", or "persistent"
    skin: "DarkSorcerer"              # Mojang player name or skin UUID
    initial_position: [100.0, 64.0, 50.0]
    initial_yaw: 180.0
    initial_pitch: 0.0
    initial_equipment:
      main_hand: "NETHERITE_SWORD"
      helmet: "NETHERITE_HELMET"
```

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Yes** | Unique identifier for the actor in this scene. |
| `type` | `string` | No | `virtual` (default), `self_clone`, or `persistent`. |
| `skin` | `string` | No | Skin profile name or UUID for virtual actors. |
| `initial_position` | `[x, y, z]` | **Yes** | World coordinates where the actor spawns. |
| `initial_yaw` | `float` | No | Initial horizontal rotation in degrees (default `0.0`). |
| `initial_pitch` | `float` | No | Initial vertical pitch in degrees (default `0.0`). |
| `initial_equipment`| `map` | No | Initial equipment map (`main_hand`, `off_hand`, armor). |

---

## 🛤️ Supported Track Types (`tracks`)

CinematicEngine provides specialized tracks for each subsystem:

| Type (`type`) | Role | Primary Purpose |
| :--- | :--- | :--- |
| `camera` | Continuous | Camera movement and orientation (splines, rigs, and look-at). |
| `actor_motion` | Continuous | Actor position trajectories (LERP or splines with tangent heading). |
| `actor_action` | Discrete | Poses (`CROUCHING`), combat actions (`SWING`), item usage, and equipment swaps. |
| `effect` | Discrete | Bukkit particle emissions and positional sound effects. |
| `actor` | Continuous (Legacy) | Simple legacy single-track actor movement from earlier versions. |

---

## 🎯 Keyframe Specification

Keyframes dictate property values at specific ticks:

```yaml
keyframes:
  - tick: 0
    position: [100.0, 64.0, 100.0]
    yaw: -90.0
    pitch: 15.0
    fov: 70.0
    interpolation: "ease_in_out"
```

### Keyframe Fields by Track Type:

| Field | Type | Supported Tracks | Description |
| :--- | :--- | :--- | :--- |
| `tick` | `integer` | All | Timeline position in game ticks (`0 <= tick <= duration`). |
| `position` | `[x, y, z]` | `camera`, `actor_motion`, `actor` | World coordinates as a 3-element numeric list. |
| `yaw` / `pitch` | `float` | `camera`, `actor_motion`, `actor` | Orientation rotation angles in degrees. |
| `fov` | `float` | `camera` | Field of View in degrees (default `70.0`). |
| `interpolation`| `string` | `camera`, `actor_motion` | Transition curve (`linear`, `ease_in_out`). |
| `pose` | `string` | `actor_action` | Entity body pose (`STANDING`, `CROUCHING`, etc.). |
| `action` | `string` | `actor_action` | Combat animation (`SWING_MAIN_HAND`, `HURT`, etc.). |
| `item_usage` | `string` | `actor_action` | Usage stance (`BLOCKING`, `BOW_PULL`, etc.). |
| `equipment` | `map` | `actor_action` | Armor and hand slots modified at this tick. |
| `particle` | `map` | `effect` | Particle definition triggered at this tick. |
| `sound` | `map` | `effect` | Sound effect definition triggered at this tick. |

---

## 🛡️ Validation Rules (`SceneDtoValidator`)

When `/cine reload` or scene initialization runs, the engine applies rigorous validation rules:

1. **Actor Staging Consistency**:
   - Actor IDs declared under `actors:` must be unique.
   - Every actor must provide valid 3-element `initial_position` coordinates.
   - Tracks of type `actor_motion` and `actor_action` must define a valid `actor_id`.
2. **Chronological Consistency**:
   - `tick` must be `>= 0` and `<= duration`.
   - Keyframes are sorted by ascending `tick` automatically during mapping.
3. **Coordinate Validation**:
   - `position`, `initial_position`, and `target` vectors must be numeric lists containing exactly 3 values: `[x, y, z]`.
4. **Camera Track Requirements**:
   - A camera track requires at least 2 keyframes to define a path.
   - Accepts `look_at` / `look-at` modes: `fixed`, `static`, `actor`, or `forward`.
   - The `actor` mode requires `target_actor` (or `target-actor`).
