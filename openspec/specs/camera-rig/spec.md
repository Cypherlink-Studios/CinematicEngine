# camera-rig Specification

## Purpose
Provides isolated, client-interpolated camera attachment using Paper Display Entities and spectator mode for smooth, judder-free cinematic playback.

## Requirements

### Requirement: Native Display Entity Rig Mounting
The system SHALL mount viewers to a dedicated camera rig entity in spectator mode configured with sub-tick interpolation.

#### Scenario: Viewer joins cinematic playback
- **WHEN** cinematic playback begins for a viewer
- **THEN** the system saves the viewer's current location and gamemode, changes the viewer's gamemode to spectator, and targets a dedicated camera rig entity with teleport duration set for client interpolation.

#### Scenario: Private entity visibility
- **WHEN** a camera rig entity is spawned for a viewer
- **THEN** the entity SHALL be hidden from all other players on the server and visible only to the active viewer.

### Requirement: Viewer State Preservation and Restoration
The system SHALL restore the viewer to their exact pre-cinematic state upon completion, interruption, or disconnect.

#### Scenario: Cinematic completes normally
- **WHEN** a cinematic playback reaches its end tick
- **THEN** the spectator target is detached, the camera rig entity is removed from the world, and the viewer is restored to their initial location, gamemode, and flight state.

#### Scenario: Playback stopped or cancelled prematurely
- **WHEN** a cinematic playback is stopped, cancelled, or reloaded
- **THEN** all bound viewers are immediately detached, rig entities despawned, and original player states restored.

#### Scenario: Viewer disconnects during playback
- **WHEN** a viewer disconnects while attached to an active camera rig
- **THEN** the camera rig entity is despawned and viewer tracking state is cleaned up to prevent entity leaks.

### Requirement: Spectator Dismount Prevention
The system SHALL prevent viewers from detaching from the spectator camera rig before playback finishes.

#### Scenario: Viewer attempts to sneak
- **WHEN** a viewer attempts to sneak or detach from the spectator target during playback
- **THEN** the detachment action is cancelled or immediately retargeted to maintain continuous camera lock.
