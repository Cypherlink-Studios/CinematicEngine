---
title: Camera Dynamics and Rigs
description: In-depth technical guide on Dual camera mount architecture, PacketEvents packet-only camera, Display entity rigs, and dynamic LookAt targeting strategies.
sidebar:
  order: 5
---

# Camera Dynamics and Rigs

The camera system in CinematicEngine is designed to solve one of the oldest challenges in Minecraft development: **eliminating camera stutter, jitter, and network packet desync during automated movements**.

---

## 🎥 Dual Camera Mount Architecture

CinematicEngine features a polymorphic **Dual Camera Mount Architecture** managed by `CameraRigManager`:

```text
+--------------------------------------------------------------------------+
|                        DUAL CAMERA RIG STRATEGY                          |
+--------------------------------------------------------------------------+

                           [CameraRigManager]
                                   |
             +---------------------+---------------------+
             | mode = PACKET_VIRTUAL                     | mode = SERVER_DISPLAY
             v                                           v
  [PacketCameraRigSession]                     [DisplayCameraRigSession]
  - PacketEvents 2.14.0                        - Paper ItemDisplay in chunk
  - Virtual entity spawn packet                - player.setSpectatorTarget()
  - WrapperPlayServerCamera(virtualId)         - rig.teleport(loc)
  - WrapperPlayServerEntityTeleport            - Native client interpolation
  - Reset: WrapperPlayServerCamera(self)       - Restore: rig.remove()
```

### 1. `PACKET_VIRTUAL` (Prioritized Default)
- **Zero Server Entities**: Spawns a clientbound virtual camera entity directly to the viewer's network stream using `WrapperPlayServerSpawnEntity` with `EntityType.ITEM_DISPLAY`.
- **Spectator Camera Binding**: Sends `WrapperPlayServerCamera(virtualEntityId)` via PacketEvents 2.14.0. The Minecraft client locks its view to the virtual entity without any entity existing in the server world.
- **Sub-Tick Motion**: Each frame update emits `WrapperPlayServerEntityTeleport`, providing jitter-free motion without mutating or ticking server world chunks.
- **Clean Detach**: Upon completion or interruption, the engine dispatches `WrapperPlayServerCamera(player.getEntityId())` to snap the viewer's perspective back to their own body, followed by `WrapperPlayServerDestroyEntities`.

### 2. `SERVER_DISPLAY` (Fallback & Long-Range Streaming)
- **World Entity Rig**: Spawns an invisible Paper `ItemDisplay` into the world with `teleportDuration = 1` for client GPU transformation interpolation.
- **Cross-Chunk Streaming**: Because a physical entity resides in the server world, Paper handles chunk loading and streaming natively over large distances.
- **Automatic Fallback**: If PacketEvents is unavailable, `CameraRigManager` automatically falls back to `SERVER_DISPLAY`.

---

## ⚙️ Configuration

Configure the default mounting strategy in `plugins/CinematicEngine/config.yml`:

```yaml
camera:
  # Default mount mode: PACKET_VIRTUAL (recommended) or SERVER_DISPLAY
  mount-mode: PACKET_VIRTUAL
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

During a cinematic, the viewer is switched to spectator mode and attached to the rig. `SpectatorSafetyListener` enforces strict safeguards for both modes:

- **Dismount Prevention**: Intercepts sneak and stop-spectating events (`PlayerToggleSneakEvent`, `PlayerStopSpectatingEntityEvent`). If in `PACKET_VIRTUAL` mode, it automatically re-dispatches `WrapperPlayServerCamera` to keep the client locked.
- **State Serialization**: Captures original gamemode, flying status, coordinates, pitch, and yaw prior to scene start.
- **Emergency Clean-up**: If a player disconnects, dies, or the server stops, the engine safely restores the player's original state and destroys virtual or physical camera rigs without leaving orphan entities.
