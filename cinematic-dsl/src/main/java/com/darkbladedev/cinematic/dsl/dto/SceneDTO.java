package com.darkbladedev.cinematic.dsl.dto;

import java.util.List;
import java.util.Map;

public record SceneDTO(
        String id,
        int duration,
        List<ActorDTO> actors,
        List<TrackDTO> tracks,
        Map<String, Object> metadata
) {
    public SceneDTO {
        actors = actors == null ? List.of() : List.copyOf(actors);
        tracks = tracks == null ? List.of() : List.copyOf(tracks);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public SceneDTO(String id, int duration, List<TrackDTO> tracks, Map<String, Object> metadata) {
        this(id, duration, List.of(), tracks, metadata);
    }
}
