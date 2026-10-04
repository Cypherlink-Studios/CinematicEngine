package com.darkbladedev.cinematic.actors;

import org.joml.Vector3d;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FakeEntityActor implements Actor, Movable {
    private final UUID id;
    private final Consumer<Vector3d> teleportHandler;
    private final BiConsumer<Float, Float> rotationHandler;
    private final Supplier<Boolean> validityHandler;
    private Vector3d lastPosition = new Vector3d();
    private float lastYaw;
    private float lastPitch;

    public FakeEntityActor(
            UUID id,
            Consumer<Vector3d> teleportHandler,
            BiConsumer<Float, Float> rotationHandler,
            Supplier<Boolean> validityHandler
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.teleportHandler = Objects.requireNonNull(teleportHandler, "teleportHandler");
        this.rotationHandler = Objects.requireNonNull(rotationHandler, "rotationHandler");
        this.validityHandler = Objects.requireNonNull(validityHandler, "validityHandler");
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public void teleport(Vector3d position) {
        this.lastPosition = new Vector3d(position);
        teleportHandler.accept(position);
    }

    @Override
    public void rotate(float yaw, float pitch) {
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        rotationHandler.accept(yaw, pitch);
    }

    @Override
    public boolean isValid() {
        return validityHandler.get();
    }

    @Override
    public Vector3d position() {
        return new Vector3d(lastPosition);
    }

    @Override
    public void moveTo(Vector3d position, boolean instant) {
        teleport(position);
    }

    @Override
    public float yaw() {
        return lastYaw;
    }

    @Override
    public float pitch() {
        return lastPitch;
    }
}
