package com.darkbladedev.cinematic.dsl.registry;

import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;

public interface TrackFactory {
    Track create(TrackDTO dto);
}
