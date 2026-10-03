package com.darkbladedev.cinematic.dsl.mapper;

import com.darkbladedev.cinematic.camera.CameraPathMode;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.camera.targeting.ActorTargetStrategy;
import com.darkbladedev.cinematic.camera.targeting.StaticTargetStrategy;
import com.darkbladedev.cinematic.camera.targeting.VelocityForwardStrategy;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.factory.CameraTrackFactory;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;
import com.darkbladedev.cinematic.dsl.validator.track.CameraTrackValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CameraTrackFactoryTest {
    private CameraTrackFactory factory;
    private CameraTrackValidator validator;

    @BeforeEach
    void setUp() {
        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        factory = new CameraTrackFactory(interpolatorRegistry);
        validator = new CameraTrackValidator();
    }

    @Test
    void mapsCatmullRomSplinePathMode() {
        TrackDTO dto = new TrackDTO(
                "cam-1",
                "camera",
                Map.of("path-mode", "catmull-rom"),
                List.of(
                        new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 64, 0), "yaw", 0.0F, "pitch", 0.0F)),
                        new KeyframeDTO(20L, "linear", Map.of("position", List.of(10, 65, 0), "yaw", 0.0F, "pitch", 0.0F))
                )
        );

        CameraTrack track = factory.create(dto);
        assertThat(track.pathMode()).isEqualTo(CameraPathMode.CATMULL_ROM);
    }

    @Test
    void mapsStaticLookAtStrategy() {
        TrackDTO dto = new TrackDTO(
                "cam-static",
                "camera",
                Map.of(
                        "path-mode", "catmull-rom",
                        "look-at", Map.of("mode", "static", "target", List.of(100.0, 64.0, 50.0))
                ),
                List.of(
                        new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 64, 0))),
                        new KeyframeDTO(20L, "linear", Map.of("position", List.of(10, 64, 0)))
                )
        );

        ValidationCollector collector = new ValidationCollector();
        validator.validate(dto, 20, collector);
        assertThat(collector.errors()).isEmpty();

        CameraTrack track = factory.create(dto);
        assertThat(track.lookAtStrategy()).isInstanceOf(StaticTargetStrategy.class);
    }

    @Test
    void mapsActorLookAtStrategy() {
        TrackDTO dto = new TrackDTO(
                "cam-actor",
                "camera",
                Map.of(
                        "look-at", Map.of("mode", "actor", "target-actor", "boss-villager")
                ),
                List.of(
                        new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 64, 0))),
                        new KeyframeDTO(20L, "linear", Map.of("position", List.of(10, 64, 0)))
                )
        );

        CameraTrack track = factory.create(dto);
        assertThat(track.lookAtStrategy()).isInstanceOf(ActorTargetStrategy.class);
        ActorTargetStrategy strategy = (ActorTargetStrategy) track.lookAtStrategy();
        assertThat(strategy.actorId()).isEqualTo("boss-villager");
    }

    @Test
    void mapsVelocityForwardStrategy() {
        TrackDTO dto = new TrackDTO(
                "cam-forward",
                "camera",
                Map.of(
                        "look-at", Map.of("mode", "forward")
                ),
                List.of(
                        new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 64, 0))),
                        new KeyframeDTO(20L, "linear", Map.of("position", List.of(10, 64, 0)))
                )
        );

        CameraTrack track = factory.create(dto);
        assertThat(track.lookAtStrategy()).isInstanceOf(VelocityForwardStrategy.class);
    }
}
