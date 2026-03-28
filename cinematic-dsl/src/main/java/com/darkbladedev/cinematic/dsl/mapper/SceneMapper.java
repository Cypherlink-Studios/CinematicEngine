package com.darkbladedev.cinematic.dsl.mapper;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.validator.SceneDtoValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SceneMapper {
    private final SceneDtoValidator validator;
    private final TrackRegistry trackRegistry;

    public SceneMapper(SceneDtoValidator validator, TrackRegistry trackRegistry) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.trackRegistry = Objects.requireNonNull(trackRegistry, "trackRegistry");
    }

    public Scene map(SceneDTO dto) {
        validator.validate(dto);
        List<Track> tracks = new ArrayList<>(dto.tracks().size());
        for (TrackDTO trackDto : dto.tracks()) {
            TrackFactory factory = trackRegistry.factoryFor(trackDto.type())
                    .orElseThrow(() -> new SceneMappingException("Track no soportado: " + trackDto.type()));
            tracks.add(factory.create(trackDto));
        }
        return new Scene(dto.id(), dto.duration(), tracks);
    }
}
