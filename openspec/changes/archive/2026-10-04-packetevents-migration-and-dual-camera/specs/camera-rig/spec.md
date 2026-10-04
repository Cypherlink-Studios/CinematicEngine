# Spec Delta

## ADDED Requirements

### Requirement: Packet-Only Camera Rig Mounting
The system SHALL support mounting viewers to a virtual packet-only camera entity using PacketEvents `WrapperPlayServerCamera` without spawning server-side world entities, serving as the prioritized default camera mount strategy.

#### Scenario: Mount viewer to packet camera
- **WHEN** cinematic playback begins for a viewer with packet-only camera mode active
- **THEN** the system puts the viewer into spectator mode, dispatches a clientbound virtual camera entity spawn packet, and dispatches a set camera packet binding the viewer's viewpoint to the virtual entity.

#### Scenario: Sub-tick packet camera motion update
- **WHEN** the cinematic updates the camera transform on a playback tick in packet-only mode
- **THEN** the system dispatches clientbound entity teleport packets for the virtual camera entity without mutating or ticking server world entities.

## MODIFIED Requirements

### Requirement: Viewer State Preservation and Restoration
The system SHALL restore the viewer to their exact pre-cinematic state upon completion, interruption, or disconnect, cleaning up all active camera mounts regardless of whether the mount mode is packet-only or display-entity based.

#### Scenario: Cinematic completes normally
- **WHEN** a cinematic playback reaches its end tick
- **THEN** the spectator camera target is detached, the camera rig entity is despawned (via packet or world removal according to mount mode), the viewer's camera is reset to their own entity, and the viewer is restored to their initial location, gamemode, and flight state.

#### Scenario: Playback stopped or cancelled prematurely
- **WHEN** a cinematic playback is stopped, cancelled, or reloaded
- **THEN** all bound viewers are immediately detached, camera packets or rig entities cleaned up, and original player states restored.

#### Scenario: Viewer disconnects during playback
- **WHEN** a viewer disconnects while attached to an active camera rig
- **THEN** the camera rig session is cleaned up and all tracking state is cleared to prevent resource leaks.
