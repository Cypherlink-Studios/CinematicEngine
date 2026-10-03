package com.darkbladedev.cinematic.dsl.mapper.factory;

import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraPathMode;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.camera.targeting.*;
import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.dsl.dto.CameraTrackDTO;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.TypeConversion;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CameraTrackFactory implements TrackFactory {
    private final InterpolatorRegistry interpolatorRegistry;

    public CameraTrackFactory(InterpolatorRegistry interpolatorRegistry) {
        this.interpolatorRegistry = Objects.requireNonNull(interpolatorRegistry, "interpolatorRegistry");
    }

    @Override
    public CameraTrack create(TrackDTO dto) {
        CameraTrackDTO cameraDto = CameraTrackDTO.fromTrackDto(dto);
        String trackId = cameraDto.id() == null || cameraDto.id().isBlank() ? "camera_track" : cameraDto.id();
        CameraPathMode pathMode = cameraDto.pathMode();
        LookAtStrategy lookAtStrategy = resolveLookAtStrategy(cameraDto.lookAt());

        List<CameraFrame> frames = new ArrayList<>(cameraDto.keyframes().size());
        float currentFov = 70.0F;

        for (int index = 0; index < cameraDto.keyframes().size(); index++) {
            KeyframeDTO frame = cameraDto.keyframes().get(index);
            String basePath = "tracks[type=camera].keyframes[" + index + "]";
            Vector3d position = TypeConversion.toVector3(frame.values().get("position"), basePath + ".position");

            float yaw = 0.0F;
            if (frame.values().containsKey("yaw")) {
                yaw = TypeConversion.toFloat(frame.values().get("yaw"), basePath + ".yaw");
            }

            float pitch = 0.0F;
            if (frame.values().containsKey("pitch")) {
                pitch = TypeConversion.toFloat(frame.values().get("pitch"), basePath + ".pitch");
            }

            Object rawFov = frame.values().get("fov");
            if (rawFov != null) {
                currentFov = TypeConversion.toFloat(rawFov, basePath + ".fov");
            }

            Interpolator interpolator = interpolatorRegistry.resolveOrDefault(frame.interpolation());
            frames.add(new CameraFrame(frame.tick(), position, yaw, pitch, currentFov, interpolator));
        }

        return new CameraTrack(trackId, frames, pathMode, lookAtStrategy);
    }

    private LookAtStrategy resolveLookAtStrategy(Map<String, Object> lookAtMap) {
        if (lookAtMap == null || lookAtMap.isEmpty()) {
            return FixedAnglesStrategy.INSTANCE;
        }

        Object rawMode = lookAtMap.get("mode");
        String mode = rawMode == null ? "fixed" : rawMode.toString().trim().toLowerCase();

        switch (mode) {
            case "static", "point", "poi" -> {
                Object targetObj = lookAtMap.get("target");
                if (targetObj != null) {
                    Vector3d target = TypeConversion.toVector3(targetObj, "tracks[type=camera].look-at.target");
                    return new StaticTargetStrategy(target);
                }
                return FixedAnglesStrategy.INSTANCE;
            }
            case "actor" -> {
                Object rawActor = lookAtMap.get("target-actor");
                if (rawActor == null) {
                    rawActor = lookAtMap.get("targetActor");
                }
                if (rawActor == null) {
                    rawActor = lookAtMap.get("actor");
                }
                if (rawActor == null) {
                    rawActor = lookAtMap.get("actorId");
                }
                if (rawActor != null) {
                    return new ActorTargetStrategy(rawActor.toString());
                }
                return FixedAnglesStrategy.INSTANCE;
            }
            case "forward", "velocity" -> {
                return VelocityForwardStrategy.INSTANCE;
            }
            default -> {
                return FixedAnglesStrategy.INSTANCE;
            }
        }
    }
}
