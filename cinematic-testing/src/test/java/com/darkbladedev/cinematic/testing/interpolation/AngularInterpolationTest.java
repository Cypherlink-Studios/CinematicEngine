package com.darkbladedev.cinematic.testing.interpolation;

import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class AngularInterpolationTest {
    @Test
    void yawInterpolationTakesShortestPathAcrossZero() {
        CameraTrack track = new CameraTrack(
                "yaw-wrap",
                List.of(
                        new CameraFrame(0L, new Vector3d(), 350.0F, 0.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(), 10.0F, 0.0F, 70.0F, new LinearInterpolator())
                )
        );

        CameraState midpoint = evaluateAtTick(track, 5L);
        float normalizedMidYaw = normalize(midpoint.yaw());
        assertThat(normalizedMidYaw).isCloseTo(0.0F, offset(NumericTolerance.FLOAT_EPSILON));
        assertThat(Math.abs(midpoint.yaw() - 350.0F)).isLessThan(20.0F);
    }

    @Test
    void pitchInterpolationAvoidsUnnecessaryFullRotations() {
        CameraTrack track = new CameraTrack(
                "pitch-wrap",
                List.of(
                        new CameraFrame(0L, new Vector3d(), 0.0F, 10.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(), 0.0F, 350.0F, 70.0F, new LinearInterpolator())
                )
        );

        CameraState midpoint = evaluateAtTick(track, 5L);
        float normalizedMidPitch = normalize(midpoint.pitch());
        assertThat(normalizedMidPitch).isCloseTo(0.0F, offset(NumericTolerance.FLOAT_EPSILON));
        assertThat(Math.abs(midpoint.pitch() - 10.0F)).isLessThan(20.0F);
    }

    private CameraState evaluateAtTick(CameraTrack track, long tick) {
        AtomicReference<CameraState> stateReference = new AtomicReference<>();
        CameraOutput output = stateReference::set;
        ServiceTimelineContext context = new ServiceTimelineContext(tick, Map.of(CameraOutput.class, output));
        track.evaluate(tick, context);
        return stateReference.get();
    }

    private float normalize(float degrees) {
        float normalized = degrees % 360.0F;
        if (normalized < 0.0F) {
            normalized += 360.0F;
        }
        return normalized;
    }
}
