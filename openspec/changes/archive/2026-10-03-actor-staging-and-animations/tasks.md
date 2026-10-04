# Tasks

## 1. Domain Model & Capability Interfaces (`cinematic-actors`)

- [x] 1.1 Define capability interfaces (`Movable`, `Animatable`, `Equippable`) and core enums (`ActorPose`, `ActorAction`, `ItemUsageState`, `EquipmentSlot`), and verify unit tests in `cinematic-actors`.
- [x] 1.2 Refactor `Actor` to provide default capability getters (`asMovable()`, `asAnimatable()`, `asEquippable()`), update `PlayerActor`, and verify that `./gradlew :cinematic-actors:test` compiles and passes.

## 2. DSL Data Models, Parsers & Validation (`cinematic-dsl`)

- [x] 2.1 Add `ActorDTO` and support for `actors:` declaration in `SceneDTO` and `SnakeYamlSceneParser`, and verify parser tests with YAML test fixtures.
- [x] 2.2 Implement `ActorMotionTrackFactory`, `ActorActionTrackFactory`, and their validators in `cinematic-dsl`, verifying factory mapping tests.
- [x] 2.3 Extend `SceneDtoValidator` to validate the `actors:` block (unique IDs, valid coordinates, required fields) and verify validation failure scenarios in `SceneDtoValidatorTest`.

## 3. Track Runtime Evaluators (`cinematic-dsl` & `cinematic-core`)

- [x] 3.1 Implement `ActorMotionTrack` supporting continuous LERP/Spline spatial interpolation and velocity-forward tangent heading (`heading: "tangent"` / `follow_path: true`), and verify with motion tests.
- [x] 3.2 Implement `ActorActionTrack` supporting discrete tick dispatch for poses, animations, item usages, and equipment changes without altering motion, and verify with action tests.

## 4. Packet-Level Adapters & Staging Orchestration (`cinematic-adapters`)

- [x] 4.1 Implement `VirtualPlayerActor` using ProtocolLib packets (`PlayerInfoUpdate`, `AddEntity`, `SetEntityData`, `SetEquipment`, `Animate`, `RemoveEntities`) with safe despawn, and verify packet generation tests.
- [x] 4.2 Implement `SkinCacheService` for resolving and locally caching Mojang player skins, and verify unit tests with cache hits and misses.
- [x] 4.3 Implement `SceneActorSession` managing the staging lifecycle for `virtual`, `self_clone`, and `persistent` actors with guaranteed cleanup on scene completion or cancellation, and verify session tests.

## 5. Bootstrap Integration & End-to-End Verification (`plugin-bootstrap` & `cinematic-testing`)

- [x] 5.1 Wire `SceneActorSession` into `DemoCinematicOrchestrator`, providing `ActorResolver` to `TimelineContext`, and register disconnect cleanup hooks in `PlayerQuitListener`, verifying orchestrator tests.
- [x] 5.2 Create end-to-end integration tests in `cinematic-testing` staging a full scene with camera tracking, actor motion, and action dispatch, and verify that `./gradlew check` passes completely.
