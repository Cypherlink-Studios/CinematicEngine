package com.darkbladedev.cinematic.camera;

import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.model.Keyframe;
import org.joml.Vector3d;

import java.util.Objects;

public record CameraFrame(long tick, Vector3d position, float yaw, float pitch, float fov, Interpolator interpolator) implements Keyframe {
    public CameraFrame {
        Objects.requireNonNull(position, "position");
        interpolator = interpolator == null ? new LinearInterpolator() : interpolator;
    }
}
