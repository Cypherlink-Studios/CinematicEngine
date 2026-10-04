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

Every scene file represents a single timeline sequence and must contain the following top-level keys:

```yaml
id: "castle_siege_intro"
duration: 300 # Total sequence length in game ticks (20 ticks = 1s)

metadata:
  title: "Castle Siege Dramatic Intro"
  author: "BuildTeam"
  description: "Epic camera swoop over the castle walls ending at the throne room."

tracks:
  - type: "camera"
    # ... camera track configuration ...
  - type: "actor"
    # ... actor track configuration ...
  - type: "effect"
    # ... effect track configuration ...
```

### Top-Level Properties

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Yes** | Unique identifier for the scene (used in commands such as `/cine play <id>`). |
| `duration` | `integer` | **Yes** | Total duration of the scene in game ticks (must be positive, e.g. `200` = 10s). |
| `metadata` | `map` | No | Arbitrary key-value metadata (e.g. `title`, `author`, `version`). |
| `tracks` | `list` | **Yes** | Collection of concurrent tracks executing on the unified timeline. |

---

## 🛤️ Track Structure

A track represents an independent timeline channel controlling a specific subsystem (camera, actors, or environmental effects).

```yaml
tracks:
  - type: "camera"
    id: "main_camera"
    data:
      path-mode: "spline"
      look-at:
        mode: "static"
        target: [120.5, 75.0, -45.0]
    keyframes:
      - tick: 0
        position: [150.0, 90.0, -10.0]
        fov: 70.0
        interpolation: "ease_in_out"
      - tick: 150
        position: [130.0, 80.0, -30.0]
        fov: 85.0
        interpolation: "ease_in_out"
      - tick: 300
        position: [115.0, 75.0, -42.0]
        fov: 70.0
        interpolation: "linear"
```

### Common Track Properties

- `type` (`string`): The registered track factory type (`camera`, `actor`, `effect`).
- `id` (`string`, optional): Identifier for the track (defaults to `<type>_track`).
- `data` (`map`, optional): Configuration parameters specific to the track type.
- `keyframes` (`list`): List of chronological control points defining changes over time.

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

### Keyframe Fields

| Field | Type | Track Types | Description |
| :--- | :--- | :--- | :--- |
| `tick` | `integer` | All | Timeline position in game ticks (`0 <= tick <= duration`). |
| `position` | `[x, y, z]` | `camera`, `actor` | World coordinates as a 3-element numeric list. |
| `yaw` | `float` | `camera`, `actor` | Horizontal rotation angle in degrees (-180 to 180). |
| `pitch` | `float` | `camera`, `actor` | Vertical rotation angle in degrees (-90 to 90). |
| `fov` | `float` | `camera` | Field of View in degrees (default `70.0`). |
| `interpolation` | `string` | `camera`, `actor` | Transition function from previous keyframe (`linear`, `ease_in_out`). |
| `particle` | `map` | `effect` | Particle definition triggered at this tick. |
| `sound` | `map` | `effect` | Sound effect definition triggered at this tick. |

---

## 🛡️ Validation Rules (`SceneDtoValidator`)

When `/cine reload` or scene initialization runs, the engine applies rigorous validation rules:

1. **Chronological Consistency**:
   - `tick` must be `>= 0` and `<= duration`.
   - Keyframes are sorted by ascending `tick` during mapping.
2. **Coordinate Validation**:
   - `position` and `target` vectors must be numeric lists containing exactly 3 values: `[x, y, z]`.
3. **At Least One Track**:
   - A scene must contain at least one valid track.
4. **Camera Track Requirements**:
   - A camera track requires at least 2 keyframes to define a path.
5. **Path Mode Validation**:
   - `path-mode` accepts `linear` or `spline`. Invalid entries default safely to `linear`.
6. **LookAt Validation**:
   - `look-at.mode` accepts `fixed`, `static`, `actor`, or `forward`.
   - `mode: static` requires a valid `target: [x, y, z]` vector.
   - `mode: actor` requires `target-actor` (or `actorId`).
