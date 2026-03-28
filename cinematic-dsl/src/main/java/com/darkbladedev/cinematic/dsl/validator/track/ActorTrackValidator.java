package com.darkbladedev.cinematic.dsl.validator.track;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;

public final class ActorTrackValidator implements TrackValidator {
    @Override
    public void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector) {
        if (!dto.data().containsKey("actor_id")) {
            collector.add("tracks[type=actor].actor_id es obligatorio.");
        }
        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=actor].keyframes[" + index + "]";
            if (!frame.values().containsKey("position")) {
                collector.add(basePath + ".position es obligatorio.");
            }
        }
    }
}
