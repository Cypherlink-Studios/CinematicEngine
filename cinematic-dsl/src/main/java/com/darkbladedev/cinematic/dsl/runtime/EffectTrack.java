package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class EffectTrack implements Track {
    private final String id;
    private final List<EffectFrame> frames;

    public EffectTrack(String id, List<EffectFrame> frames) {
        this.id = Objects.requireNonNull(id, "id");
        this.frames = Objects.requireNonNull(frames, "frames")
                .stream()
                .sorted(Comparator.comparingLong(EffectFrame::tick))
                .toList();
        if (this.frames.isEmpty()) {
            throw new IllegalArgumentException("EffectTrack requiere al menos un keyframe");
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
        EffectOutput output = context.getService(EffectOutput.class).orElse(null);
        if (output == null) {
            return;
        }
        for (EffectFrame frame : frames) {
            if (frame.tick() != tick) {
                continue;
            }
            if (frame.sound() != null) {
                output.playSound(frame.sound());
            }
            if (frame.particle() != null) {
                output.spawnParticle(frame.particle());
            }
        }
    }
}
