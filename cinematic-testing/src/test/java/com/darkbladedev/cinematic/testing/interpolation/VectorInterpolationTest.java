package com.darkbladedev.cinematic.testing.interpolation;

import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import com.darkbladedev.cinematic.testing.support.VectorAssertions;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class VectorInterpolationTest {
    @Test
    void vectorInterpolationProgressesWithoutAbruptJumps() {
        CameraTrack track = new CameraTrack(
                "vector-track",
                java.util.List.of(
                        new CameraFrame(0L, new Vector3d(0.0D, 0.0D, 0.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(10.0D, 0.0D, 0.0D), 0.0F, 0.0F, 70.0F, new LinearInterpolator())
                )
        );

        double previousDistance = -1.0D;
        Vector3d previousPosition = null;
        for (long tick = 0; tick <= 10; tick++) {
            CameraState state = evaluateAtTick(track, tick);
            Vector3d position = state.position();
            double distance = position.distance(0.0D, 0.0D, 0.0D);
            assertThat(distance).isGreaterThanOrEqualTo(previousDistance - NumericTolerance.DOUBLE_EPSILON);
            if (previousPosition != null) {
                double stepDistance = position.distance(previousPosition);
                assertThat(stepDistance).isLessThanOrEqualTo(1.0D + NumericTolerance.DOUBLE_EPSILON);
            }
            previousDistance = distance;
            previousPosition = new Vector3d(position);
        }

        VectorAssertions.assertVectorEquals(new Vector3d(10.0D, 0.0D, 0.0D), evaluateAtTick(track, 10).position(), NumericTolerance.DOUBLE_EPSILON);
    }

    private CameraState evaluateAtTick(CameraTrack track, long tick) {
        AtomicReference<CameraState> stateReference = new AtomicReference<>();
        CameraOutput output = stateReference::set;
        ServiceTimelineContext context = new ServiceTimelineContext(tick, Map.of(CameraOutput.class, output));
        track.evaluate(tick, context);
        return stateReference.get();
    }
}
