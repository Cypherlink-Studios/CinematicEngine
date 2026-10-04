# Spec Delta

## MODIFIED Requirements

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
