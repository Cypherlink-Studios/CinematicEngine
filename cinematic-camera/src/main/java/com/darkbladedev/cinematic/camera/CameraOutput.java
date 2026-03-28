package com.darkbladedev.cinematic.camera;

@FunctionalInterface
public interface CameraOutput {
    void apply(CameraState state);
}
