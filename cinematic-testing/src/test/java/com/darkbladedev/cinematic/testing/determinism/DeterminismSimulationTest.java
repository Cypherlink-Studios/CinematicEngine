package com.darkbladedev.cinematic.testing.determinism;

import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.testing.support.FrameSnapshot;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import com.darkbladedev.cinematic.testing.support.TestTimelineRunner;
import com.darkbladedev.cinematic.testing.support.VectorAssertions;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class DeterminismSimulationTest {
    @Test
    void fullSimulationProducesIdenticalSnapshotsAcrossRuns() {
        Scene scene = deterministicScene();
        List<FrameSnapshot> firstRun = runScene(scene);
        List<FrameSnapshot> secondRun = runScene(scene);

        assertThat(firstRun).hasSameSizeAs(secondRun);
        for (int index = 0; index < firstRun.size(); index++) {
            FrameSnapshot first = firstRun.get(index);
            FrameSnapshot second = secondRun.get(index);
            assertThat(first.tick()).isEqualTo(second.tick());
            VectorAssertions.assertVectorEquals(first.position(), second.position(), NumericTolerance.DOUBLE_EPSILON);
            assertThat(first.yaw()).isCloseTo(second.yaw(), offset(NumericTolerance.FLOAT_EPSILON));
            assertThat(first.pitch()).isCloseTo(second.pitch(), offset(NumericTolerance.FLOAT_EPSILON));
            assertThat(first.fov()).isCloseTo(second.fov(), offset(NumericTolerance.FLOAT_EPSILON));
        }
    }

    @Test
    void simulationDoesNotDependOnWallClockTiming() throws InterruptedException {
        Scene scene = deterministicScene();
        List<FrameSnapshot> normalRun = runSceneWithOptionalDelay(scene, false);
        List<FrameSnapshot> delayedRun = runSceneWithOptionalDelay(scene, true);

        assertThat(delayedRun).hasSameSizeAs(normalRun);
        for (int index = 0; index < normalRun.size(); index++) {
            FrameSnapshot expected = normalRun.get(index);
            FrameSnapshot actual = delayedRun.get(index);
            assertThat(actual.tick()).isEqualTo(expected.tick());
            VectorAssertions.assertVectorEquals(expected.position(), actual.position(), NumericTolerance.DOUBLE_EPSILON);
            assertThat(actual.yaw()).isCloseTo(expected.yaw(), offset(NumericTolerance.FLOAT_EPSILON));
            assertThat(actual.pitch()).isCloseTo(expected.pitch(), offset(NumericTolerance.FLOAT_EPSILON));
            assertThat(actual.fov()).isCloseTo(expected.fov(), offset(NumericTolerance.FLOAT_EPSILON));
        }
    }

    private Scene deterministicScene() {
        CameraTrack cameraTrack = new CameraTrack(
                "deterministic-camera",
                List.of(
                        new CameraFrame(0L, new Vector3d(0.0D, 65.0D, 0.0D), 350.0F, 5.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(20L, new Vector3d(20.0D, 70.0D, -10.0D), 20.0F, -10.0F, 90.0F, new EaseInOutInterpolator()),
                        new CameraFrame(35L, new Vector3d(35.0D, 72.0D, -15.0D), 80.0F, -20.0F, 95.0F, new LinearInterpolator())
                )
        );
        return new Scene("determinism-scene", 35L, List.of(cameraTrack));
    }

    private List<FrameSnapshot> runScene(Scene scene) {
        try {
            return runSceneWithOptionalDelay(scene, false);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new AssertionError(interruptedException);
        }
    }

    private List<FrameSnapshot> runSceneWithOptionalDelay(Scene scene, boolean withDelay) throws InterruptedException {
        TestTimelineRunner runner = new TestTimelineRunner();
        List<FrameSnapshot> snapshots = new ArrayList<>();
        runner.play(scene, (currentScene, localTick) -> new com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext(
                localTick,
                Map.of(CameraOutput.class, (CameraOutput) state -> snapshots.add(FrameSnapshot.of(runner.currentTick(), state)))
        ));
        for (int tick = 0; tick < 36; tick++) {
            if (withDelay && tick % 7 == 0) {
                Thread.sleep(1L);
            }
            runner.advanceTicks(1L);
        }
        runner.stop();
        return List.copyOf(snapshots);
    }
}
