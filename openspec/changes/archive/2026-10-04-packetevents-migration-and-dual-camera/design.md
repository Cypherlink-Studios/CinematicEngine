# Design

## Context

See `proposal.md` for background and motivation. Currently, `cinematic-adapters` contains a minimal `ProtocolLibBridge`, stubbed packet methods in `VirtualPlayerActor`, and a single physical camera rig (`CameraRigManager`) that spawns Paper `ItemDisplay` entities into the server world.

## Goals / Non-Goals

**Goals:**
- Transition all packet-level logic from ProtocolLib to PacketEvents 2.14.0 (`com.github.retrooper:packetevents-spigot:2.14.0`).
- Treat PacketEvents as an external dependency provided by the server environment (`compileOnly` in Gradle, `depend: [packetevents]` in `plugin.yml`).
- Create `PacketEventsBridge` as the central gateway for packet availability checks and thread-safe packet dispatch to Bukkit `Player` instances and viewer collections.
- Implement complete virtual actor and self-clone packet lifecycle in `VirtualPlayerActor` using PacketEvents 2.14.0 wrappers, adhering to typed `EntityData<?>` generics.
- Implement dual camera mounting in `CameraRigManager`:
  - `PACKET_VIRTUAL`: Prioritized default using `WrapperPlayServerCamera` on a virtual entity without world mutation.
  - `SERVER_DISPLAY`: Fallback/alternative using world-spawned `ItemDisplay` for long-range cross-chunk tracking.
- Expose default camera mount mode in configuration and allow per-session selection.

**Non-Goals:**
- Shading or relocating PacketEvents inside the plugin JAR.
- Packet interception or incoming packet cancellation outside of spectator dismount prevention.
- Modifying math splines or runtime scheduler logic (which remain cleanly isolated in `cinematic-core` and `cinematic-runtime`).

## Decisions

### 1. External Dependency Model for PacketEvents 2.14.0
- **Decision**: Declare `packetevents-spigot` as `compileOnly` in `cinematic-adapters` and `plugin-bootstrap`, resolving from CodeMC repository (`https://repo.codemc.io/repository/maven-releases/`). Declare `depend: [packetevents]` in `plugin.yml`.
- **Rationale**: Keeps the plugin JAR lightweight, avoids shading/relocation overhead, and guarantees binary compatibility with other plugins using the shared PacketEvents runtime.
- **Alternatives Considered**: Shading with relocation (`shadowJar`). Rejected per user direction to keep it as an external plugin dependency.

### 2. PacketEvents Bridge (`PacketEventsBridge`)
- **Decision**: Introduce `PacketEventsBridge` in `com.darkbladedev.cinematic.adapters.packet` replacing `ProtocolLibBridge`. It wraps:
  - `isAvailable()`: Verifies PacketEvents API is loaded and enabled.
  - `sendPacket(Player, Object)`: Sends a packet wrapper to a single Bukkit player.
  - `broadcastPacket(Iterable<Player>, Object)`: Dispatches a packet to all viewers in the collection.
- **Rationale**: Isolates PacketEvents API calls from domain classes, making unit testing and mock verification simple.

### 3. Virtual Actor Pipeline & Strongly-Typed EntityData
- **Decision**: In `VirtualPlayerActor`, implement the packet lifecycle:
  - **Spawn**: `WrapperPlayServerPlayerInfoUpdate` with `Action.ADD_PLAYER` and `Action.UPDATE_LISTED` (listed: false), attaching `UserProfile` with textures from `SkinData`; followed by `WrapperPlayServerSpawnEntity` (`EntityType.PLAYER`), initial `WrapperPlayServerEntityHeadLook`, and `WrapperPlayServerEntityMetadata`.
  - **Metadata & Pose**: PacketEvents 2.14.0 enforces generic typing on `EntityData<?>`. We construct `List<EntityData<?>>` using `EntityDataTypes.BYTE` (flags, skin model parts) and `EntityDataTypes.ENTITY_POSE` (mapped from `ActorPose`).
  - **Motion**: For continuous playback, compute delta. If delta $\le 8$ blocks and not instant, dispatch `WrapperPlayServerEntityRelativeMoveAndRotation`; otherwise dispatch `WrapperPlayServerEntityTeleport`.
  - **Actions & Equipment**: Dispatch `WrapperPlayServerEntityAnimation` for actions and `WrapperPlayServerEntityEquipment` for slots.
  - **Despawn**: Dispatch `WrapperPlayServerDestroyEntities` and `WrapperPlayServerPlayerInfoUpdate` with `Action.REMOVE_PLAYER`.

### 4. Dual Camera Mount Architecture
- **Decision**: Introduce `CameraMountMode` enum (`PACKET_VIRTUAL`, `SERVER_DISPLAY`) and refactor `CameraRigSession` into an interface/hierarchy:
  - `PacketCameraRigSession`: Spawns a virtual packet-only entity (e.g. `ItemDisplay` or `Marker` packet) to the viewer, sends `WrapperPlayServerCamera(virtualEntityId)`, sends `WrapperPlayServerEntityTeleport` on updates, and on termination sends `WrapperPlayServerCamera(player.getEntityId())` followed by `WrapperPlayServerDestroyEntities`.
  - `DisplayCameraRigSession`: Preserves existing physical `ItemDisplay` spawning with client interpolation duration and Bukkit spectator target.
  - `CameraRigManager`: Configured with default mode (`PACKET_VIRTUAL` by default) while allowing runtime mode selection.

```
+--------------------------------------------------------------------------+
|                        DUAL CAMERA RIG STRATEGY                          |
+--------------------------------------------------------------------------+

                           [CameraRigManager]
                                   |
             +---------------------+---------------------+
             | mode = PACKET_VIRTUAL                     | mode = SERVER_DISPLAY
             v                                           v
  [PacketCameraRigSession]                     [DisplayCameraRigSession]
  - Virtual entity spawn packet                - Paper ItemDisplay in chunk
  - WrapperPlayServerCamera                    - player.setSpectatorTarget()
  - WrapperPlayServerEntityTeleport            - rig.teleport(loc)
  - Restore: SetCamera(self)                   - Restore: rig.remove()
```

## Risks / Trade-offs

- **[Risk: Server missing PacketEvents plugin]** → **Mitigation**: `plugin.yml` declares `depend: [packetevents]`. During startup, `CinematicEnginePlugin` checks `PacketEventsBridge.isAvailable()` and disables cleanly with a human-readable error log if absent.
- **[Risk: View distance / chunk void in packet-only camera]** → **Mitigation**: Packet-only mode is ideal for local/medium range cutscenes. Dual-mode architecture enables scenes that traverse huge distances to use `SERVER_DISPLAY` so Paper handles chunk streaming natively.
- **[Risk: PacketEvents 2.14.0 generic type variance]** → **Mitigation**: Use explicit wildcard generic typing `List<EntityData<?>>` in metadata builders to avoid compiler mismatch warnings and errors.
