package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.Movable;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class ActorMotionTrackTest {

    @Test
    @DisplayName("ActorMotionTrack linearly interpolates position and tangent yaw")
    void linearInterpolationAndTangentYaw() {
        MockMovableActor actor = new MockMovableActor("hero");
        ActorResolver resolver = id -> "hero".equals(id) ? Optional.of(actor) : Optional.empty();
        TimelineContext context = new MockTimelineContext(resolver);

        ActorMotionTrack track = new ActorMotionTrack(
                "motion1",
                "hero",
                "linear",
                "tangent",
                null,
                List.of(
                        new ActorMotionFrame(0L, new Vector3d(0.0, 64.0, 0.0), null, null, null, new LinearInterpolator()),
                        new ActorMotionFrame(100L, new Vector3d(100.0, 64.0, 0.0), null, null, null, new LinearInterpolator())
                )
        );

        track.evaluate(0L, context);
        assertThat(actor.pos.x).isEqualTo(0.0, offset(1e-4));

        track.evaluate(50L, context);
        assertThat(actor.pos.x).isEqualTo(50.0, offset(1e-4));
        assertThat(actor.pos.y).isEqualTo(64.0, offset(1e-4));
        // Moving along +X axis: direction is (100, 0, 0), yaw = atan2(-100, 0) = -90 degrees
        assertThat(actor.yaw).isEqualTo(-90.0f, offset(0.5f));

        track.evaluate(100L, context);
        assertThat(actor.pos.x).isEqualTo(100.0, offset(1e-4));
    }

    @Test
    @DisplayName("ActorMotionTrack spline path evaluates smooth Catmull-Rom curve")
    void splinePathEvaluation() {
        MockMovableActor actor = new MockMovableActor("guard");
        ActorResolver resolver = id -> "guard".equals(id) ? Optional.of(actor) : Optional.empty();
        TimelineContext context = new MockTimelineContext(resolver);

        ActorMotionTrack track = new ActorMotionTrack(
                "motion_spline",
                "guard",
                "spline",
                "tangent",
                null,
                List.of(
                        new ActorMotionFrame(0L, new Vector3d(0.0, 64.0, 0.0), null, null, null, new LinearInterpolator()),
                        new ActorMotionFrame(50L, new Vector3d(10.0, 65.0, 10.0), null, null, null, new LinearInterpolator()),
                        new ActorMotionFrame(100L, new Vector3d(20.0, 64.0, 0.0), null, null, null, new LinearInterpolator())
                )
        );

        track.evaluate(25L, context);
        assertThat(actor.pos.x).isBetween(0.0, 15.0);
        assertThat(actor.pos.y).isBetween(64.0, 65.5);

        track.evaluate(50L, context);
        assertThat(actor.pos.x).isEqualTo(10.0, offset(0.1));
        assertThat(actor.pos.y).isEqualTo(65.0, offset(0.1));
        assertThat(actor.pos.z).isEqualTo(10.0, offset(0.1));
    }

    private static class MockMovableActor implements Actor, Movable {
        final String name;
        final UUID id = UUID.randomUUID();
        Vector3d pos = new Vector3d();
        float yaw;
        float pitch;

        MockMovableActor(String name) { this.name = name; }
        @Override public UUID id() { return id; }
        @Override public void teleport(Vector3d position) { this.pos = new Vector3d(position); }
        @Override public void rotate(float yaw, float pitch) { this.yaw = yaw; this.pitch = pitch; }
        @Override public boolean isValid() { return true; }
        @Override public Vector3d position() { return pos; }
        @Override public void moveTo(Vector3d position, boolean instant) { this.pos = new Vector3d(position); }
        @Override public float yaw() { return yaw; }
        @Override public float pitch() { return pitch; }
    }

    private static class MockTimelineContext implements TimelineContext {
        final ActorResolver resolver;
        MockTimelineContext(ActorResolver resolver) { this.resolver = resolver; }
        @Override public long currentTick() { return 0; }
        @SuppressWarnings("unchecked")
        @Override public <T> Optional<T> getService(Class<T> serviceType) {
            if (serviceType == ActorResolver.class) return Optional.of((T) resolver);
            return Optional.empty();
        }
    }
}
