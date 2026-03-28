package com.darkbladedev.cinematic.dsl.validator;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.DefaultDslComponents;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SceneDtoValidatorTest {
    @Test
    void rejectsInvalidDurationAndInvalidInterpolator() {
        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator validator = new SceneDtoValidator(trackRegistry, interpolatorRegistry);
        SceneDTO invalidScene = new SceneDTO(
                "intro",
                0,
                List.of(
                        new TrackDTO(
                                "cam1",
                                "camera",
                                Map.of(),
                                List.of(
                                        new KeyframeDTO(0L, "invalid", Map.of("position", List.of(0, 1, 2), "yaw", 0, "pitch", 0))
                                )
                        )
                ),
                Map.of()
        );

        assertThatThrownBy(() -> validator.validate(invalidScene))
                .isInstanceOf(SceneValidationException.class)
                .hasMessageContaining("duración")
                .hasMessageContaining("Interpolador inválido");
    }
}
