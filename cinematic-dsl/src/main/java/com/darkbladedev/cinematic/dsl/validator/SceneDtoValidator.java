package com.darkbladedev.cinematic.dsl.validator;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;

import java.util.List;
import java.util.Objects;

public final class SceneDtoValidator {
    private final TrackRegistry trackRegistry;
    private final InterpolatorRegistry interpolatorRegistry;

    public SceneDtoValidator(TrackRegistry trackRegistry, InterpolatorRegistry interpolatorRegistry) {
        this.trackRegistry = Objects.requireNonNull(trackRegistry, "trackRegistry");
        this.interpolatorRegistry = Objects.requireNonNull(interpolatorRegistry, "interpolatorRegistry");
    }

    public void validate(SceneDTO dto) {
        ValidationCollector collector = new ValidationCollector();
        if (dto == null) {
            collector.add("La escena no puede ser nula.");
            throwValidationIfNeeded(collector);
        }
        if (dto.id() == null || dto.id().isBlank()) {
            collector.add("El id de la escena es obligatorio.");
        }
        if (dto.duration() <= 0) {
            collector.add("La duración debe ser mayor que 0.");
        }
        if (dto.tracks() == null || dto.tracks().isEmpty()) {
            collector.add("La escena debe tener al menos un track.");
            throwValidationIfNeeded(collector);
        }
        List<TrackDTO> tracks = dto.tracks();
        for (int index = 0; index < tracks.size(); index++) {
            validateTrack(tracks.get(index), index, dto.duration(), collector);
        }
        throwValidationIfNeeded(collector);
    }

    private void validateTrack(TrackDTO track, int index, int duration, ValidationCollector collector) {
        if (track == null) {
            collector.add("Track nulo en índice " + index + ".");
            return;
        }
        if (track.type() == null || track.type().isBlank()) {
            collector.add("tracks[" + index + "].type es obligatorio.");
            return;
        }
        if (!trackRegistry.contains(track.type())) {
            collector.add("tracks[" + index + "].type no soportado: " + track.type());
            return;
        }
        if (track.keyframes() == null || track.keyframes().isEmpty()) {
            collector.add("tracks[" + index + "] debe contener keyframes.");
            return;
        }
        long previousTick = Long.MIN_VALUE;
        for (int frameIndex = 0; frameIndex < track.keyframes().size(); frameIndex++) {
            KeyframeDTO frame = track.keyframes().get(frameIndex);
            if (frame == null) {
                collector.add("tracks[" + index + "].keyframes[" + frameIndex + "] no puede ser nulo.");
                continue;
            }
            if (frame.tick() < 0L || frame.tick() > duration) {
                collector.add("tracks[" + index + "].keyframes[" + frameIndex + "].tick fuera de rango.");
            }
            if (frame.tick() <= previousTick) {
                collector.add("tracks[" + index + "].keyframes debe estar ordenado por tick ascendente sin repetidos.");
            }
            previousTick = frame.tick();
            if (frame.interpolation() != null && !frame.interpolation().isBlank() && !interpolatorRegistry.contains(frame.interpolation())) {
                collector.add("Interpolador inválido en tracks[" + index + "].keyframes[" + frameIndex + "]: " + frame.interpolation());
            }
        }
        trackRegistry.validatorFor(track.type()).ifPresent(validator -> validator.validate(track, duration, collector));
    }

    private void throwValidationIfNeeded(ValidationCollector collector) {
        if (collector.hasErrors()) {
            throw new SceneValidationException(String.join(" | ", collector.errors()));
        }
    }
}
