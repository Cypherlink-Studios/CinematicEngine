# Tasks

## 1. Dependency & Build Configuration

- [x] 1.1 Add CodeMC repository to root `build.gradle.kts` and configure `packetevents = "2.14.0"` / `packetevents-spigot` in `gradle/libs.versions.toml`, removing ProtocolLib. Verify dependency resolution with `./gradlew dependencies`.
- [x] 1.2 Update `cinematic-adapters/build.gradle.kts` and `plugin-bootstrap/build.gradle.kts` replacing `protocollib` with `packetevents-spigot` as `compileOnly`. Verify compilation with `./gradlew compileJava`.
- [x] 1.3 Update `plugin-bootstrap/src/main/resources/plugin.yml` replacing `softdepend: [ProtocolLib]` with `depend: [packetevents]`. Verify descriptor configuration.

## 2. PacketEvents Bridge Layer

- [x] 2.1 Implement `PacketEventsBridge` in `com.darkbladedev.cinematic.adapters.packet` replacing `ProtocolLibBridge`, providing availability checking, player packet dispatch, and broadcast packet dispatch. Verify with unit tests.
- [x] 2.2 Update references across adapters and bootstrap (`CinematicEnginePlugin`, `DemoCinematicOrchestrator`, `SceneActorSession`) from `ProtocolLibBridge` to `PacketEventsBridge`. Verify with `./gradlew test`.

## 3. VirtualPlayerActor Packet Pipeline

- [x] 3.1 Implement virtual player spawn packets (`WrapperPlayServerPlayerInfoUpdate` with unlisted status + `WrapperPlayServerSpawnEntity` + head rotation) in `VirtualPlayerActor`. Verify with packet generation tests.
- [x] 3.2 Implement strongly-typed entity metadata packet generation (`WrapperPlayServerEntityMetadata` with `List<EntityData<?>>`) handling skin model parts, flags, and poses in `VirtualPlayerActor`. Verify with metadata unit tests.
- [x] 3.3 Implement continuous relative motion (`WrapperPlayServerEntityRelativeMoveAndRotation`), absolute teleportation (`WrapperPlayServerEntityTeleport`), actions (`WrapperPlayServerEntityAnimation`), and equipment (`WrapperPlayServerEntityEquipment`). Verify with actor motion tests.
- [x] 3.4 Implement despawn and tab cleanup packets (`WrapperPlayServerDestroyEntities` + `WrapperPlayServerPlayerInfoUpdate` remove). Verify complete despawn lifecycle.

## 4. Dual Camera Mount Architecture

- [x] 4.1 Define `CameraMountMode` enum (`PACKET_VIRTUAL`, `SERVER_DISPLAY`) and refactor `CameraRigSession` to support polymorphic session types (`PacketCameraRigSession` and `DisplayCameraRigSession`). Verify interface structure.
- [x] 4.2 Implement `PacketCameraRigSession` using `WrapperPlayServerSpawnEntity` for a private virtual entity, `WrapperPlayServerCamera` to mount the viewer, `WrapperPlayServerEntityTeleport` for sub-tick updates, and restore logic detaching camera to the player entity. Verify with packet camera unit tests.
- [x] 4.3 Update `CameraRigManager` to support configurable dual-mounting strategy defaulting to `PACKET_VIRTUAL`, while retaining `SERVER_DISPLAY` for long-distance streaming. Verify dual-mode switching tests.
- [x] 4.4 Expose camera mounting mode in plugin config/orchestrator and add spectator dismount protection listeners for both modes. Verify spectator safety tests.

## 5. Documentation, Metadata & Verification

- [x] 5.1 Update `docs/metadata.yml` removing ProtocolLib dependency and documenting PacketEvents 2.14.0 requirement and dual-camera capabilities.
- [x] 5.2 Update multilingual documentation (`camera-dynamics.md`, `getting-started.md`) across `docs/en` and `docs/es` reflecting PacketEvents 2.14.0 and dual camera mount modes.
- [x] 5.3 Run full test suite, code coverage verification, and OpenSpec validation (`./gradlew test`, `openspec validate`). Verify all tests pass cleanly.
