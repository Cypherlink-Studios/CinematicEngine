package com.darkbladedev.cinematic.dsl.validator.track;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;

public final class CameraTrackValidator implements TrackValidator {
    @Override
    public void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector) {
        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=camera].keyframes[" + index + "]";
            if (!frame.values().containsKey("position")) {
                collector.add(basePath + ".position es obligatorio.");
            }
            if (!frame.values().containsKey("yaw")) {
                collector.add(basePath + ".yaw es obligatorio.");
            }
            if (!frame.values().containsKey("pitch")) {
                collector.add(basePath + ".pitch es obligatorio.");
            }
        }
    }
}
