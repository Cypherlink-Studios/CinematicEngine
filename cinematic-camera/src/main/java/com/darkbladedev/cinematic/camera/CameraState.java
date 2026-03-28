package com.darkbladedev.cinematic.camera;

import org.joml.Vector3d;

public record CameraState(Vector3d position, float yaw, float pitch, float fov) {
}
