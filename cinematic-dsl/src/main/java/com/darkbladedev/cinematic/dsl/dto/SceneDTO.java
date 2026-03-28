package com.darkbladedev.cinematic.dsl.dto;

import java.util.List;
import java.util.Map;

public record SceneDTO(
        String id,
        int duration,
        List<TrackDTO> tracks,
        Map<String, Object> metadata
) {
}
