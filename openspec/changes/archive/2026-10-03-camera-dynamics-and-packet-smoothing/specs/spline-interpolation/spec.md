# Spec Delta: Spline Interpolation

## Purpose

Computes continuous, smooth 3D trajectories across waypoints using centripetal Catmull-Rom splines without sharp angular corners.

## ADDED Requirements

### Requirement: Centripetal Catmull-Rom Spline Evaluation
The system SHALL evaluate 3D positions along a centripetal Catmull-Rom spline with continuous velocity across waypoints.

#### Scenario: Smooth trajectory across keyframes
- **WHEN** a camera track configured for spline pathing evaluates ticks between waypoints
- **THEN** evaluated positions follow a continuous curve that passes through each keyframe position without abrupt directional kinks at keyframe boundaries.

#### Scenario: Exact keyframe position arrival
- **WHEN** evaluation tick matches a keyframe's exact tick
- **THEN** the returned position matches the keyframe's spatial coordinates within numeric floating-point tolerance.

### Requirement: Boundary and Edge-Case Resiliency
The system SHALL handle trajectory boundaries and identical coordinates without producing numeric errors.

#### Scenario: Phantom endpoint extrapolation
- **WHEN** evaluating the initial or final segments of a keyframe sequence
- **THEN** boundary control points are synthesized to produce smooth tangents through the first and last waypoints.

#### Scenario: Coincident keyframe coordinates
- **WHEN** consecutive keyframes share identical coordinates
- **THEN** the spline returns that coordinate stably without division by zero or NaN values.
