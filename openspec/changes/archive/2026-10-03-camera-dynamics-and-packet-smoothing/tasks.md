# Tasks

## 1. Core Spline Trajectories (cinematic-core)

- [x] 1.1 Implement `CatmullRomSpline` using centripetal parameterization ($\alpha = 0.5$) with endpoint phantom handling and verify via JUnit tests in `cinematic-testing` or `cinematic-core`.
- [x] 1.2 Implement numerical resiliency for duplicate or zero-distance adjacent keyframes in `CatmullRomSpline` and verify with unit tests.

## 2. Camera Orientation & Targeting (cinematic-camera)

- [x] 2.1 Define `LookAtStrategy` interface and implement `FixedAnglesStrategy` and `StaticTargetStrategy` for world coordinate aiming, verified with orientation calculation unit tests.
- [x] 2.2 Implement `ActorTargetStrategy` (dynamic tracking of scene actors) and `VelocityForwardStrategy` (tangent heading alignment) in `cinematic-camera`, verified with unit tests.
- [x] 2.3 Refactor `CameraTrack` to support both spline position evaluation and `LookAtStrategy` orientation, verified with test suite updates in `cinematic-testing`.

## 3. Paper Display Entity Spectator Rig (cinematic-adapters)

- [x] 3.1 Implement `CameraRigSession` tracking original player location, gamemode, flight state, and bound Display Entity in `cinematic-adapters`.
- [x] 3.2 Implement `DisplayEntityCameraRig` using Paper `ItemDisplay`/`BlockDisplay` with `teleport_duration = 1`, private viewer visibility (`setVisibleByDefault(false)`, `player.showEntity`), and spectator mounting.
- [x] 3.3 Implement `SpectatorSafetyListener` to prevent voluntary sneak dismounts and clean up sessions on player disconnect.
- [x] 3.4 Wire `DisplayEntityCameraRig` into camera output execution and add plugin disable cleanup hooks to prevent orphan entities or stranded spectators.

## 4. DSL & Serialization (cinematic-dsl)

- [x] 4.1 Update `CameraTrackDTO` and `TrackDTO` to support `pathMode` (e.g., `linear`, `catmull-rom`) and `lookAt` configuration blocks (mode, target coordinates, actor ID).
- [x] 4.2 Update `CameraTrackFactory` in `cinematic-dsl` to construct `CameraTrack` with configured spline and `LookAtStrategy`, verified with DSL mapper unit tests.

## 5. End-to-End Verification & Validation

- [x] 5.1 Add end-to-end simulation tests in `cinematic-testing` verifying complete spline camera playback, look-at targeting, and rig session transitions.
- [x] 5.2 Run `./gradlew test` and verify that all test suites across all modules pass cleanly.
- [x] 5.3 Validate the OpenSpec change with `openspec validate camera-dynamics-and-packet-smoothing`.
