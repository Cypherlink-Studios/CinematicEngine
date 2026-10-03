# Spec Delta: Look-At Targeting

## Purpose

Directs camera view orientation dynamically toward points of interest, moving actors, or forward along the trajectory path.

## ADDED Requirements

### Requirement: Static Point-of-Interest Targeting
The system SHALL compute yaw and pitch to continuously orient the camera toward a fixed world coordinate.

#### Scenario: Orbiting or panning around a fixed coordinate
- **WHEN** a camera track is configured with a static look-at vector
- **THEN** camera orientation at each tick is computed pointing directly toward the target coordinate from the camera's current position.

### Requirement: Dynamic Actor Tracking
The system SHALL track a moving actor's position dynamically across timeline ticks.

#### Scenario: Camera follows a moving scene actor
- **WHEN** a camera track targets an actor by identifier
- **THEN** camera orientation is calculated toward the actor's evaluated position at each tick.

#### Scenario: Targeted actor not present
- **WHEN** the specified actor cannot be resolved in the timeline context
- **THEN** the camera maintains its last known orientation or falls back to default keyframe angles without throwing an unhandled exception.

### Requirement: Velocity Forward Heading
The system SHALL align camera yaw and pitch with the direction of travel when configured for forward heading.

#### Scenario: Camera moves along curved trajectory
- **WHEN** camera position changes between ticks in velocity-forward mode
- **THEN** yaw and pitch reflect the normalized direction vector of travel.
