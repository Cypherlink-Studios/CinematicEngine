package com.darkbladedev.cinematic.camera.targeting;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.Optional;

public final class ActorTargetStrategy implements LookAtStrategy {
    private final String actorId;

    public ActorTargetStrategy(String actorId) {
        this.actorId = Objects.requireNonNull(actorId, "actorId");
    }

    public String actorId() {
        return actorId;
    }

    @Override
    public CameraOrientation calculateOrientation(
            long tick,
            Vector3d currentPosition,
            Vector3d velocityDirection,
            float fallbackYaw,
            float fallbackPitch,
            TimelineContext context
    ) {
        if (context == null) {
            return new CameraOrientation(fallbackYaw, fallbackPitch);
        }

        Optional<ActorPositionLookup> lookup = context.getService(ActorPositionLookup.class);
        if (lookup.isEmpty()) {
            return new CameraOrientation(fallbackYaw, fallbackPitch);
        }

        Optional<Vector3d> actorPos = lookup.get().findPosition(actorId);
        if (actorPos.isEmpty()) {
            return new CameraOrientation(fallbackYaw, fallbackPitch);
        }

        return LookAtStrategy.computeDirection(currentPosition, actorPos.get(), fallbackYaw, fallbackPitch);
    }
}
