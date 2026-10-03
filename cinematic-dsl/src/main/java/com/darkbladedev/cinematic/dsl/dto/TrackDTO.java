package com.darkbladedev.cinematic.dsl.dto;

import java.util.List;
import java.util.Map;

public record TrackDTO(
        String id,
        String type,
        Map<String, Object> data,
        List<KeyframeDTO> keyframes
) {
    public Object get(String key) {
        return data == null ? null : data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        if (data == null || !data.containsKey(key)) {
            return defaultValue;
        }
        Object value = data.get(key);
        return value == null ? defaultValue : (T) value;
    }
}
