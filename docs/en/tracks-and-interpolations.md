---
title: Tracks, Keyframes and Interpolation
description: Comprehensive guide to timeline tracks, keyframe positioning, linear/easing interpolation, and Catmull-Rom splines.
sidebar:
  order: 6
---

# Tracks, Keyframes and Interpolation

At the core of CinematicEngine lies a deterministic timeline evaluation engine. Each scene consists of one or more **tracks** running in parallel, with properties evaluated across discrete **keyframes**.

---

## 🛤️ Supported Track Types

| Track Type | Factory | Primary Output | Typical Use Cases |
| :--- | :--- | :--- | :--- |
| `camera` | `CameraTrackFactory` | `CameraFrame` (pos, yaw, pitch, fov) | Directing viewer viewpoint and trajectory. |
| `actor` | `ActorTrackFactory` | `ActorFrame` (pos, yaw, pitch, action) | Directing actors, NPCs, and fake entities. |
| `effect` | `EffectTrackFactory` | `EffectFrame` (particles, sounds) | Timed audio-visual accents, explosions, ambient music. |

---

## ⏱️ Keyframe Mechanics

A keyframe specifies the state of a track at a discrete moment in time (measured in integer game ticks). Between keyframes, the runtime calculates transitional states using mathematical interpolation functions.

```yaml
keyframes:
  - tick: 0
    position: [0.0, 64.0, 0.0]
    interpolation: "ease_in_out"
  - tick: 60
    position: [20.0, 75.0, 10.0]
    interpolation: "linear"
```

- When evaluating tick `30` (halfway), the engine identifies surrounding keyframes (`tick 0` and `tick 60`), calculates normalized time $t = \frac{30 - 0}{60 - 0} = 0.5$, and applies the specified interpolator.

---

## 📈 Interpolation Algorithms

### 1. Linear Interpolation (`LinearInterpolator`)
Interpolates linearly from start to finish at constant velocity:
$$f(t) = t$$

```yaml
interpolation: "linear"
```
*Best for*: Constant-speed sweeps, machine movements, or mechanical paths.

### 2. Ease-In-Out Interpolation (`EaseInOutInterpolator`)
Accelerates smoothly at the beginning and decelerates gently toward the target using a smoothstep polynomial curve:
$$f(t) = t^2 (3 - 2t)$$

```yaml
interpolation: "ease_in_out"
```
*Best for*: Natural camera movements, cinematic transitions, starting and stopping shots.

---

## 🌀 Catmull-Rom Spline Curve Interpolation

While linear interpolation creates straight paths between keyframes, it results in harsh corners and sudden directional jolts at waypoints.

CinematicEngine solves this with **Catmull-Rom Cubic Spline interpolation** (`CatmullRomSpline`):

```yaml
data:
  path-mode: "spline"
```

```text
Linear Path (Harsh Corners):
[P0] ───────────────> [P1] ╲
                           ╲
                            ╲
                             ▼
                            [P2] ───────────────> [P3]

Catmull-Rom Spline (Silky-Smooth Curve):
[P0] ╭────────────────[P1]
      ╲                  ╲
       ╲                  ╰───────[P2]────────────╮
        ╰──────────────────────────────────────────╯ [P3]
```

### Mathematical Formulation
For any segment between points $P_1$ and $P_2$, the spline utilizes the preceding point $P_0$ and subsequent point $P_3$ to construct continuous velocity tangents:

$$P(t) = \frac{1}{2} \left[ 2P_1 + (P_2 - P_0)t + (2P_0 - 5P_1 + 4P_2 - P_3)t^2 + (-P_0 + 3P_1 - 3P_2 + P_3)t^3 \right]$$

### Key Benefits:
- **$C^1$ Tangent Continuity**: Zero angular snap; transitions through waypoints are completely continuous.
- **Passes Through Control Points**: Unlike B-splines or Bézier curves where points act as invisible pull magnets, Catmull-Rom splines pass directly through each keyframe coordinate.
- **Dynamic Endpoint Handling**: The engine automatically mirrors boundary points at start and end frames to maintain smooth entry and exit vectors.
