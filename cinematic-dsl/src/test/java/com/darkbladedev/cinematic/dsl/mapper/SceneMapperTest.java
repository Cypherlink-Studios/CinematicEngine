package com.darkbladedev.cinematic.dsl.mapper;

import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.validator.SceneDtoValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SceneMapperTest {
    @Test
    void mapsCameraTrackToDomainScene() {
        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator validator = DefaultDslComponents.validator(trackRegistry, interpolatorRegistry);
        SceneMapper mapper = DefaultDslComponents.mapper(validator, trackRegistry);
        SceneDTO dto = new SceneDTO(
                "intro",
                200,
                List.of(
                        new TrackDTO(
                                "camera_main",
                                "camera",
                                Map.of(),
                                List.of(
                                        new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 70, 0), "yaw", 0.0, "pitch", 0.0, "fov", 70.0)),
                                        new KeyframeDTO(100L, "ease_in_out", Map.of("position", List.of(10, 75, 10), "yaw", 90.0, "pitch", -10.0))
                                )
                        )
                ),
                Map.of()
        );

        Scene scene = mapper.map(dto);

        assertThat(scene.id()).isEqualTo("intro");
        assertThat(scene.durationTicks()).isEqualTo(200L);
        assertThat(scene.tracks()).hasSize(1);
        assertThat(scene.tracks().getFirst()).isInstanceOf(CameraTrack.class);
    }
}
