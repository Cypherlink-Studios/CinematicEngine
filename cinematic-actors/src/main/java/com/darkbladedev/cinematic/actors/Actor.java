package com.darkbladedev.cinematic.actors;

import org.joml.Vector3d;

import java.util.UUID;

public interface Actor {
    UUID id();

    void teleport(Vector3d position);

    void rotate(float yaw, float pitch);

    boolean isValid();
}
