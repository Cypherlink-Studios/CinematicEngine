package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.core.model.Keyframe;
import org.joml.Vector3d;

public record ActorMotionFrame(
        long tick,
        Vector3d position,
        Vector3d lookAt,
        Float yaw,
        Float pitch,
        Interpolator interpolator
) implements Keyframe {
}
