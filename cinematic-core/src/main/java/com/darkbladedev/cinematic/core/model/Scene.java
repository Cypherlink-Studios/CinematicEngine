package com.darkbladedev.cinematic.core.model;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;

import java.util.List;
import java.util.Objects;

public final class Scene {
    private final String id;
    private final long durationTicks;
    private final List<Track> tracks;

    public Scene(String id, long durationTicks, List<Track> tracks) {
        this.id = Objects.requireNonNull(id, "id");
        this.durationTicks = durationTicks;
        this.tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
    }

    public String id() {
        return id;
    }

    public long durationTicks() {
        return durationTicks;
    }

    public List<Track> tracks() {
        return tracks;
    }

    public void evaluate(long tick, TimelineContext context) {
        for (Track track : tracks) {
            if (track.isActiveAt(tick)) {
                track.evaluate(tick, context);
            }
        }
    }
}
