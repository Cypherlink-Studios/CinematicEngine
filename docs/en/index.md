---
title: Introduction to CinematicEngine
description: High-performance cinematic and dynamic camera sequencing engine for Minecraft Paper and Purpur servers.
sidebar:
  order: 1
---

# Introduction to CinematicEngine

Welcome to the official documentation for **CinematicEngine**, an advanced, modular cinematic and dynamic camera sequencing engine engineered for **Paper** and **Purpur** Minecraft servers (1.20+ and 1.21.x).

CinematicEngine empowers server administrators and developers to choreograph Hollywood-grade cutscenes, architectural flythroughs, boss battle introductions, and immersive narrative experiences directly in vanilla-compatible Minecraft without requiring client-side modifications.

---

## ⚡ Architecture Overview

CinematicEngine is built with a strictly decoupled multi-module architecture:

```text
+-----------------------------------------------------------------------------+
|                          CINEMATICENGINE ARCHITECTURE                       |
+-----------------------------------------------------------------------------+
|                                                                             |
|  [PRESENTATION LAYER]    /cinematic & /cine commands                        |
|                          Dynamic tab completion & admin controls            |
|                          DemoCinematicOrchestrator session management       |
|                                     │                                       |
|                                     ▼                                       |
|  [DSL & PARSING]         Declarative YAML scenes (plugins/CinematicEngine/) |
|                          SnakeYamlSceneParser & SceneDtoValidator           |
|                          TrackRegistry & InterpolatorRegistry mapping       |
|                                     │                                       |
|                                     ▼                                       |
|  [CAMERA & DYNAMICS]     CameraTrack & CameraFrame                          |
|                          LookAt Targeting (Fixed, Static POI, Actor, Forward)|
|                          Catmull-Rom Spline & Linear interpolation          |
|                                     │                                       |
|                                     ▼                                       |
|  [ADAPTERS & SAFETY]     Dual Camera Rig (PACKET_VIRTUAL & SERVER_DISPLAY)  |
|                          PacketEventsBridge (WrapperPlayServerCamera)       |
|                          SpectatorSafetyListener (State & Inventory restore)|
|                                     │                                       |
|                                     ▼                                       |
|  [CORE & RUNTIME]        TimelinePlayer & BukkitTickScheduler               |
|                          ActorTrack & EffectTrack (Particles & Audio)       |
|                          TimelineContext & Keyframe timeline execution      |
|                                                                             |
+-----------------------------------------------------------------------------+
```

---

## 🚀 Key Capabilities

### 1. Dual Camera Mount Architecture & PacketEvents 2.14.0
Traditional Minecraft cutscene plugins teleport players directly every tick, resulting in visible camera jitter, stuttering motion, and network desync. CinematicEngine features a dual mounting system:
- **`PACKET_VIRTUAL` (Default)**: Employs PacketEvents 2.14.0 `WrapperPlayServerCamera` to lock the spectator's client view to a clientbound virtual entity with zero server-side entities.
- **`SERVER_DISPLAY`**: Uses world-spawned Paper `ItemDisplay` entities with client GPU transformation interpolation for long-distance cross-chunk streaming.

### 2. Catmull-Rom Spline Curve Interpolation
Linear camera motion can feel robotic and abrupt at sharp corners. CinematicEngine features native **Catmull-Rom cubic spline interpolation**, calculating smooth continuous velocity tangents across 3D control points so the camera swoops gracefully through intricate spaces.

### 3. Dynamic LookAt Targeting Engine
Decouple camera position from camera focus:
- **Fixed Angles**: Explicit yaw and pitch control per keyframe.
- **Static Target (POI)**: Locks the camera's gaze onto a fixed block coordinate or point of interest while the rig flies around it.
- **Actor Target**: Continuously tracks moving players or NPCs in real time.
- **Velocity Forward**: Automatically aligns the camera along its instantaneous direction of travel.

### 4. Declarative YAML Scene DSL
Author complex cinematics effortlessly using human-readable YAML scene files. Define duration, keyframe positions, custom FOV, transitions, particle bursts, and spatial sound triggers with full schema validation and helpful error reporting.

### 5. Multi-Track Timeline Sequencing
Synchronize camera movement with world events on a unified timeline:
- **Camera Tracks**: Position, rotation, FOV, and path modes.
- **Actor Tracks**: Coordinates, animations, and movement for participating players and NPCs.
- **Effect Tracks**: Spatial sound effects and particle bursts precisely timed to tick offsets.

### 6. Comprehensive Spectator Safety & State Protection
The engine protects viewers throughout their cinematic experience:
- Automatically saves player gamemode, flying state, location, and inventory.
- Prevents player inputs, dismounts, or spectator interaction during cutscenes.
- Guarantees complete player state restoration upon scene completion, manual cancellation, or server disconnect.

---

## 📖 Navigating the Documentation

- **[Getting Started](/en/docs/cinematicengine/getting-started/)**: Installation prerequisites, PacketEvents setup, and building your first cutscene.
- **[Commands & Permissions](/en/docs/cinematicengine/commands-and-permissions/)**: Full reference for `/cinematic` (`/cine`) commands and permission nodes.
- **[DSL Scenes](/en/docs/cinematicengine/dsl-scenes/)**: YAML scene specification, structure, metadata, and syntax rules.
- **[Camera Dynamics](/en/docs/cinematicengine/camera-dynamics/)**: Dual camera rigs, PacketEvents spectator packets, and LookAt modes.
- **[Tracks & Interpolations](/en/docs/cinematicengine/tracks-and-interpolations/)**: Keyframes, splines, ease curves, and multi-track timelines.
- **[Actors & Effects](/en/docs/cinematicengine/actors-and-effects/)**: Controlling entities, spawning particle effects, and spatial audio.
- **[Developer API](/en/docs/cinematicengine/developer-api/)**: Java API, `CinematicService`, custom tracks, and runtime extensions.
