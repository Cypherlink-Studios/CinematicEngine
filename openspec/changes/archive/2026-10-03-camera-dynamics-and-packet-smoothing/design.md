# Design: Camera Dynamics and Packet Smoothing

## Context

See [proposal.md](file:///c:/Users/antua/OneDrive/Documentos/Programming/JAVA/PLUGINS/CinematicEngine/openspec/changes/camera-dynamics-and-packet-smoothing/proposal.md) for the core motivation. Currently, `PlayerCameraOutput` applies camera positions by calling `player.teleport()` every server tick, causing visual micro-stutter on clients with 60–144+ Hz monitors. `CameraTrack` performs piecewise linear interpolation between keyframes without rotational target tracking or continuous derivative continuity.

## Goals / Non-Goals

**Goals:**
- Provide butter-smooth (60–144+ FPS) camera movement by leveraging Paper 1.20.6 Display Entities with `teleport_duration` and spectator targeting.
- Implement centripetal Catmull-Rom spline pathing ($C^1$ continuity) in `cinematic-core` passing through all keyframe waypoints.
- Support flexible camera gaze targeting: static coordinate focus, real-time actor tracking, velocity forward heading, and manual angles.
- Ensure safe spectator lifecycle handling: save/restore gamemode and position, prevent dismount on sneak, and clean up on disconnect or plugin reload.
- Extend `cinematic-dsl` to declare spline path modes and look-at targets in YAML/JSON scenes.

**Non-Goals:**
- Vanilla FOV attribute hacking via movement speed modification (FOV remains decoupled in the data model for future or modded use).
- Third-party client mod integrations (maintain 100% pure vanilla/Paper compatibility).
- Camera roll/tilt emulation (vanilla spectator protocol does not support z-axis rotation).

## Decisions

### 1. Paper 1.20.6 Display Entity Rig over ArmorStands or Pure ProtocolLib Packets
- **Choice**: Spawn a dedicated, invisible `ItemDisplay` or `BlockDisplay` entity per spectator with `setTeleportDuration(1)`. Hide it globally using `entity.setVisibleByDefault(false)` and reveal it only to the viewer with `player.showEntity(plugin, entity)`. Mount the player using `player.setGameMode(GameMode.SPECTATOR)` and `player.setSpectatorTarget(cameraEntity)`.
- **Rationale**: Paper's Display Entities have client-side linear interpolation natively wired into their network sync (`teleport_duration`). The client OpenGL loop smoothly glides between server ticks without requiring manual packet hacks.
- **Alternatives Considered**:
  - *ArmorStand Rig*: Lacks explicit `teleport_duration` control; interpolation can jitter over fluctuating ping.
  - *ProtocolLib Virtual Packet Entities*: Avoids spawning server entities, but requires extensive low-level NMS/packet maintenance across Minecraft protocol bumps.

### 2. Centripetal Catmull-Rom Spline ($\alpha = 0.5$) over Bezier or Uniform Spline
- **Choice**: Implement `CatmullRomSpline` using the centripetal parameterization ($\alpha = 0.5$).
- **Rationale**: A Catmull-Rom curve passes directly through all keyframes (unlike Bezier curves, which require off-path control handles). The centripetal formulation prevents loop cusps and overshoot oscillations common in uniform Catmull-Rom splines when waypoints have uneven distances.
- **Alternatives Considered**:
  - *Cubic Bezier*: High authoring friction without a 3D visual editor.
  - *Uniform Catmull-Rom*: Prone to unwanted loops and extreme overshoots on tight turns.

### 3. Strategy Pattern for Camera Orientation (`LookAtStrategy`)
- **Choice**: Separate position evaluation from view orientation:
  - `FixedAnglesStrategy`: Uses explicit keyframe `(yaw, pitch)`.
  - `StaticTargetStrategy`: Calculates directional vector toward a fixed `Vector3d`.
  - `ActorTargetStrategy`: Dynamically queries the targeted actor's spatial position from the scene context at the current tick.
  - `VelocityForwardStrategy`: Aligns heading with the normalized trajectory velocity tangent vector $\vec{v} = \frac{d\vec{p}}{dt}$.
- **Rationale**: Keeps `CameraTrack` modular and allows any positioning mode (linear or spline) to pair with any orientation mode.

### 4. Robust Viewer Session Management (`CameraRigSession`)
- **Choice**: Maintain active viewer state records storing previous location, previous gamemode, and bound rig entity.
  - On cutscene start: Snapshot state, switch to spectator, bind target.
  - On tick: Evaluate spline & orientation, move rig entity.
  - On sneak: Listen to `PlayerToggleSneakEvent` and prevent detachment during playback.
  - On stop/finish/disconnect/disable: Remove rig entity, unbind spectator target, restore snapshot state.
- **Rationale**: Prevents players from getting stranded in spectator mode or losing their original survival coordinates if a cutscene terminates unexpectedly.

## Risks / Trade-offs

- **[Risk] Spectator Shift Detach**: Player presses sneak (`Shift`) to leave the spectator target.
  - *Mitigation*: Intercept `PlayerToggleSneakEvent` (or schedule a next-tick retargeting if client predicts detach) so camera lock is maintained.
- **[Risk] Server Restart / Plugin Reload**: Server shuts down or reloads during a cutscene.
  - *Mitigation*: Register an explicit `onDisable` cleanup routine in the plugin bootstrap to restore all active sessions and despawn entities cleanly.
- **[Risk] Target Actor Missing or Despawned**: A camera set to track an actor finds no active actor entity.
  - *Mitigation*: `ActorTargetStrategy` falls back gracefully to the camera's last known orientation or keyframe orientation without throwing an exception.
