# Proposal

## Why

CinematicEngine currently relies on an initial skeleton bridge for ProtocolLib, which is a legacy packet interception layer with notable overhead and known threading incompatibilities on modern multi-threaded Paper forks (such as Folia). Furthermore, actor packet methods (`VirtualPlayerActor`) are currently stubs, and camera mounting is strictly tied to physical world Display Entities (`ItemDisplay`). Migrating to PacketEvents 2.14.0 provides a modern, high-performance, strongly-typed packet foundation with official Folia support, unlocks complete packet-level virtual actor rendering (spawning, skin layers, animations, equipment, and sub-tick movement), and enables a zero-world-mutation Packet-Only camera rig (`WrapperPlayServerCamera`) while preserving dual support for server-side Display Entities.

## What Changes

- **PacketEvents 2.14.0 Integration**: Replace ProtocolLib dependency with PacketEvents 2.14.0 as an external plugin dependency (`packetevents-spigot` in Gradle compileOnly, repository at CodeMC, and `depend: [packetevents]` in `plugin.yml`).
- **PacketEvents Bridge**: Replace `ProtocolLibBridge` with a thread-safe `PacketEventsBridge` providing unified packet dispatching and availability checking for scene viewers.
- **Complete `VirtualPlayerActor` Pipeline**: Implement full clientbound packet lifecycle for virtual actors and viewer self-clones using PacketEvents 2.14.0 wrappers (`WrapperPlayServerPlayerInfoUpdate` with non-listed status, `WrapperPlayServerSpawnEntity`, strongly-typed `WrapperPlayServerEntityMetadata` with `List<EntityData<?>>`, `WrapperPlayServerEntityHeadLook`, `WrapperPlayServerEntityEquipment`, and delta/teleport movement packets).
- **Dual Camera Mount Architecture (Priority Packet-Only)**:
  - **`PACKET_VIRTUAL` (Default/Prioritized)**: Mounts the client spectator camera directly to a packet-only virtual entity via `WrapperPlayServerCamera` and updates position via `WrapperPlayServerEntityTeleport`, creating zero server world entities and zero chunk ticking overhead.
  - **`SERVER_DISPLAY` (Fallback/Alternative)**: Retains the existing Paper `ItemDisplay` spectator rig with client-side interpolation duration for scenarios requiring native server-side chunk streaming over massive distances.
- **Documentation & Metadata Alignment**: Update `docs/metadata.yml` and documentation guides to reflect PacketEvents 2.14.0 dependency and dual camera rig capabilities.

## Capabilities

### New Capabilities
<!-- No new isolated capability root required; behavior evolves existing camera-rig and actor-staging specs -->

### Modified Capabilities
- `camera-rig`: Specify dual camera mount strategies (`PACKET_VIRTUAL` as prioritized default via PacketEvents `WrapperPlayServerCamera`, and `SERVER_DISPLAY` via Paper Display Entities) with configurable selection and complete detachment cleanup.
- `actor-staging`: Formalize packet transport and lifecycle implementation using PacketEvents 2.14.0 wrappers for virtual players, self-clones, unlisted tab entries, typed `EntityData<?>` metadata, and equipment/animation dispatch.

## Impact

- **Dependencies**: ProtocolLib 5.3.0 is removed; PacketEvents 2.14.0 (`com.github.retrooper:packetevents-spigot:2.14.0`) added via CodeMC repository.
- **Plugin Descriptor**: `plugin-bootstrap/src/main/resources/plugin.yml` updates `softdepend: [ProtocolLib]` to `depend: [packetevents]`.
- **Runtime & Adapters**: `ProtocolLibBridge` replaced by `PacketEventsBridge`; `VirtualPlayerActor` stubs replaced by live PacketEvents calls; `CameraRigManager` refactored into a dual-mode manager supporting `PacketCameraRigSession` and `DisplayCameraRigSession`.
- **Compatibility**: Requires PacketEvents 2.14.0 installed on the Paper/Purpur server.
