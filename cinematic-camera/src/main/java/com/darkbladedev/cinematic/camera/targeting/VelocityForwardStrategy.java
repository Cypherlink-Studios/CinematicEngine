package com.darkbladedev.cinematic.camera.targeting;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

public final class VelocityForwardStrategy implements LookAtStrategy {
    public static final VelocityForwardStrategy INSTANCE = new VelocityForwardStrategy();

    @Override
    public CameraOrientation calculateOrientation(
            long tick,
            Vector3d currentPosition,
            Vector3d velocityDirection,
            float fallbackYaw,
            float fallbackPitch,
            TimelineContext context
    ) {
        if (velocityDirection == null || velocityDirection.lengthSquared() < 1e-8D) {
            return new CameraOrientation(fallbackYaw, fallbackPitch);
        }

        Vector3d forwardTarget = new Vector3d(currentPosition).add(velocityDirection);
        return LookAtStrategy.computeDirection(currentPosition, forwardTarget, fallbackYaw, fallbackPitch);
    }
}
