package com.darkbladedev.cinematic.camera;

import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CameraTrack implements Track {
    private final String id;
    private final List<CameraFrame> frames;

    public CameraTrack(String id, List<CameraFrame> frames) {
        this.id = Objects.requireNonNull(id, "id");
        this.frames = Objects.requireNonNull(frames, "frames")
                .stream()
                .sorted(Comparator.comparingLong(CameraFrame::tick))
                .toList();
        if (this.frames.isEmpty()) {
            throw new IllegalArgumentException("CameraTrack requires at least one frame");
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
        CameraState state = evaluateState(tick);
        context.getService(CameraOutput.class).ifPresent(output -> output.apply(state));
    }

    private CameraState evaluateState(long tick) {
        if (frames.size() == 1 || tick <= frames.getFirst().tick()) {
            return toState(frames.getFirst());
        }
        if (tick >= frames.getLast().tick()) {
            return toState(frames.getLast());
        }
        for (int index = 0; index < frames.size() - 1; index++) {
            CameraFrame from = frames.get(index);
            CameraFrame to = frames.get(index + 1);
            if (tick >= from.tick() && tick <= to.tick()) {
                double segmentLength = (double) (to.tick() - from.tick());
                double progress = (tick - from.tick()) / segmentLength;
                double easedProgress = to.interpolator().interpolate(progress);
                Vector3d interpolatedPosition = new Vector3d(from.position()).lerp(to.position(), easedProgress);
                float interpolatedYaw = interpolateAngle(from.yaw(), to.yaw(), easedProgress);
                float interpolatedPitch = interpolateAngle(from.pitch(), to.pitch(), easedProgress);
                float interpolatedFov = lerp(from.fov(), to.fov(), easedProgress);
                return new CameraState(interpolatedPosition, interpolatedYaw, interpolatedPitch, interpolatedFov);
            }
        }
        return toState(frames.getLast());
    }

    private CameraState toState(CameraFrame frame) {
        return new CameraState(new Vector3d(frame.position()), frame.yaw(), frame.pitch(), frame.fov());
    }

    private float interpolateAngle(float from, float to, double progress) {
        float delta = wrapDegrees(to - from);
        return from + (float) (delta * progress);
    }

    private float lerp(float from, float to, double progress) {
        return from + (float) ((to - from) * progress);
    }

    private float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }
}
