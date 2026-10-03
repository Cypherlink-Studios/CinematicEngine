package com.darkbladedev.cinematic.testing.camera;

import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraPathMode;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.camera.targeting.ActorPositionLookup;
import com.darkbladedev.cinematic.camera.targeting.ActorTargetStrategy;
import com.darkbladedev.cinematic.camera.targeting.StaticTargetStrategy;
import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.testing.support.TestTimelineRunner;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CameraDynamicsEndToEndTest {

    @Test
    void endToEndSplineCameraTimelineExecutionProducesSmoothOutputs() {
        List<CameraState> outputStates = new ArrayList<>();
        CameraOutput output = outputStates::add;

        CameraTrack track = new CameraTrack(
                "main-camera",
                List.of(
                        new CameraFrame(0L, new Vector3d(0.0D, 64.0D, 0.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(10.0D, 70.0D, 5.0D), 0.0F, 0.0F, 70.0F, new EaseInOutInterpolator()),
                        new CameraFrame(20L, new Vector3d(25.0D, 68.0D, -10.0D), 0.0F, 0.0F, 70.0F, new EaseInOutInterpolator()),
                        new CameraFrame(30L, new Vector3d(40.0D, 64.0D, 0.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator())
                ),
                CameraPathMode.CATMULL_ROM,
                new StaticTargetStrategy(new Vector3d(20.0D, 64.0D, 0.0D))
        );

        Scene scene = new Scene("spline-scene", 30L, List.of(track));
        TestTimelineRunner runner = new TestTimelineRunner();

        runner.play(scene, (currentScene, localTick) -> new TimelineContext() {
            @Override
            public long currentTick() {
                return localTick;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<T> getService(Class<T> serviceType) {
                if (serviceType.equals(CameraOutput.class)) {
                    return Optional.of((T) output);
                }
                return Optional.empty();
            }
        });

        runner.advanceTicks(31L);
        runner.stop();

        assertThat(outputStates).hasSize(30);

        // Verify continuity: maximum step distance between ticks is bounded
        for (int i = 1; i < outputStates.size(); i++) {
            CameraState prev = outputStates.get(i - 1);
            CameraState curr = outputStates.get(i);

            double distance = curr.position().distance(prev.position());
            assertThat(distance).isLessThan(4.0D);

            // Yaw and pitch must not be NaN
            assertThat(Float.isNaN(curr.yaw())).isFalse();
            assertThat(Float.isNaN(curr.pitch())).isFalse();
        }
    }

    @Test
    void endToEndActorTrackingTracksMovingTargetAcrossScene() {
        List<CameraState> outputStates = new ArrayList<>();
        CameraOutput output = outputStates::add;

        Map<Long, Vector3d> actorPositions = new HashMap<>();
        for (long tick = 0; tick <= 20; tick++) {
            actorPositions.put(tick, new Vector3d(0.0D, 64.0D, tick * 2.0D)); // Actor moving South
        }

        CameraTrack track = new CameraTrack(
                "actor-tracking-cam",
                List.of(
                        new CameraFrame(0L, new Vector3d(10.0D, 64.0D, 0.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(20L, new Vector3d(10.0D, 64.0D, 40.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator())
                ),
                CameraPathMode.CATMULL_ROM,
                new ActorTargetStrategy("moving-actor")
        );

        Scene scene = new Scene("actor-scene", 20L, List.of(track));
        TestTimelineRunner runner = new TestTimelineRunner();

        runner.play(scene, (currentScene, localTick) -> new TimelineContext() {
            @Override
            public long currentTick() {
                return localTick;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<T> getService(Class<T> serviceType) {
                if (serviceType.equals(CameraOutput.class)) {
                    return Optional.of((T) output);
                }
                if (serviceType.equals(ActorPositionLookup.class)) {
                    ActorPositionLookup lookup = actorId -> Optional.ofNullable(actorPositions.get(localTick));
                    return Optional.of((T) lookup);
                }
                return Optional.empty();
            }
        });

        runner.advanceTicks(21L);
        runner.stop();

        assertThat(outputStates).hasSize(20);
        for (CameraState state : outputStates) {
            assertThat(Float.isNaN(state.yaw())).isFalse();
            assertThat(Float.isNaN(state.pitch())).isFalse();
        }
    }
}
