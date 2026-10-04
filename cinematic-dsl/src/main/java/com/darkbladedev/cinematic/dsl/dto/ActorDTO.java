package com.darkbladedev.cinematic.dsl.dto;

import org.joml.Vector3d;

import java.util.Map;

public record ActorDTO(
        String id,
        String type,
        String skin,
        Vector3d initialPosition,
        Float initialYaw,
        Float initialPitch,
        Map<String, String> initialEquipment,
        Map<String, Object> data
) {
    public ActorDTO {
        initialEquipment = initialEquipment == null ? Map.of() : Map.copyOf(initialEquipment);
        data = data == null ? Map.of() : Map.copyOf(data);
    }
}
