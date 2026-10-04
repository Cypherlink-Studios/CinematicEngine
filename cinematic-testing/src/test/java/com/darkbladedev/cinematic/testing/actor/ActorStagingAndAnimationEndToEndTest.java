package com.darkbladedev.cinematic.testing.actor;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.adapters.actor.SceneActorSession;
import com.darkbladedev.cinematic.adapters.actor.SkinCacheService;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.mapper.DefaultDslComponents;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.SceneMapper;
import com.darkbladedev.cinematic.dsl.parser.SceneParser;
import com.darkbladedev.cinematic.dsl.parser.SnakeYamlSceneParser;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.runtime.ActorResolver;
import com.darkbladedev.cinematic.dsl.validator.SceneDtoValidator;
import com.darkbladedev.cinematic.testing.support.TestTimelineRunner;
import org.joml.Vector3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ActorStagingAndAnimationEndToEndTest {

    @Test
    @DisplayName("End-to-end full scene execution with actors staging, camera look-at, motion splines, and action dispatch")
    void fullSceneExecutionWithStagingAndMultiLayerTracks() throws IOException {
        String yamlContent = """
                id: epic_ambush
                duration: 40
                
                actors:
                  - id: hero
                    type: self_clone
                    initial_position: [0.0, 64.0, 0.0]
                    initial_yaw: 0.0
                    initial_equipment:
                      main_hand: IRON_SWORD
                  - id: assassin
                    type: virtual
                    skin: ShadowRogue
                    initial_position: [20.0, 68.0, 10.0]
                    initial_yaw: 180.0
                    initial_equipment:
                      main_hand: BOW
                
                tracks:
                  - type: camera
                    id: cam_track
                    data:
                      path_mode: linear
                      look_at:
                        mode: actor
                        target_actor: hero
                    keyframes:
                      - tick: 0
                        position: [-10.0, 68.0, 0.0]
                      - tick: 40
                        position: [-5.0, 68.0, 20.0]
                
                  - type: actor_motion
                    id: hero_motion
                    data:
                      actor_id: hero
                      path_mode: spline
                      heading: tangent
                    keyframes:
                      - tick: 0
                        position: [0.0, 64.0, 0.0]
                      - tick: 20
                        position: [5.0, 64.0, 10.0]
                      - tick: 40
                        position: [10.0, 64.0, 20.0]
                
                  - type: actor_action
                    id: hero_actions
                    data:
                      actor_id: hero
                    keyframes:
                      - tick: 0
                        pose: STANDING
                      - tick: 25
                        pose: CROUCHING
                        item_usage: BLOCKING
                      - tick: 35
                        action: HURT
                
                  - type: actor_action
                    id: assassin_actions
                    data:
                      actor_id: assassin
                    keyframes:
                      - tick: 10
                        action: SWING_MAIN_HAND
                        equipment:
                          main_hand: DIAMOND_SWORD
                """;

        Path sceneFile = Files.createTempFile("epic-scene-", ".yml");
        Files.writeString(sceneFile, yamlContent);

        // 1. Parse and validate scene
        SceneParser parser = new SnakeYamlSceneParser();
        SceneDTO dto = parser.parse(sceneFile);

        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator validator = DefaultDslComponents.validator(trackRegistry, interpolatorRegistry);
        validator.validate(dto);

        SceneMapper mapper = DefaultDslComponents.mapper(validator, trackRegistry);
        Scene scene = mapper.map(dto);

        // 2. Initialize actor session
        SceneActorSession actorSession = new SceneActorSession(dto, List::of, new SkinCacheService(), null);
        assertThat(actorSession.isActive()).isTrue();
        assertThat(actorSession.activeActors()).hasSize(2);

        Actor heroActor = actorSession.resolve("hero").orElseThrow();
        Actor assassinActor = actorSession.resolve("assassin").orElseThrow();

        assertThat(heroActor.asEquippable().orElseThrow().getEquipment(EquipmentSlot.MAIN_HAND)).isEqualTo("IRON_SWORD");
        assertThat(assassinActor.asEquippable().orElseThrow().getEquipment(EquipmentSlot.MAIN_HAND)).isEqualTo("BOW");

        // 3. Run execution across the timeline
        List<CameraState> recordedCameraStates = new ArrayList<>();
        CameraOutput cameraOutput = recordedCameraStates::add;

        TestTimelineRunner runner = new TestTimelineRunner();
        runner.play(scene, (currentScene, localTick) -> new TimelineContext() {
            @Override
            public long currentTick() {
                return localTick;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<T> getService(Class<T> serviceType) {
                if (serviceType == CameraOutput.class) {
                    return Optional.of((T) cameraOutput);
                }
                if (serviceType == ActorResolver.class) {
                    return Optional.of((T) actorSession);
                }
                if (serviceType == com.darkbladedev.cinematic.camera.targeting.ActorPositionLookup.class) {
                    return Optional.of((T) actorSession);
                }
                return Optional.empty();
            }
        });

        // 4. Verify playback results
        runner.advanceTicks(41);

        // Hero motion: moved from [0, 64, 0] towards [10, 64, 20]
        Vector3d finalHeroPos = heroActor.asMovable().orElseThrow().position();
        assertThat(finalHeroPos.x).isGreaterThan(5.0);
        assertThat(finalHeroPos.z).isGreaterThan(10.0);

        // Hero actions: crouching, blocking, received hurt
        assertThat(heroActor.asAnimatable().orElseThrow().pose()).isEqualTo(ActorPose.CROUCHING);
        assertThat(heroActor.asAnimatable().orElseThrow().itemUsage()).isEqualTo(ItemUsageState.BLOCKING);

        // Assassin: swapped weapon to DIAMOND_SWORD
        assertThat(assassinActor.asEquippable().orElseThrow().getEquipment(EquipmentSlot.MAIN_HAND)).isEqualTo("DIAMOND_SWORD");

        // Camera: recorded all frames and tracked the hero
        assertThat(recordedCameraStates).hasSize(40);
        CameraState lastCamera = recordedCameraStates.getLast();
        assertThat(lastCamera.position().z).isGreaterThan(15.0);
        assertThat(lastCamera.yaw()).isNotZero();

        // 5. Cleanup
        actorSession.cleanup();
        assertThat(actorSession.isActive()).isFalse();
        assertThat(heroActor.isValid()).isFalse();
        assertThat(assassinActor.isValid()).isFalse();
    }
}
