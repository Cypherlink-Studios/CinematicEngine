package com.darkbladedev.cinematic.dsl.mapper.factory;

import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.TypeConversion;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.runtime.ActorFrame;
import com.darkbladedev.cinematic.dsl.runtime.ActorTrack;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ActorTrackFactory implements TrackFactory {
    private final InterpolatorRegistry interpolatorRegistry;

    public ActorTrackFactory(InterpolatorRegistry interpolatorRegistry) {
        this.interpolatorRegistry = Objects.requireNonNull(interpolatorRegistry, "interpolatorRegistry");
    }

    @Override
    public ActorTrack create(TrackDTO dto) {
        String trackId = dto.id() == null || dto.id().isBlank() ? "actor_track" : dto.id();
        String actorId = TypeConversion.toStringValue(dto.data().get("actor_id"), "tracks[type=actor].actor_id");
        List<ActorFrame> frames = new ArrayList<>(dto.keyframes().size());
        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=actor].keyframes[" + index + "]";
            Vector3d position = TypeConversion.toVector3(frame.values().get("position"), basePath + ".position");
            Vector3d lookAt = null;
            if (frame.values().containsKey("look_at")) {
                lookAt = TypeConversion.toVector3(frame.values().get("look_at"), basePath + ".look_at");
            }
            Interpolator interpolator = interpolatorRegistry.resolveOrDefault(frame.interpolation());
            frames.add(new ActorFrame(frame.tick(), position, lookAt, interpolator));
        }
        return new ActorTrack(trackId, actorId, frames);
    }
}
