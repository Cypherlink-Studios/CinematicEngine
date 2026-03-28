package com.darkbladedev.cinematic.dsl.runtime;

import org.joml.Vector3d;

public record ParticleEffect(String type, int count, Vector3d offset, float speed) {
}
