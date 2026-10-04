package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.Animatable;
import com.darkbladedev.cinematic.actors.Equippable;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActorActionTrackTest {

    @Test
    @DisplayName("ActorActionTrack dispatches poses, actions, items, and equipment on target ticks")
    void dispatchesDiscreteActionsOnTargetTicks() {
        MockActionActor actor = new MockActionActor("hero");
        ActorResolver resolver = id -> "hero".equals(id) ? Optional.of(actor) : Optional.empty();
        TimelineContext context = new MockTimelineContext(resolver);

        ActorActionTrack track = new ActorActionTrack(
                "action1",
                "hero",
                List.of(
                        new ActorActionFrame(0L, ActorPose.STANDING, null, ItemUsageState.NONE, Map.of(EquipmentSlot.MAIN_HAND, "WOODEN_SWORD"), null, null),
                        new ActorActionFrame(30L, null, ActorAction.SWING_MAIN_HAND, null, Map.of(), null, null),
                        new ActorActionFrame(50L, ActorPose.CROUCHING, null, ItemUsageState.BLOCKING, Map.of(EquipmentSlot.MAIN_HAND, "DIAMOND_SWORD"), null, null)
                )
        );

        track.evaluate(0L, context);
        assertThat(actor.pose).isEqualTo(ActorPose.STANDING);
        assertThat(actor.gear.get(EquipmentSlot.MAIN_HAND)).isEqualTo("WOODEN_SWORD");
        assertThat(actor.lastAction).isNull();

        // Tick 15: nothing should change
        track.evaluate(15L, context);
        assertThat(actor.pose).isEqualTo(ActorPose.STANDING);
        assertThat(actor.lastAction).isNull();

        // Tick 30: swing
        track.evaluate(30L, context);
        assertThat(actor.lastAction).isEqualTo(ActorAction.SWING_MAIN_HAND);
        assertThat(actor.pose).isEqualTo(ActorPose.STANDING);

        // Tick 50: crouch, blocking, diamond sword
        track.evaluate(50L, context);
        assertThat(actor.pose).isEqualTo(ActorPose.CROUCHING);
        assertThat(actor.usage).isEqualTo(ItemUsageState.BLOCKING);
        assertThat(actor.gear.get(EquipmentSlot.MAIN_HAND)).isEqualTo("DIAMOND_SWORD");
    }

    private static class MockActionActor implements Actor, Animatable, Equippable {
        final UUID id = UUID.randomUUID();
        final String name;
        ActorPose pose = ActorPose.STANDING;
        ActorAction lastAction;
        ItemUsageState usage = ItemUsageState.NONE;
        final Map<EquipmentSlot, String> gear = new EnumMap<>(EquipmentSlot.class);

        MockActionActor(String name) { this.name = name; }
        @Override public UUID id() { return id; }
        @Override public void teleport(Vector3d position) {}
        @Override public void rotate(float yaw, float pitch) {}
        @Override public boolean isValid() { return true; }
        @Override public void setPose(ActorPose pose) { this.pose = pose; }
        @Override public ActorPose pose() { return pose; }
        @Override public void triggerAction(ActorAction action) { this.lastAction = action; }
        @Override public void setItemUsage(ItemUsageState state) { this.usage = state; }
        @Override public ItemUsageState itemUsage() { return usage; }
        @Override public void setEquipment(EquipmentSlot slot, String itemKey) { gear.put(slot, itemKey); }
        @Override public String getEquipment(EquipmentSlot slot) { return gear.get(slot); }
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
