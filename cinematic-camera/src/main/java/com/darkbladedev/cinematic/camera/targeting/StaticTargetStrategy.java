package com.darkbladedev.cinematic.camera.targeting;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

import java.util.Objects;

public final class StaticTargetStrategy implements LookAtStrategy {
    private final Vector3d target;

    public StaticTargetStrategy(Vector3d target) {
        this.target = new Vector3d(Objects.requireNonNull(target, "target"));
    }

    public Vector3d target() {
        return new Vector3d(target);
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
        return LookAtStrategy.computeDirection(currentPosition, target, fallbackYaw, fallbackPitch);
    }
}
