package com.darkbladedev.cinematic.actors;

import org.joml.Vector3d;

import java.util.Optional;
import java.util.UUID;

public interface Actor {
    UUID id();

    void teleport(Vector3d position);

    void rotate(float yaw, float pitch);

    boolean isValid();

    default void despawn() {
    }

    default Optional<Movable> asMovable() {
        if (this instanceof Movable movable) {
            return Optional.of(movable);
        }
        return Optional.empty();
    }

    default Optional<Animatable> asAnimatable() {
        if (this instanceof Animatable animatable) {
            return Optional.of(animatable);
        }
        return Optional.empty();
    }

    default Optional<Equippable> asEquippable() {
        if (this instanceof Equippable equippable) {
            return Optional.of(equippable);
        }
        return Optional.empty();
    }
}
