# Proposal: Camera Dynamics and Packet Smoothing

## Why

Currently, `PlayerCameraOutput` relies on raw `player.teleport()` calls evaluated once per server tick (20 TPS / 50ms intervals). Because Minecraft client display loops run at 60–144+ FPS without sub-tick teleport interpolation, viewers experience severe camera stutter, judder, and network teleport confirmation friction. Furthermore, `CameraTrack` only provides piecewise linear interpolation between waypoints—yielding unnatural, sharp trajectory corners—and lacks dynamic gaze targeting (Look-At) and decouple-safe camera rigging.

Formalizing this change provides high-fidelity, cinema-grade camera movement by leveraging native Paper 1.20.6 Display Entity interpolation (`teleport_duration`), $C^1$-continuous Catmull-Rom splines, and flexible Look-At orientation strategies.

## What Changes

- **Spectator Camera Rig (`cinematic-adapters`)**: Implement an isolated spectator camera rig using private Paper 1.20.6 Display Entities (`BlockDisplay`/`ItemDisplay`) with `setTeleportDuration(1)`. Viewers enter spectator mode and spectate the rig entity, achieving native client-side sub-tick interpolation at their display's full refresh rate (60–144+ Hz).
- **Spectator Safety & Lifecycle Management**: Add automatic viewer state preservation and restoration (previous gamemode, location, flight state), alongside dismount/sneak cancellation and clean teardown upon cutscene completion, pause, stop, or player disconnect.
- **Catmull-Rom Splines (`cinematic-core`)**: Introduce centripetal Catmull-Rom spline trajectory computation for smooth, loop-free 3D camera paths passing smoothly through waypoints without velocity discontinuities.
- **Look-At Targeting Strategies (`cinematic-camera`)**: Add support for camera orientation modes:
  - Fixed angles (manual yaw/pitch).
  - Static target (focusing on a world coordinate).
  - Dynamic actor tracking (dynamically aiming at a scene actor's position).
  - Velocity heading (automatically facing forward along the trajectory tangent).
- **Decoupled FOV Handling**: Keep FOV within the data model while decoupling it from intrusive client-side modifications on vanilla viewers.
- **DSL Support (`cinematic-dsl`)**: Extend `CameraTrackDTO` and YAML mappers to parse spline pathing modes and look-at target definitions.

## Capabilities

### New Capabilities
- `camera-rig`: Isolated spectator camera mount using Paper 1.20.6 Display Entities with client-side sub-tick interpolation (`teleport_duration`), private per-viewer visibility, and robust player state restoration.
- `spline-interpolation`: Continuous $C^1$ trajectory interpolation using centripetal Catmull-Rom curves across keyframes in 3D space.
- `look-at-targeting`: Dynamic and static camera orientation calculation (fixed, static point of interest, actor tracking, and velocity forward).

### Modified Capabilities
<!-- None: Initial capability specifications for the project -->

## Impact

- **`cinematic-core`**: New spline interfaces and centripetal Catmull-Rom spline implementation.
- **`cinematic-camera`**: Enhanced `CameraTrack` evaluation supporting splines and `LookAtTarget` strategies.
- **`cinematic-adapters`**: New `SpectatorCameraRig` / `DisplayEntityCameraOutput`, event listeners for sneak prevention and disconnect cleanup.
- **`cinematic-dsl`**: Expanded DTOs and schema mappers to deserialize spline tracks and look-at blocks.
- **Dependencies**: Uses existing Paper 1.20.6 API (display entities, per-player visibility) and JOML 1.10.8 math.
