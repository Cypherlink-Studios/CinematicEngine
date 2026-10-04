package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.Movable;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.core.spline.CatmullRomSpline;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ActorMotionTrack implements Track {
    private final String id;
    private final String actorId;
    private final String pathMode;
    private final String heading;
    private final Vector3d globalLookAt;
    private final List<ActorMotionFrame> frames;
    private final CatmullRomSpline spline;

    public ActorMotionTrack(
            String id,
            String actorId,
            String pathMode,
            String heading,
            Vector3d globalLookAt,
            List<ActorMotionFrame> frames
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.pathMode = pathMode == null || pathMode.isBlank() ? "linear" : pathMode.toLowerCase();
        this.heading = heading == null || heading.isBlank() ? "tangent" : heading.toLowerCase();
        this.globalLookAt = globalLookAt == null ? null : new Vector3d(globalLookAt);
        this.frames = Objects.requireNonNull(frames, "frames")
                .stream()
                .sorted(Comparator.comparingLong(ActorMotionFrame::tick))
                .toList();

        if (this.frames.isEmpty()) {
            throw new IllegalArgumentException("ActorMotionTrack requiere al menos un keyframe");
        }

        if ("spline".equalsIgnoreCase(this.pathMode) && this.frames.size() >= 2) {
            List<Vector3d> points = this.frames.stream().map(ActorMotionFrame::position).toList();
            this.spline = new CatmullRomSpline(points);
        } else {
            this.spline = null;
        }
    }

    @Override
    public String id() {
        return id;
    }

    public String actorId() {
        return actorId;
    }

    public String pathMode() {
        return pathMode;
    }

    public String heading() {
        return heading;
    }

    public List<ActorMotionFrame> frames() {
        return frames;
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
        Movable movable = actor.asMovable().orElse(null);
        if (movable == null) {
            return;
        }

        MotionState state = evaluateState(tick);
        movable.moveTo(state.position(), false);
        movable.rotate(state.yaw(), state.pitch());
    }

    private MotionState evaluateState(long tick) {
        if (frames.size() == 1 || tick <= frames.getFirst().tick()) {
            return toState(frames.getFirst());
        }
        if (tick >= frames.getLast().tick()) {
            return toState(frames.getLast());
        }

        if (spline != null) {
            long start = frames.getFirst().tick();
            long end = frames.getLast().tick();
            double t = (double) (tick - start) / (double) (end - start);
            Vector3d pos = spline.evaluate(t);
            float yaw = 0.0f;
            float pitch = 0.0f;

            if (isTangentHeading()) {
                Vector3d tangent = spline.evaluateTangent(t);
                if (tangent.lengthSquared() > 1e-6D) {
                    yaw = (float) Math.toDegrees(Math.atan2(-tangent.x, tangent.z));
                    double h = Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z);
                    pitch = (float) Math.toDegrees(-Math.atan2(tangent.y, h));
                }
            } else if (globalLookAt != null) {
                Orientation orient = orientationTo(pos, globalLookAt);
                yaw = orient.yaw;
                pitch = orient.pitch;
            } else {
                ActorMotionFrame ref = frames.getFirst();
                yaw = ref.yaw() != null ? ref.yaw() : 0.0f;
                pitch = ref.pitch() != null ? ref.pitch() : 0.0f;
            }
            return new MotionState(pos, yaw, pitch);
        }

        for (int i = 0; i < frames.size() - 1; i++) {
            ActorMotionFrame from = frames.get(i);
            ActorMotionFrame to = frames.get(i + 1);
            if (tick >= from.tick() && tick <= to.tick()) {
                double span = (double) (to.tick() - from.tick());
                if (span <= 0.0D) {
                    return toState(to);
                }
                double progress = (tick - from.tick()) / span;
                double eased = to.interpolator().interpolate(progress);
                Vector3d pos = new Vector3d(from.position()).lerp(to.position(), eased);

                float yaw = 0.0f;
                float pitch = 0.0f;

                if (isTangentHeading()) {
                    Vector3d dir = new Vector3d(to.position()).sub(from.position());
                    if (dir.lengthSquared() > 1e-6D) {
                        yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
                        double h = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                        pitch = (float) Math.toDegrees(-Math.atan2(dir.y, h));
                    }
                } else if (to.lookAt() != null) {
                    Orientation orient = orientationTo(pos, to.lookAt());
                    yaw = orient.yaw;
                    pitch = orient.pitch;
                } else if (globalLookAt != null) {
                    Orientation orient = orientationTo(pos, globalLookAt);
                    yaw = orient.yaw;
                    pitch = orient.pitch;
                } else if (from.yaw() != null && to.yaw() != null) {
                    yaw = (float) (from.yaw() + (to.yaw() - from.yaw()) * eased);
                    pitch = from.pitch() != null && to.pitch() != null ? (float) (from.pitch() + (to.pitch() - from.pitch()) * eased) : 0.0f;
                }
                return new MotionState(pos, yaw, pitch);
            }
        }
        return toState(frames.getLast());
    }

    private boolean isTangentHeading() {
        return "tangent".equalsIgnoreCase(heading) || "follow_path".equalsIgnoreCase(heading);
    }

    private MotionState toState(ActorMotionFrame frame) {
        Vector3d pos = new Vector3d(frame.position());
        float yaw = frame.yaw() != null ? frame.yaw() : 0.0f;
        float pitch = frame.pitch() != null ? frame.pitch() : 0.0f;
        if (frame.lookAt() != null) {
            Orientation o = orientationTo(pos, frame.lookAt());
            yaw = o.yaw;
            pitch = o.pitch;
        } else if (globalLookAt != null) {
            Orientation o = orientationTo(pos, globalLookAt);
            yaw = o.yaw;
            pitch = o.pitch;
        }
        return new MotionState(pos, yaw, pitch);
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

    private record MotionState(Vector3d position, float yaw, float pitch) {
    }

    private record Orientation(float yaw, float pitch) {
    }
}
