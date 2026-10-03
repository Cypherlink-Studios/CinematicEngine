package com.darkbladedev.cinematic.testing.camera;

import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraPathMode;
import com.darkbladedev.cinematic.camera.CameraState;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.camera.targeting.*;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class LookAtStrategyTest {

    @Test
    void fixedAnglesStrategyPreservesFallbackOrientation() {
        FixedAnglesStrategy strategy = FixedAnglesStrategy.INSTANCE;
        CameraOrientation orientation = strategy.calculateOrientation(
                0L,
                new Vector3d(0, 64, 0),
                new Vector3d(1, 0, 0),
                45.0F,
                -15.0F,
                null
        );

        assertThat(orientation.yaw()).isEqualTo(45.0F);
        assertThat(orientation.pitch()).isEqualTo(-15.0F);
    }

    @Test
    void staticTargetStrategyComputesCorrectDirection() {
        // Camera at (0, 64, 0), looking at target at (0, 64, 10) -> exactly South (yaw = 0, pitch = 0)
        StaticTargetStrategy southStrategy = new StaticTargetStrategy(new Vector3d(0, 64, 10));
        CameraOrientation south = southStrategy.calculateOrientation(
                0L,
                new Vector3d(0, 64, 0),
                null,
                90.0F,
                0.0F,
                null
        );
        assertThat(south.yaw()).isCloseTo(0.0F, offset(0.01F));
        assertThat(south.pitch()).isCloseTo(0.0F, offset(0.01F));

        // Camera looking at (10, 64, 0) -> exactly East (yaw = -90)
        StaticTargetStrategy eastStrategy = new StaticTargetStrategy(new Vector3d(10, 64, 0));
        CameraOrientation east = eastStrategy.calculateOrientation(
                0L,
                new Vector3d(0, 64, 0),
                null,
                0.0F,
                0.0F,
                null
        );
        assertThat(east.yaw()).isCloseTo(-90.0F, offset(0.01F));

        // Camera looking straight down at (0, 54, 0) -> pitch = 90
        StaticTargetStrategy downStrategy = new StaticTargetStrategy(new Vector3d(0, 54, 0));
        CameraOrientation down = downStrategy.calculateOrientation(
                0L,
                new Vector3d(0, 64, 0),
                null,
                0.0F,
                0.0F,
                null
        );
        assertThat(down.pitch()).isCloseTo(90.0F, offset(0.01F));
    }

    @Test
    void actorTargetStrategyResolvesDynamicActorPosition() {
        ActorPositionLookup lookup = actorId -> {
            if ("npc-boss".equals(actorId)) {
                return Optional.of(new Vector3d(0.0D, 64.0D, -10.0D)); // North (yaw = 180)
            }
            return Optional.empty();
        };

        TimelineContext context = new ServiceTimelineContext(0L, Map.of(ActorPositionLookup.class, lookup));
        ActorTargetStrategy strategy = new ActorTargetStrategy("npc-boss");

        CameraOrientation orientation = strategy.calculateOrientation(
                0L,
                new Vector3d(0.0D, 64.0D, 0.0D),
                null,
                0.0F,
                0.0F,
                context
        );

        assertThat(Math.abs(orientation.yaw())).isCloseTo(180.0F, offset(0.01F));
    }

    @Test
    void actorTargetStrategyFallsBackWhenActorNotFound() {
        TimelineContext context = new ServiceTimelineContext(0L, Map.of());
        ActorTargetStrategy strategy = new ActorTargetStrategy("unknown-actor");

        CameraOrientation orientation = strategy.calculateOrientation(
                0L,
                new Vector3d(0.0D, 64.0D, 0.0D),
                null,
                33.0F,
                -12.0F,
                context
        );

        assertThat(orientation.yaw()).isEqualTo(33.0F);
        assertThat(orientation.pitch()).isEqualTo(-12.0F);
    }

    @Test
    void velocityForwardStrategyFollowsMotionDirection() {
        VelocityForwardStrategy strategy = VelocityForwardStrategy.INSTANCE;

        // Moving South (+Z)
        CameraOrientation south = strategy.calculateOrientation(
                0L,
                new Vector3d(0, 64, 0),
                new Vector3d(0, 0, 5),
                99.0F,
                99.0F,
                null
        );
        assertThat(south.yaw()).isCloseTo(0.0F, offset(0.01F));

        // Stationary velocity falls back
        CameraOrientation stationary = strategy.calculateOrientation(
                0L,
                new Vector3d(0, 64, 0),
                new Vector3d(0, 0, 0),
                42.0F,
                11.0F,
                null
        );
        assertThat(stationary.yaw()).isEqualTo(42.0F);
        assertThat(stationary.pitch()).isEqualTo(11.0F);
    }

    @Test
    void cameraTrackWithCatmullRomAndLookAtEvaluatesSmoothly() {
        CameraTrack track = new CameraTrack(
                "spline-camera",
                List.of(
                        new CameraFrame(0L, new Vector3d(0, 64, 0), 0, 0, 70, new LinearInterpolator()),
                        new CameraFrame(10L, new Vector3d(10, 66, 5), 0, 0, 70, new LinearInterpolator()),
                        new CameraFrame(20L, new Vector3d(25, 68, -10), 0, 0, 70, new LinearInterpolator()),
                        new CameraFrame(30L, new Vector3d(40, 64, 0), 0, 0, 70, new LinearInterpolator())
                ),
                CameraPathMode.CATMULL_ROM,
                new StaticTargetStrategy(new Vector3d(20, 64, 0))
        );

        TimelineContext context = new ServiceTimelineContext(10L, Map.of());
        CameraState stateAt10 = track.evaluateState(10L, context);

        assertThat(stateAt10.position().x).isCloseTo(10.0D, offset(0.01D));
        assertThat(stateAt10.position().y).isCloseTo(66.0D, offset(0.01D));
        assertThat(stateAt10.position().z).isCloseTo(5.0D, offset(0.01D));
        // Orientation should point toward (20, 64, 0)
        assertThat(stateAt10.pitch()).isGreaterThan(0.0F); // Looking downward (y=66 down to y=64)
    }
}
