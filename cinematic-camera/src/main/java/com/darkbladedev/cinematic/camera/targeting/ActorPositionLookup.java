package com.darkbladedev.cinematic.camera.targeting;

import org.joml.Vector3d;

import java.util.Optional;

@FunctionalInterface
public interface ActorPositionLookup {
    Optional<Vector3d> findPosition(String actorId);
}
