package com.darkbladedev.cinematic.testing.camera;

import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import com.darkbladedev.cinematic.testing.support.VectorAssertions;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class CameraTrackMotionTest {
    @Test
    void cameraMovementIsContinuousAcrossTicks() {
        CameraTrack track = new CameraTrack(
                "camera-motion",
                List.of(
                        new CameraFrame(0L, new Vector3d(0.0D, 64.0D, 0.0D), 0.0F, 5.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(20L, new Vector3d(20.0D, 70.0D, -10.0D), 90.0F, -10.0F, 90.0F, new EaseInOutInterpolator())
                )
        );

        CameraState previous = null;
        for (long tick = 0; tick <= 20; tick++) {
            CameraState current = evaluateAtTick(track, tick);
            if (previous != null) {
                assertThat(current.position().distance(previous.position())).isLessThanOrEqualTo(2.0D);
                assertThat(Math.abs(current.yaw() - previous.yaw())).isLessThanOrEqualTo(10.0F);
                assertThat(Math.abs(current.pitch() - previous.pitch())).isLessThanOrEqualTo(5.0F);
            }
            previous = current;
        }
    }

    @Test
    void cameraReachesFinalKeyframeExactlyAtEnd() {
        CameraTrack track = new CameraTrack(
                "camera-final-state",
                List.of(
                        new CameraFrame(0L, new Vector3d(1.0D, 2.0D, 3.0D), 15.0F, 5.0F, 80.0F, new LinearInterpolator()),
                        new CameraFrame(12L, new Vector3d(12.0D, 20.0D, -3.0D), 120.0F, -30.0F, 95.0F, new LinearInterpolator())
                )
        );

        CameraState finalState = evaluateAtTick(track, 12L);
        VectorAssertions.assertVectorEquals(new Vector3d(12.0D, 20.0D, -3.0D), finalState.position(), NumericTolerance.DOUBLE_EPSILON);
        assertThat(finalState.yaw()).isCloseTo(120.0F, offset(NumericTolerance.FLOAT_EPSILON));
        assertThat(finalState.pitch()).isCloseTo(-30.0F, offset(NumericTolerance.FLOAT_EPSILON));
        assertThat(finalState.fov()).isCloseTo(95.0F, offset(NumericTolerance.FLOAT_EPSILON));
    }

    @Test
    void fovInterpolationIsProgressiveWithoutJumps() {
        CameraTrack track = new CameraTrack(
                "camera-fov",
                List.of(
                        new CameraFrame(0L, new Vector3d(), 0.0F, 0.0F, 60.0F, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(), 0.0F, 0.0F, 110.0F, new LinearInterpolator())
                )
        );

        float previousFov = Float.NEGATIVE_INFINITY;
        for (long tick = 0; tick <= 10; tick++) {
            float currentFov = evaluateAtTick(track, tick).fov();
            assertThat(currentFov).isGreaterThanOrEqualTo(previousFov);
            if (previousFov > Float.NEGATIVE_INFINITY) {
                assertThat(currentFov - previousFov).isLessThanOrEqualTo(5.1F);
            }
            previousFov = currentFov;
        }
    }

    private CameraState evaluateAtTick(CameraTrack track, long tick) {
        AtomicReference<CameraState> stateReference = new AtomicReference<>();
        CameraOutput output = stateReference::set;
        ServiceTimelineContext context = new ServiceTimelineContext(tick, Map.of(CameraOutput.class, output));
        track.evaluate(tick, context);
        return stateReference.get();
    }
}
