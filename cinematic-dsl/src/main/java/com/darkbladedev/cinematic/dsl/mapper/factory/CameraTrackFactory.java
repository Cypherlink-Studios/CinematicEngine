package com.darkbladedev.cinematic.dsl.mapper.factory;

import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.TypeConversion;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CameraTrackFactory implements TrackFactory {
    private final InterpolatorRegistry interpolatorRegistry;

    public CameraTrackFactory(InterpolatorRegistry interpolatorRegistry) {
        this.interpolatorRegistry = Objects.requireNonNull(interpolatorRegistry, "interpolatorRegistry");
    }

    @Override
    public CameraTrack create(TrackDTO dto) {
        String trackId = dto.id() == null || dto.id().isBlank() ? "camera_track" : dto.id();
        List<CameraFrame> frames = new ArrayList<>(dto.keyframes().size());
        float currentFov = 70.0F;
        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=camera].keyframes[" + index + "]";
            Vector3d position = TypeConversion.toVector3(frame.values().get("position"), basePath + ".position");
            float yaw = TypeConversion.toFloat(frame.values().get("yaw"), basePath + ".yaw");
            float pitch = TypeConversion.toFloat(frame.values().get("pitch"), basePath + ".pitch");
            Object rawFov = frame.values().get("fov");
            if (rawFov != null) {
                currentFov = TypeConversion.toFloat(rawFov, basePath + ".fov");
            }
            Interpolator interpolator = interpolatorRegistry.resolveOrDefault(frame.interpolation());
            frames.add(new CameraFrame(frame.tick(), position, yaw, pitch, currentFov, interpolator));
        }
        return new CameraTrack(trackId, frames);
    }
}
