package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ActorTrack implements Track {
    private final String id;
    private final String actorId;
    private final List<ActorFrame> frames;

    public ActorTrack(String id, String actorId, List<ActorFrame> frames) {
        this.id = Objects.requireNonNull(id, "id");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.frames = Objects.requireNonNull(frames, "frames")
                .stream()
                .sorted(Comparator.comparingLong(ActorFrame::tick))
                .toList();
        if (this.frames.isEmpty()) {
            throw new IllegalArgumentException("ActorTrack requiere al menos un keyframe");
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long startTick() {
        return frames.getFirst().tick();
    }

    @Override
    public long endTick() {
        return frames.getLast().tick();
    }

    @Override
    public void evaluate(long tick, TimelineContext context) {
        ActorResolver resolver = context.getService(ActorResolver.class).orElse(null);
        if (resolver == null) {
            return;
        }
        Actor actor = resolver.resolve(actorId).orElse(null);
        if (actor == null || !actor.isValid()) {
            return;
        }
        EvaluatedActorFrame state = evaluateState(tick);
        actor.teleport(state.position());
        if (state.lookAt() != null) {
            Orientation orientation = orientationTo(state.position(), state.lookAt());
            actor.rotate(orientation.yaw(), orientation.pitch());
        }
    }

    private EvaluatedActorFrame evaluateState(long tick) {
        if (frames.size() == 1 || tick <= frames.getFirst().tick()) {
            return toState(frames.getFirst());
        }
        if (tick >= frames.getLast().tick()) {
            return toState(frames.getLast());
        }
        for (int index = 0; index < frames.size() - 1; index++) {
            ActorFrame from = frames.get(index);
            ActorFrame to = frames.get(index + 1);
            if (tick >= from.tick() && tick <= to.tick()) {
                double span = (double) (to.tick() - from.tick());
                if (span <= 0.0D) {
                    return toState(to);
                }
                double progress = (tick - from.tick()) / span;
                double eased = to.interpolator().interpolate(progress);
                Vector3d interpolatedPosition = new Vector3d(from.position()).lerp(to.position(), eased);
                Vector3d interpolatedLookAt = interpolateLookAt(from.lookAt(), to.lookAt(), eased);
                return new EvaluatedActorFrame(interpolatedPosition, interpolatedLookAt);
            }
        }
        return toState(frames.getLast());
    }

    private EvaluatedActorFrame toState(ActorFrame frame) {
        return new EvaluatedActorFrame(new Vector3d(frame.position()), frame.lookAt() == null ? null : new Vector3d(frame.lookAt()));
    }

    private Vector3d interpolateLookAt(Vector3d from, Vector3d to, double progress) {
        if (from == null && to == null) {
            return null;
        }
        if (from == null) {
            return new Vector3d(to);
        }
        if (to == null) {
            return new Vector3d(from);
        }
        return new Vector3d(from).lerp(to, progress);
    }

    private Orientation orientationTo(Vector3d from, Vector3d target) {
        Vector3d direction = new Vector3d(target).sub(from);
        if (direction.lengthSquared() == 0.0D) {
            return new Orientation(0.0F, 0.0F);
        }
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float pitch = (float) Math.toDegrees(-Math.atan2(direction.y, horizontal));
        return new Orientation(yaw, pitch);
    }

    private record EvaluatedActorFrame(Vector3d position, Vector3d lookAt) {
    }

    private record Orientation(float yaw, float pitch) {
    }
}
