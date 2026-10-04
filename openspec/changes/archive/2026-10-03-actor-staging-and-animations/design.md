# Design

## Context

See `proposal.md` for motivation and background.

Currently, `cinematic-actors` provides minimal implementations (`PlayerActor`, `FakeEntityActor`, `NPCActor`) implementing an `Actor` interface that only exposes `teleport()`, `rotate()`, and `isValid()`. In `cinematic-dsl`, `ActorTrack` only handles basic linear position and target look-at, and requires `ActorResolver` from `TimelineContext`, which `DemoCinematicOrchestrator` does not supply.

To bring cinematics to life, we need a complete staging and animation architecture spanning DSL definitions, capability-based domain models, and packet-level network dispatch via ProtocolLib.

## Goals / Non-Goals

**Goals:**
- **Modular Multi-Track Architecture**: Separate continuous spatial interpolation (`actor_motion`) from discrete dramatic events (`actor_action`).
- **Capability-Based Actor Model**: Replace monolithic actor methods with clean component capabilities (`Movable`, `Animatable`, `Equippable`).
- **Packet-Level Virtual Staging**: Spawn virtual actors and spectator self-clones exclusively via clientbound packets with zero world pollution and guaranteed cleanup.
- **Velocity-Forward Tangent Heading**: Allow actors to automatically orient their body along the movement trajectory without manual yaw/pitch keyframing on every curve.
- **DSL & Orchestrator Integration**: Parse and validate `actors:`, map `actor_motion` and `actor_action`, and orchestrate `SceneActorSession` inside `DemoCinematicOrchestrator`.

**Non-Goals:**
- External skeletal model engines (e.g., ModelEngine, ItemsAdder custom rigs) are deferred to future plugins/adapters.
- Server-side physics or navigation pathfinding; actors follow exact spline and keyframe paths.
- Interactive in-game timeline scrubbing tools.

## Decisions

### 1. Multi-Layer Track Architecture (Option B)
- **Decision**: Separate actor tracks into `actor_motion` (continuous spatial evaluation) and `actor_action` (discrete event evaluation).
- **Rationale**: Continuous spatial curves (such as Catmull-Rom splines or ease-in-out transitions) require uninterrupted interpolation between keyframes. Triggering actions (swords swings, damage flinches, item equips, or poses) in the middle of a movement arc should not split or distort the underlying velocity curve or require redundant coordinate definitions.
- **Alternatives Considered**: Unified mono-track (`actor`). Rejected because forcing action triggers into spatial keyframes breaks spline acceleration and makes timing tweaks tedious.

### 2. Component-Based Actor Capabilities (Enfoque B)
- **Decision**: Define a lightweight `Actor` interface that exposes optional capabilities:
  - `asMovable()`: spatial positioning, relative movement, body and head rotation.
  - `asAnimatable()`: entity poses (`CROUCHING`, `SWIMMING`, `SLEEPING`, etc.), combat animation packets (`SWING`, `HURT`, `CRITICAL`), and item usage states (`BLOCKING`, `BOW_PULL`).
  - `asEquippable()`: slot equipment updates (`MAIN_HAND`, `OFF_HAND`, `HELMET`, etc.).
- **Rationale**: Adheres to the Interface Segregation Principle (ISP). Humanoid actors implement all three capabilities; future props or display entities only implement what they support without throwing unsupported operation exceptions.
- **Alternatives Considered**: Monolithic `Actor` with all methods. Rejected due to tight coupling and poor support for non-humanoid actors.

### 3. Packet-Based Virtual Actor and Self-Clone Lifecycle
- **Decision**: Manage virtual actors and viewer clones purely via clientbound packets (`PlayerInfoUpdate`, `AddEntity`, `SetEntityData`, `SetEquipment`, `Animate`, `RemoveEntities`) using `ProtocolLibBridge`.
- **Rationale**:
  - Eliminates orphan entities in Bukkit worlds if the server or client restarts.
  - Allows each viewer to see their own tailored self-clone (`viewer.getPlayerProfile()`).
  - Client connections automatically dispose of virtual entities if the player disconnects.
- **Alternatives Considered**: Spawning Bukkit server-side entities with metadata tags. Rejected due to entity chunk leaks, tick overhead on the server thread, and lack of per-player isolation.

### 4. Movement Heading and Spline Trajectory
- **Decision**: `actor_motion` tracks will support `path_mode: "spline"` (reusing Catmull-Rom math) and `heading: "tangent"` (or `follow_path: true`).
- **Rationale**: Computing the derivative/velocity vector \(\vec{v} = \frac{d\vec{p}}{dt}\) at each tick yields the exact forward tangent angle \(\text{yaw} = \text{atan2}(-v_x, v_z)\), freeing the animator from manually calculating rotation on complex curves.
- **Alternatives Considered**: Requiring explicit yaw/pitch keyframes for every waypoint. Rejected due to poor authoring ergonomics.

### 5. Skin Caching Architecture
- **Decision**: Virtual actors specified with a player skin name (`skin: "PlayerName"`) resolve their `GameProfile` textures via Mojang API and store them in an in-memory and disk cache (`.cinematic/skins_cache/`).
- **Rationale**: Prevents Mojang session server rate-limiting (600 requests / 10 minutes) and allows cutscenes to run offline once cached.

## Risks / Trade-offs

- **[Risk]** Packet structure differences across Paper/Minecraft versions.
  - **Mitigation**: Encapsulate all packet building inside adapter classes utilizing ProtocolLib's version-agnostic wrapper patterns, matching the existing `CameraRigManager` design.
- **[Risk]** Viewer disconnection during cinematic leaves dangling state.
  - **Mitigation**: Register `PlayerQuitListener` hook in `DemoCinematicOrchestrator` to immediately call `endSession(viewerId)` on `SceneActorSession`.
- **[Risk]** Large scenes with many virtual actors sending too many packets.
  - **Mitigation**: Virtual actors only send `RelEntityMove` or metadata updates when state actually changes or during active keyframe spans.
