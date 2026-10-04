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

    @Test
    void rejectsDuplicateActorIdsAndMissingInitialPosition() {
        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator validator = new SceneDtoValidator(trackRegistry, interpolatorRegistry);

        SceneDTO duplicateActorsScene = new SceneDTO(
                "scene1",
                100,
                List.of(
                        new com.darkbladedev.cinematic.dsl.dto.ActorDTO("hero", "virtual", null, new org.joml.Vector3d(0, 0, 0), null, null, Map.of(), Map.of()),
                        new com.darkbladedev.cinematic.dsl.dto.ActorDTO("hero", "virtual", null, new org.joml.Vector3d(1, 1, 1), null, null, Map.of(), Map.of())
                ),
                List.of(
                        new TrackDTO("cam", "camera", Map.of(), List.of(new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 0, 0), "yaw", 0, "pitch", 0))))
                ),
                Map.of()
        );

        assertThatThrownBy(() -> validator.validate(duplicateActorsScene))
                .isInstanceOf(SceneValidationException.class)
                .hasMessageContaining("duplicado: 'hero'");

        SceneDTO missingPosScene = new SceneDTO(
                "scene2",
                100,
                List.of(
                        new com.darkbladedev.cinematic.dsl.dto.ActorDTO("warrior", "virtual", null, null, null, null, Map.of(), Map.of())
                ),
                List.of(
                        new TrackDTO("cam", "camera", Map.of(), List.of(new KeyframeDTO(0L, "linear", Map.of("position", List.of(0, 0, 0), "yaw", 0, "pitch", 0))))
                ),
                Map.of()
        );

        assertThatThrownBy(() -> validator.validate(missingPosScene))
                .isInstanceOf(SceneValidationException.class)
                .hasMessageContaining("initial_position es obligatorio");
    }

    @Test
    void acceptsValidActorsAndMultiLayerActorTracks() {
        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator validator = new SceneDtoValidator(trackRegistry, interpolatorRegistry);

        SceneDTO validScene = new SceneDTO(
                "valid_scene",
                100,
                List.of(
                        new com.darkbladedev.cinematic.dsl.dto.ActorDTO("hero", "self_clone", null, new org.joml.Vector3d(10, 64, 10), 0.0f, 0.0f, Map.of(), Map.of()),
                        new com.darkbladedev.cinematic.dsl.dto.ActorDTO("guard", "virtual", "GuardSkin", new org.joml.Vector3d(20, 64, 20), 180.0f, 0.0f, Map.of("main_hand", "IRON_SWORD"), Map.of())
                ),
                List.of(
                        new TrackDTO(
                                "motion",
                                "actor_motion",
                                Map.of("actor_id", "hero", "path_mode", "spline", "heading", "tangent"),
                                List.of(
                                        new KeyframeDTO(0L, "linear", Map.of("position", List.of(10, 64, 10))),
                                        new KeyframeDTO(50L, "linear", Map.of("position", List.of(15, 64, 12))),
                                        new KeyframeDTO(100L, "linear", Map.of("position", List.of(20, 64, 15)))
                                )
                        ),
                        new TrackDTO(
                                "actions",
                                "actor_action",
                                Map.of("actor_id", "hero"),
                                List.of(
                                        new KeyframeDTO(0L, null, Map.of("pose", "STANDING")),
                                        new KeyframeDTO(60L, null, Map.of("pose", "CROUCHING", "action", "SWING_MAIN_HAND", "item_usage", "BLOCKING"))
                                )
                        )
                ),
                Map.of()
        );

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> validator.validate(validScene));
    }
}
