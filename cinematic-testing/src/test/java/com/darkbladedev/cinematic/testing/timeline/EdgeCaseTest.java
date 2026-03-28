package com.darkbladedev.cinematic.testing.timeline;

import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import com.darkbladedev.cinematic.testing.support.TestTimelineRunner;
import com.darkbladedev.cinematic.testing.support.VectorAssertions;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class EdgeCaseTest {
    @Test
    void cameraTrackWithTwoKeyframesAtSameTickIsStable() {
        CameraTrack track = new CameraTrack(
                "same-tick",
                List.of(
                        new CameraFrame(5L, new Vector3d(0.0D, 64.0D, 0.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(5L, new Vector3d(10.0D, 65.0D, 2.0D), 45.0F, 10.0F, 90.0F, new EaseInOutInterpolator())
                )
        );

        CameraState state = evaluateAtTick(track, 5L);
        VectorAssertions.assertVectorEquals(new Vector3d(10.0D, 65.0D, 2.0D), state.position(), NumericTolerance.DOUBLE_EPSILON);
        assertThat(state.yaw()).isCloseTo(45.0F, offset(NumericTolerance.FLOAT_EPSILON));
    }

    @Test
    void cameraTrackRequiresAtLeastOneFrame() {
        assertThatThrownBy(() -> new CameraTrack("empty-track", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires at least one frame");
    }

    @Test
    void sceneWithZeroDurationDoesNotEvaluateTracksOutOfRange() {
        RecordingTrack track = new RecordingTrack("zero-duration");
        Scene scene = new Scene("scene-zero", 0L, List.of(track));
        TestTimelineRunner runner = new TestTimelineRunner();

        runner.play(scene, (currentScene, localTick) -> TestTimelineRunner.emptyContext(localTick));
        runner.advanceTicks(2L);
        runner.stop();

        assertThat(track.evaluatedTicks()).isEmpty();
    }

    @Test
    void rotationInterpolationAcrossThreeSixtyBoundaryUsesShortestPath() {
        CameraTrack track = new CameraTrack(
                "rotation-boundary",
                List.of(
                        new CameraFrame(0L, new Vector3d(), 350.0F, 0.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(), 10.0F, 0.0F, 70.0F, new LinearInterpolator())
                )
        );

        CameraState state = evaluateAtTick(track, 5L);
        float normalizedYaw = normalize(state.yaw());
        assertThat(normalizedYaw).isCloseTo(0.0F, offset(NumericTolerance.FLOAT_EPSILON));
    }

    private CameraState evaluateAtTick(CameraTrack track, long tick) {
        AtomicReference<CameraState> stateReference = new AtomicReference<>();
        CameraOutput output = stateReference::set;
        ServiceTimelineContext context = new ServiceTimelineContext(tick, Map.of(CameraOutput.class, output));
        track.evaluate(tick, context);
        return stateReference.get();
    }

    private float normalize(float value) {
        float normalized = value % 360.0F;
        if (normalized < 0.0F) {
            normalized += 360.0F;
        }
        return normalized;
    }

    private static final class RecordingTrack implements Track {
        private final String id;
        private final List<Long> evaluatedTicks;

        private RecordingTrack(String id) {
            this.id = id;
            this.evaluatedTicks = new ArrayList<>();
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public long startTick() {
            return 0L;
        }

        @Override
        public long endTick() {
            return 0L;
        }

        @Override
        public void evaluate(long tick, TimelineContext context) {
            evaluatedTicks.add(tick);
        }

        private List<Long> evaluatedTicks() {
            return List.copyOf(evaluatedTicks);
        }
    }
}
