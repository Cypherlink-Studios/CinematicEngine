package com.darkbladedev.cinematic.dsl.validator.track;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;

public final class EffectTrackValidator implements TrackValidator {
    @Override
    public void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector) {
        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=effect].keyframes[" + index + "]";
            boolean hasSound = frame.values().containsKey("play_sound");
            boolean hasParticle = frame.values().containsKey("particle");
            if (!hasSound && !hasParticle) {
                collector.add(basePath + " debe definir play_sound o particle.");
            }
        }
    }
}
