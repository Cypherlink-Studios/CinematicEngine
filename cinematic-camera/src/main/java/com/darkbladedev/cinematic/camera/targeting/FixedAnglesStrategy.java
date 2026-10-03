package com.darkbladedev.cinematic.camera.targeting;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

public final class FixedAnglesStrategy implements LookAtStrategy {
    public static final FixedAnglesStrategy INSTANCE = new FixedAnglesStrategy();

    @Override
    public CameraOrientation calculateOrientation(
            long tick,
            Vector3d currentPosition,
            Vector3d velocityDirection,
            float fallbackYaw,
            float fallbackPitch,
            TimelineContext context
    ) {
        return new CameraOrientation(fallbackYaw, fallbackPitch);
    }
}
