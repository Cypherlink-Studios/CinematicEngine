# actor-animation-tracks Specification

## Purpose
Provides independent multi-layer tracks for continuous spatial actor motion with spline and tangent heading support, and discrete dramatic actor actions for poses, animations, item usage, and equipment changes.

## Requirements

### Requirement: Continuous Actor Motion Track
The system SHALL support an `actor_motion` track that evaluates continuous position and orientation interpolation along keyframes. The track SHALL support linear and Catmull-Rom spline path modes, and SHALL support automatic velocity-forward orientation facing the movement tangent, target look-at coordinates, or fixed angle keyframes.

#### Scenario: Continuous motion with spline interpolation
- **WHEN** an `actor_motion` track with `spline` path mode is evaluated at an intermediate tick between keyframes
- **THEN** the actor's position is smoothly updated along the Catmull-Rom spline curve without step stutter

#### Scenario: Automatic heading facing motion tangent
- **WHEN** an `actor_motion` keyframe specifies tangent-following orientation (`follow_path: true` or `heading: "tangent"`)
- **THEN** the actor body yaw is calculated from the motion velocity vector and updated to face the direction of travel

### Requirement: Discrete Actor Action and Pose Track
The system SHALL support an `actor_action` track that dispatches discrete events for entity poses (`STANDING`, `CROUCHING`, `SWIMMING`, `SLEEPING`, `FALL_FLYING`, `SPIN_ATTACK`), combat animations (`SWING_MAIN_HAND`, `SWING_OFF_HAND`, `HURT`, `CRITICAL_HIT`), item usage states (`BLOCKING`, `BOW_PULL`, `EATING`), and equipment changes (`MAIN_HAND`, `OFF_HAND`, armor slots) at designated timeline ticks without altering or interrupting spatial motion trajectories.

#### Scenario: Entity pose modification at designated tick
- **WHEN** a cinematic timeline reaches a tick configured with a pose change to `CROUCHING`
- **THEN** entity metadata packets are sent updating the actor's pose state to crouching

#### Scenario: Combat animation and equipment swap dispatch
- **WHEN** a timeline tick triggers `SWING_MAIN_HAND` and equips a `DIAMOND_SWORD`
- **THEN** an animation packet and an equipment packet are dispatched to viewers for that actor

#### Scenario: Action dispatch does not fragment motion trajectory
- **WHEN** an actor has concurrent `actor_motion` and `actor_action` tracks active over the same tick interval
- **THEN** action events execute precisely on their keyframe ticks while motion interpolation proceeds continuously across the interval without velocity dips or coordinate redefinition
