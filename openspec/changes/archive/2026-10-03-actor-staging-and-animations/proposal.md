# Proposal

## Why

CinematicEngine currently has a minimal `Actor` interface limited to rigid tick-by-tick `teleport()` and `rotate()`, with no runtime actor lifecycle management, no staging orchestration, and no support for Minecraft poses, combat animations, item usage, or equipment changes.

This change introduces a production-ready actor staging and animation architecture. By separating continuous spatial movement (`actor_motion`) from discrete dramatic actions (`actor_action`), and providing packet-level virtual actor spawning and viewer self-cloning via ProtocolLib, creators can stage dynamic, living character cutscenes without world pollution or jagged interpolation.

## What Changes

- **Capability-Based Actor Model**: Refactor `Actor` to expose modular capabilities (`asMovable()`, `asAnimatable()`, `asEquippable()`) rather than monolithic methods.
- **Scene Cast / Staging Declaration (`actors:`)**: Support an explicit `actors:` section in Scene YAML defining virtual actors (packet-based), optional viewer self-clones (`self_clone`), and persistent world entities (`persistent`).
- **Packet-Based Actor Lifecycle & Session Management**: Introduce `SceneActorSession` to handle clientbound packet spawning (`PlayerInfoUpdate`, `AddEntity`, `SetEquipment`, `SetEntityData`), texture/skin caching for virtual actors, viewer profile copying for `self_clone`, and guaranteed cleanup upon cutscene completion, pause, cancellation, or player disconnection.
- **Multi-Layer Track Architecture (Option B)**:
  - Add `actor_motion` track: Continuous spatial interpolation supporting linear and Catmull-Rom spline curves, with automatic movement tangent orientation (`follow_path`) or look-at targeting.
  - Add `actor_action` track: Discrete event dispatching for poses (`STANDING`, `CROUCHING`, `SWIMMING`, `SLEEPING`, `FALL_FLYING`, `SPIN_ATTACK`), animation packets (`SWING_MAIN_HAND`, `SWING_OFF_HAND`, `HURT`, `CRITICAL_HIT`), item usage flags (`BLOCKING`, `BOW_PULL`), and equipment updates (`MAIN_HAND`, `OFF_HAND`, armor slots).
- **DSL Parser & Validator Extensions**: Update `SceneDTO`, `SceneDtoValidator`, and `SceneMapper` to validate and map `actors:`, `actor_motion`, and `actor_action`.
- **Runtime Orchestrator Integration**: Wire `SceneActorSession` into `DemoCinematicOrchestrator` to provide `ActorResolver` to `TimelineContext`.

## Capabilities

### New Capabilities
- `actor-staging`: Staging, declaration (`actors:`), and packet-based lifecycle orchestration for virtual mannequins, viewer self-clones, and persistent world entities with clean rollback.
- `actor-animation-tracks`: Independent continuous motion (`actor_motion` with splines and automatic tangent heading) and discrete dramatic action (`actor_action` with poses, animation packets, item usage flags, and equipment) tracks.

### Modified Capabilities
<!-- None: Existing specs (camera-rig, look-at-targeting, spline-interpolation, plugin-documentation) maintain their existing contracts. -->

## Impact

- `cinematic-actors`: Expands `Actor` with `Movable`, `Animatable`, `Equippable` capability interfaces and implements them in `PlayerActor`, `VirtualPlayerActor`, and persistent wrappers.
- `cinematic-adapters`: Implements packet-level virtual entity spawning and session cleanup via `ProtocolLibBridge`, skin caching, and orchestrator session management.
- `cinematic-dsl`: Adds DTOs, factories, and validators for `actors:`, `actor_motion`, and `actor_action`.
- `cinematic-runtime` & `plugin-bootstrap`: Integrates `SceneActorSession` into the cinematic playback lifecycle in `DemoCinematicOrchestrator`.
