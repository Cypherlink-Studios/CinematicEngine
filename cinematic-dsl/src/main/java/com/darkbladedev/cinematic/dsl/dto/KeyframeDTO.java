package com.darkbladedev.cinematic.dsl.dto;

import java.util.Map;

public record KeyframeDTO(
        long tick,
        String interpolation,
        Map<String, Object> values
) {
}
