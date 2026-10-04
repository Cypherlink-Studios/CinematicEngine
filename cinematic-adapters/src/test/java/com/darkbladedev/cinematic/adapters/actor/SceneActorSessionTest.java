package com.darkbladedev.cinematic.adapters.actor;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.dsl.dto.ActorDTO;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import org.joml.Vector3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SceneActorSessionTest {

    @Test
    @DisplayName("SceneActorSession initializes virtual actors and resolves them cleanly")
    void initializesAndResolvesActors() {
        SceneDTO scene = new SceneDTO(
                "test_scene",
                100,
                List.of(
                        new ActorDTO("hero", "self_clone", null, new Vector3d(10, 64, 10), 0.0f, 0.0f, Map.of(), Map.of()),
                        new ActorDTO("villain", "virtual", "Necromancer", new Vector3d(20, 64, 20), 180.0f, 0.0f, Map.of("main_hand", "DIAMOND_SWORD"), Map.of())
                ),
                List.of(),
                Map.of()
        );

        SceneActorSession session = new SceneActorSession(scene, List::of, new SkinCacheService(), null);

        assertThat(session.isActive()).isTrue();
        assertThat(session.activeActors()).hasSize(2);

        Optional<Actor> heroOpt = session.resolve("hero");
        assertThat(heroOpt).isPresent();
        Actor hero = heroOpt.get();
        assertThat(hero.asMovable()).isPresent();
        assertThat(hero.asMovable().get().position()).isEqualTo(new Vector3d(10, 64, 10));

        Optional<Actor> villainOpt = session.resolve("villain");
        assertThat(villainOpt).isPresent();
        Actor villain = villainOpt.get();
        assertThat(villain.asEquippable()).isPresent();
        assertThat(villain.asEquippable().get().getEquipment(EquipmentSlot.MAIN_HAND)).isEqualTo("DIAMOND_SWORD");

        // Test capabilities on virtual actor
        villain.asAnimatable().ifPresent(a -> {
            a.setPose(ActorPose.SWIMMING);
            assertThat(a.pose()).isEqualTo(ActorPose.SWIMMING);
            a.triggerAction(ActorAction.SWING_MAIN_HAND);
        });

        // Cleanup
        session.cleanup();
        assertThat(session.isActive()).isFalse();
        assertThat(hero.isValid()).isFalse();
        assertThat(villain.isValid()).isFalse();
        assertThat(session.resolve("hero")).isEmpty();
    }
}
