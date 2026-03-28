package com.darkbladedev.cinematic.testing.support;

import com.darkbladedev.cinematic.camera.CameraState;
import org.joml.Vector3d;

public record FrameSnapshot(long tick, Vector3d position, float yaw, float pitch, float fov) {
    public static FrameSnapshot of(long tick, CameraState state) {
        return new FrameSnapshot(tick, new Vector3d(state.position()), state.yaw(), state.pitch(), state.fov());
    }
}
