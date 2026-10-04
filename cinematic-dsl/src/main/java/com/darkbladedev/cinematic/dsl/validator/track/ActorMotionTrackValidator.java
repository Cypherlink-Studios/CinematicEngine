package com.darkbladedev.cinematic.dsl.validator.track;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;

public final class ActorMotionTrackValidator implements TrackValidator {
    @Override
    public void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector) {
        if (!dto.data().containsKey("actor_id")) {
            collector.add("tracks[type=actor_motion].actor_id es obligatorio.");
        }
        if (dto.keyframes().isEmpty()) {
            collector.add("tracks[type=actor_motion].keyframes no puede estar vacío.");
            return;
        }
        for (int i = 0; i < dto.keyframes().size(); i++) {
            KeyframeDTO frame = dto.keyframes().get(i);
            String basePath = "tracks[type=actor_motion].keyframes[" + i + "]";
            if (!frame.values().containsKey("position")) {
                collector.add(basePath + ".position es obligatorio.");
            }
        }
    }
}
