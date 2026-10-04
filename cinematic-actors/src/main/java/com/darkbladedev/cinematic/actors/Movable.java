package com.darkbladedev.cinematic.actors;

import org.joml.Vector3d;

public interface Movable {
    Vector3d position();

    void moveTo(Vector3d position, boolean instant);

    void rotate(float yaw, float pitch);

    default void rotateHead(float headYaw, float headPitch) {
    }

    default float yaw() {
        return 0.0f;
    }

    default float pitch() {
        return 0.0f;
    }

    default float headYaw() {
        return yaw();
    }
}
