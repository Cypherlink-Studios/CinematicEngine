package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ActorActionTrack implements Track {
    private final String id;
    private final String actorId;
    private final List<ActorActionFrame> frames;

    public ActorActionTrack(String id, String actorId, List<ActorActionFrame> frames) {
        this.id = Objects.requireNonNull(id, "id");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.frames = Objects.requireNonNull(frames, "frames")
                .stream()
                .sorted(Comparator.comparingLong(ActorActionFrame::tick))
                .toList();
        if (this.frames.isEmpty()) {
            throw new IllegalArgumentException("ActorActionTrack requiere al menos un keyframe");
        }
    }

    @Override
    public String id() {
        return id;
    }

    public String actorId() {
        return actorId;
    }

    public List<ActorActionFrame> frames() {
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

        for (ActorActionFrame frame : frames) {
            if (frame.tick() != tick) {
                continue;
            }

            actor.asAnimatable().ifPresent(animatable -> {
                if (frame.pose() != null) {
                    animatable.setPose(frame.pose());
                }
                if (frame.action() != null) {
                    animatable.triggerAction(frame.action());
                }
                if (frame.itemUsage() != null) {
                    animatable.setItemUsage(frame.itemUsage());
                }
            });

            if (frame.equipment() != null && !frame.equipment().isEmpty()) {
                actor.asEquippable().ifPresent(equippable ->
                        frame.equipment().forEach(equippable::setEquipment)
                );
            }

            if (frame.headYaw() != null) {
                actor.asMovable().ifPresent(movable ->
                        movable.rotateHead(frame.headYaw(), frame.headPitch() == null ? movable.pitch() : frame.headPitch())
                );
            }
        }
    }
}
