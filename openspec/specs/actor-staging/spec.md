# actor-staging Specification

## Purpose
Provides declaration, packet-level lifecycle management, and session orchestration for virtual actors, spectator self-clones, and persistent world entities in cinematic sequences.

## Requirements

### Requirement: Scene Cast Declaration
Scene definitions SHALL support an `actors` declaration list in the scene header specifying actor identifiers, actor types (`virtual`, `self_clone`, `persistent`), initial 3D positions, initial orientations (yaw/pitch), and optional initial equipment or skin profiles. The scene validator SHALL reject scenes containing duplicate actor identifiers or missing required initial transform data.

#### Scenario: Valid scene with virtual actor and viewer self-clone
- **WHEN** a scene is parsed containing an `actors` block with a `virtual` actor and a `self_clone` actor with valid initial coordinates
- **THEN** the parser successfully maps the cast definition and validation passes

#### Scenario: Reject duplicate actor identifiers
- **WHEN** a scene defines two actors with the same `id` attribute
- **THEN** validation fails with a descriptive error identifying the duplicate actor ID

### Requirement: Capability-Based Actor Contract
Actors SHALL expose modular capabilities (`asMovable()`, `asAnimatable()`, `asEquippable()`) rather than requiring all actor implementations to fulfill every action. Tracks interacting with an actor SHALL query the relevant capability and gracefully skip evaluation if that capability is not supported by the resolved actor.

#### Scenario: Actor exposes motion and animation capabilities
- **WHEN** a track evaluates against an actor implementing `Movable` and `Animatable`
- **THEN** both `asMovable()` and `asAnimatable()` return present optionals allowing position and pose updates

#### Scenario: Unsupported capability handled gracefully
- **WHEN** an action track attempts to equip an item on an actor whose `asEquippable()` is empty
- **THEN** the action evaluation skips equipment modification without throwing an exception or aborting the scene

### Requirement: Packet-Level Virtual Actor and Self-Clone Lifecycle
The system SHALL spawn and manage virtual actors and spectator self-clones exclusively via clientbound entity packets using PacketEvents 2.14.0 sent directly to scene viewers, ensuring no phantom entities are created in the server world. The system SHALL manage tab-list profile entries with unlisted status, dispatch strongly-typed entity metadata, stream delta and teleport transform updates, broadcast action animations and equipment slots, and guarantee complete entity cleanup upon scene completion, cancellation, or viewer disconnect.

#### Scenario: Self-clone matches viewer profile and equipment
- **WHEN** a cinematic starts with a `self_clone` actor declared and a player viewer registered
- **THEN** a virtual player entity is spawned for the viewer replicating the player's skin textures, current armor, and held items via PacketEvents wrappers with unlisted tab-list status.

#### Scenario: Real-time action animation and equipment updates
- **WHEN** an action track triggers an actor animation or equipment change on a virtual actor
- **THEN** the system dispatches corresponding PacketEvents animation and equipment packets directly to the scene viewers.

#### Scenario: Complete despawn on scene termination or player disconnect
- **WHEN** a running cinematic finishes, is stopped via command, or the viewing player disconnects
- **THEN** clientbound entity destroy and player info removal packets are dispatched immediately and all session resources are cleared.
