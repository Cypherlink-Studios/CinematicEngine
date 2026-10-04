package com.darkbladedev.cinematic.dsl.mapper.factory;

import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.TypeConversion;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.runtime.ActorMotionFrame;
import com.darkbladedev.cinematic.dsl.runtime.ActorMotionTrack;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ActorMotionTrackFactory implements TrackFactory {
    private final InterpolatorRegistry interpolatorRegistry;

    public ActorMotionTrackFactory(InterpolatorRegistry interpolatorRegistry) {
        this.interpolatorRegistry = Objects.requireNonNull(interpolatorRegistry, "interpolatorRegistry");
    }

    @Override
    public ActorMotionTrack create(TrackDTO dto) {
        String trackId = dto.id() == null || dto.id().isBlank() ? "actor_motion_track" : dto.id();
        String actorId = TypeConversion.toStringValue(dto.data().get("actor_id"), "tracks[type=actor_motion].actor_id");

        String pathMode = dto.data().get("path_mode") != null ? dto.data().get("path_mode").toString() : "linear";
        String heading = dto.data().get("heading") != null ? dto.data().get("heading").toString() : "tangent";
        Vector3d globalLookAt = null;
        if (dto.data().containsKey("look_at")) {
            globalLookAt = TypeConversion.toVector3(dto.data().get("look_at"), "tracks[type=actor_motion].look_at");
        }

        List<ActorMotionFrame> frames = new ArrayList<>(dto.keyframes().size());
        for (int i = 0; i < dto.keyframes().size(); i++) {
            KeyframeDTO frame = dto.keyframes().get(i);
            String basePath = "tracks[type=actor_motion].keyframes[" + i + "]";
            Vector3d position = TypeConversion.toVector3(frame.values().get("position"), basePath + ".position");
            Vector3d lookAt = null;
            if (frame.values().containsKey("look_at")) {
                lookAt = TypeConversion.toVector3(frame.values().get("look_at"), basePath + ".look_at");
            }

            Float yaw = null;
            if (frame.values().containsKey("yaw") && frame.values().get("yaw") instanceof Number n) {
                yaw = n.floatValue();
            }
            Float pitch = null;
            if (frame.values().containsKey("pitch") && frame.values().get("pitch") instanceof Number n) {
                pitch = n.floatValue();
            }

            Interpolator interpolator = interpolatorRegistry.resolveOrDefault(frame.interpolation());
            frames.add(new ActorMotionFrame(frame.tick(), position, lookAt, yaw, pitch, interpolator));
        }
        return new ActorMotionTrack(trackId, actorId, pathMode, heading, globalLookAt, frames);
    }
}
