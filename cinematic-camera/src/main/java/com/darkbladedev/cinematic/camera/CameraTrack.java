package com.darkbladedev.cinematic.camera;

import com.darkbladedev.cinematic.camera.targeting.CameraOrientation;
import com.darkbladedev.cinematic.camera.targeting.FixedAnglesStrategy;
import com.darkbladedev.cinematic.camera.targeting.LookAtStrategy;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.core.spline.CatmullRomSpline;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CameraTrack implements Track {
    private final String id;
    private final List<CameraFrame> frames;
    private final CameraPathMode pathMode;
    private final LookAtStrategy lookAtStrategy;

    public CameraTrack(String id, List<CameraFrame> frames) {
        this(id, frames, CameraPathMode.LINEAR, FixedAnglesStrategy.INSTANCE);
    }

    public CameraTrack(String id, List<CameraFrame> frames, CameraPathMode pathMode, LookAtStrategy lookAtStrategy) {
        this.id = Objects.requireNonNull(id, "id");
        this.frames = Objects.requireNonNull(frames, "frames")
                .stream()
                .sorted(Comparator.comparingLong(CameraFrame::tick))
                .toList();
        if (this.frames.isEmpty()) {
            throw new IllegalArgumentException("CameraTrack requires at least one frame");
        }
        this.pathMode = pathMode == null ? CameraPathMode.LINEAR : pathMode;
        this.lookAtStrategy = lookAtStrategy == null ? FixedAnglesStrategy.INSTANCE : lookAtStrategy;
    }

    @Override
    public String id() {
        return id;
    }

    public CameraPathMode pathMode() {
        return pathMode;
    }

    public LookAtStrategy lookAtStrategy() {
        return lookAtStrategy;
    }

    public List<CameraFrame> frames() {
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
        CameraState state = evaluateState(tick, context);
        context.getService(CameraOutput.class).ifPresent(output -> output.apply(state));
    }

    public CameraState evaluateState(long tick, TimelineContext context) {
        if (frames.size() == 1 || tick < frames.getFirst().tick()) {
            return toState(frames.getFirst(), context, tick);
        }
        if (tick >= frames.getLast().tick()) {
            return toState(frames.getLast(), context, tick);
        }
        for (int index = 0; index < frames.size() - 1; index++) {
            CameraFrame from = frames.get(index);
            CameraFrame to = frames.get(index + 1);
            if (tick >= from.tick() && tick <= to.tick()) {
                double segmentLength = (double) (to.tick() - from.tick());
                if (segmentLength <= 0.0D) {
                    return toState(to, context, tick);
                }
                double progress = (tick - from.tick()) / segmentLength;
                double easedProgress = to.interpolator().interpolate(progress);

                Vector3d interpolatedPosition;
                Vector3d velocityDirection;

                if (pathMode == CameraPathMode.CATMULL_ROM) {
                    Vector3d p1 = from.position();
                    Vector3d p2 = to.position();
                    Vector3d p0 = index > 0 ? frames.get(index - 1).position() : new Vector3d(p1).mul(2.0D).sub(p2);
                    Vector3d p3 = (index + 2 < frames.size()) ? frames.get(index + 2).position() : new Vector3d(p2).mul(2.0D).sub(p1);

                    interpolatedPosition = CatmullRomSpline.interpolate(p0, p1, p2, p3, easedProgress);
                    velocityDirection = new Vector3d(p2).sub(p1);
                } else {
                    interpolatedPosition = new Vector3d(from.position()).lerp(to.position(), easedProgress);
                    velocityDirection = new Vector3d(to.position()).sub(from.position());
                }

                float fallbackYaw = interpolateAngle(from.yaw(), to.yaw(), easedProgress);
                float fallbackPitch = interpolateAngle(from.pitch(), to.pitch(), easedProgress);
                CameraOrientation orientation = lookAtStrategy.calculateOrientation(
                        tick,
                        interpolatedPosition,
                        velocityDirection,
                        fallbackYaw,
                        fallbackPitch,
                        context
                );
                float interpolatedFov = lerp(from.fov(), to.fov(), easedProgress);
                return new CameraState(interpolatedPosition, orientation.yaw(), orientation.pitch(), interpolatedFov);
            }
        }
        return toState(frames.getLast(), context, tick);
    }

    private CameraState toState(CameraFrame frame, TimelineContext context, long tick) {
        CameraOrientation orientation = lookAtStrategy.calculateOrientation(
                tick,
                frame.position(),
                new Vector3d(0.0D, 0.0D, 0.0D),
                frame.yaw(),
                frame.pitch(),
                context
        );
        return new CameraState(new Vector3d(frame.position()), orientation.yaw(), orientation.pitch(), frame.fov());
    }

    private float interpolateAngle(float from, float to, double progress) {
        float delta = wrapDegrees(to - from);
        return from + (float) (delta * progress);
    }

    private float lerp(float from, float to, double progress) {
        return from + (float) ((to - from) * progress);
    }

    private float wrapDegrees(float value) {
        return LookAtStrategy.wrapDegrees(value);
    }
}
