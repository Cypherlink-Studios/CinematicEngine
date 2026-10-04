package com.darkbladedev.cinematic.dsl.dto;

import com.darkbladedev.cinematic.camera.CameraPathMode;

import java.util.List;
import java.util.Map;

public record CameraTrackDTO(
        String id,
        CameraPathMode pathMode,
        Map<String, Object> lookAt,
        List<KeyframeDTO> keyframes
) {
    @SuppressWarnings("unchecked")
    public static CameraTrackDTO fromTrackDto(TrackDTO dto) {
        CameraPathMode mode = CameraPathMode.LINEAR;
        Map<String, Object> lookAtMap = null;

        if (dto.data() != null) {
            Object rawMode = dto.data().get("path-mode");
            if (rawMode == null) {
                rawMode = dto.data().get("pathMode");
            }
            if (rawMode == null) {
                rawMode = dto.data().get("path_mode");
            }
            if (rawMode != null) {
                try {
                    mode = CameraPathMode.valueOf(rawMode.toString().trim().replace("-", "_").toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    mode = CameraPathMode.LINEAR;
                }
            }

            Object rawLookAt = dto.data().get("look-at");
            if (rawLookAt == null) {
                rawLookAt = dto.data().get("lookAt");
            }
            if (rawLookAt == null) {
                rawLookAt = dto.data().get("look_at");
            }
            if (rawLookAt instanceof Map<?, ?> map) {
                lookAtMap = (Map<String, Object>) map;
            }
        }

        return new CameraTrackDTO(dto.id(), mode, lookAtMap, dto.keyframes());
    }
}
