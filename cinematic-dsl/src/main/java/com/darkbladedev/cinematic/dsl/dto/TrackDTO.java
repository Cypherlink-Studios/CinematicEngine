package com.darkbladedev.cinematic.dsl.dto;

import java.util.List;
import java.util.Map;

public record TrackDTO(
        String id,
        String type,
        Map<String, Object> data,
        List<KeyframeDTO> keyframes
) {
}
