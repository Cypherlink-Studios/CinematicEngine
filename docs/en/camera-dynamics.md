---
title: Camera Dynamics and Rigs
description: In-depth technical guide on Display entity camera rigs, packet smoothing, and dynamic LookAt targeting strategies.
sidebar:
  order: 5
---

# Camera Dynamics and Rigs

The camera system in CinematicEngine is designed to solve one of the oldest challenges in Minecraft development: **eliminating camera stutter, jitter, and network packet desync during automated movements**.

---

## 🎥 The Display Entity Camera Rig

### The Problem with Direct Player Teleportation
Traditional Minecraft cutscene plugins teleport the viewer's `Player` entity tick-by-tick (`player.teleport()`). In Minecraft:
- The server runs at 20 ticks per second (1 tick = 50ms).
- Client monitors render at 60, 144, or 240 FPS.
- Direct player teleportation forces the client to snap between positions every 50ms, producing noticeable micro-stuttering, camera judder, and FOV warping.

### The Solution: Invisible Display Entity Rig
Introduced in Minecraft 1.19.4 / 1.20, **Display Entities** have native client-side transformation interpolation. CinematicEngine leverages this via `CameraRigManager` and `CameraRigSession`:

```text
+-------------------------------------------------------------------------+
|                       CAMERA RIG EXECUTION LIFECYCLE                    |
+-------------------------------------------------------------------------+

  1. RIG INITIALIZATION
     └── Spawns an invisible Display Entity at the scene's starting origin.
     └── Sets client transformation interpolation duration.

  2. SPECTATOR CAMERA MOUNT
     └── ProtocolLibBridge constructs a PacketType.Play.Server.CAMERA packet.
     └── Binds the viewer's client camera to the Display Entity ID.
     └── The player remains physically secure while visually attached to the rig.

  3. SUB-TICK SMOOTH MOVEMENT
     └── Server updates rig translation & rotation vectors on the tick timeline.
     └── The client GPU smoothly interpolates position between ticks at monitor FPS.

  4. RESTORATION & TEARDOWN
     └── Re-targets CAMERA packet back to the player's own entity ID.
     └── Safely removes the temporary Display Entity.
     └── Restores player gamemode, location, and inventory state.
+-------------------------------------------------------------------------+
```

---

## 🎯 LookAt Targeting Strategies

CinematicEngine separates **camera position** (where the camera flies) from **camera orientation** (where the camera points). This allows dynamic camera maneuvers like circling a monument while maintaining gaze on a fixed point.

### 1. Fixed Angles (`FixedAnglesStrategy`)
The camera points in the exact `yaw` and `pitch` directions specified in each keyframe.
```yaml
look-at:
  mode: "fixed"
```

### 2. Static Target / Point of Interest (`StaticTargetStrategy`)
The camera dynamically computes yaw and pitch at every frame to face a fixed 3D world coordinate:
```yaml
look-at:
  mode: "static" # or "point", "poi"
  target: [128.5, 64.0, -250.0]
```
*Ideal for*: Orbiting around a monument, zooming toward a chest, or panoramic sweeps.

### 3. Dynamic Actor Tracking (`ActorTargetStrategy`)
The camera tracks a moving entity (a player, NPC, or mob) continuously throughout the scene using `ActorPositionLookup`:
```yaml
look-at:
  mode: "actor"
  target-actor: "hero_npc" # Actor ID defined in actor track
```
*Ideal for*: Tracking an NPC during an action sequence or following a player riding a mount.

### 4. Forward Velocity Direction (`VelocityForwardStrategy`)
The camera automatically rotates to point in the direction of its instantaneous travel vector (derivative of the motion curve):
```yaml
look-at:
  mode: "forward" # or "velocity"
```
*Ideal for*: Rollercoaster-style flythroughs, drone shots, and flight paths.

---

## 🛡️ Spectator Safety and State Protection

During a cinematic, the viewer is switched to spectator mode and attached to the rig. `SpectatorSafetyListener` enforces strict safeguards:

- **Dismount Prevention**: Intercepts sneak/dismount packets so the player cannot accidentally detach from the camera rig.
- **Interaction Lockdown**: Blocks block breaking, placing, entity interaction, and command execution during playback.
- **State Serialization**: Saves gamemode, flying status, coordinates, pitch, yaw, and inventory prior to scene start.
- **Emergency Clean-up**: If a player disconnects, dies, or the server reloads, the listener automatically restores the player's state on rejoin and destroys orphan rigs.
