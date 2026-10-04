package com.darkbladedev.cinematic.dsl.validator.track;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;

import java.util.Map;

public final class CameraTrackValidator implements TrackValidator {
    @Override
    public void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector) {
        boolean hasDynamicLookAt = false;
        if (dto.data() != null) {
            Object lookAtObj = dto.data().get("look-at");
            if (lookAtObj == null) {
                lookAtObj = dto.data().get("lookAt");
            }
            if (lookAtObj == null) {
                lookAtObj = dto.data().get("look_at");
            }
            if (lookAtObj instanceof Map<?, ?> lookAtMap) {
                Object mode = lookAtMap.get("mode");
                if (mode != null && !"fixed".equalsIgnoreCase(mode.toString().trim())) {
                    hasDynamicLookAt = true;
                }
            }
        }

        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=camera].keyframes[" + index + "]";
            if (!frame.values().containsKey("position")) {
                collector.add(basePath + ".position es obligatorio.");
            }
            if (!hasDynamicLookAt) {
                if (!frame.values().containsKey("yaw")) {
                    collector.add(basePath + ".yaw es obligatorio.");
                }
                if (!frame.values().containsKey("pitch")) {
                    collector.add(basePath + ".pitch es obligatorio.");
                }
            }
        }
    }
}
