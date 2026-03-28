package com.darkbladedev.cinematic.dsl.validator;

import com.darkbladedev.cinematic.dsl.dto.TrackDTO;

public interface TrackValidator {
    void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector);
}
