package com.darkbladedev.cinematic.actors;

import org.joml.Vector3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ActorCapabilityTest {

    @Test
    @DisplayName("ActorPose and ActorAction enums have all required states")
    void enumsContainExpectedValues() {
        assertThat(ActorPose.values()).contains(
                ActorPose.STANDING,
                ActorPose.CROUCHING,
                ActorPose.SWIMMING,
                ActorPose.SLEEPING,
                ActorPose.FALL_FLYING,
                ActorPose.SPIN_ATTACK
        );

        assertThat(ActorAction.values()).contains(
                ActorAction.SWING_MAIN_HAND,
                ActorAction.SWING_OFF_HAND,
                ActorAction.HURT,
                ActorAction.CRITICAL_HIT,
                ActorAction.MAGIC_CRITICAL
        );

        assertThat(ItemUsageState.values()).contains(
                ItemUsageState.NONE,
                ItemUsageState.BLOCKING,
                ItemUsageState.BOW_PULL,
                ItemUsageState.EATING,
                ItemUsageState.DRINKING,
                ItemUsageState.CROSSBOW_LOAD
        );

        assertThat(EquipmentSlot.values()).contains(
                EquipmentSlot.MAIN_HAND,
                EquipmentSlot.OFF_HAND,
                EquipmentSlot.HELMET,
                EquipmentSlot.CHESTPLATE,
                EquipmentSlot.LEGGINGS,
                EquipmentSlot.BOOTS
        );
    }

    @Test
    @DisplayName("Movable implementation tracks position and rotation correctly")
    void movableTracksTransform() {
        TestMovable movable = new TestMovable(new Vector3d(10.0, 64.0, 20.0), 45.0f, -15.0f, 90.0f);

        assertThat(movable.position()).isEqualTo(new Vector3d(10.0, 64.0, 20.0));
        assertThat(movable.yaw()).isEqualTo(45.0f);
        assertThat(movable.pitch()).isEqualTo(-15.0f);
        assertThat(movable.headYaw()).isEqualTo(90.0f);

        movable.moveTo(new Vector3d(12.0, 65.0, 22.0), false);
        movable.rotate(180.0f, 0.0f);
        movable.rotateHead(190.0f, 0.0f);

        assertThat(movable.position()).isEqualTo(new Vector3d(12.0, 65.0, 22.0));
        assertThat(movable.yaw()).isEqualTo(180.0f);
        assertThat(movable.headYaw()).isEqualTo(190.0f);
    }

    @Test
    @DisplayName("Animatable and Equippable track poses, actions, item usages, and gear")
    void animatableAndEquippableWork() {
        TestAnimatable animatable = new TestAnimatable();
        TestEquippable equippable = new TestEquippable();

        assertThat(animatable.pose()).isEqualTo(ActorPose.STANDING);
        animatable.setPose(ActorPose.CROUCHING);
        assertThat(animatable.pose()).isEqualTo(ActorPose.CROUCHING);

        animatable.setItemUsage(ItemUsageState.BLOCKING);
        assertThat(animatable.itemUsage()).isEqualTo(ItemUsageState.BLOCKING);

        animatable.triggerAction(ActorAction.SWING_MAIN_HAND);
        assertThat(animatable.lastAction).isEqualTo(ActorAction.SWING_MAIN_HAND);

        equippable.setEquipment(EquipmentSlot.MAIN_HAND, "DIAMOND_SWORD");
        assertThat(equippable.getEquipment(EquipmentSlot.MAIN_HAND)).isEqualTo("DIAMOND_SWORD");
    }

    @Test
    @DisplayName("Actor default capability methods resolve implementing instances")
    void actorDefaultCapabilitiesResolveCorrectly() {
        TestFullActor fullActor = new TestFullActor();
        assertThat(fullActor.asMovable()).isPresent();
        assertThat(fullActor.asAnimatable()).isPresent();
        assertThat(fullActor.asEquippable()).isPresent();

        java.util.UUID npcId = java.util.UUID.randomUUID();
        java.util.concurrent.atomic.AtomicReference<Vector3d> moved = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicBoolean valid = new java.util.concurrent.atomic.AtomicBoolean(true);
        NPCActor npcActor = new NPCActor(npcId, moved::set, (y, p) -> {}, valid::get);

        assertThat(npcActor.id()).isEqualTo(npcId);
        assertThat(npcActor.isValid()).isTrue();
        assertThat(npcActor.asMovable()).isPresent();
        assertThat(npcActor.asAnimatable()).isEmpty();
        assertThat(npcActor.asEquippable()).isEmpty();

        npcActor.teleport(new Vector3d(5.0, 10.0, 15.0));
        assertThat(moved.get()).isEqualTo(new Vector3d(5.0, 10.0, 15.0));
        assertThat(npcActor.position()).isEqualTo(new Vector3d(5.0, 10.0, 15.0));

        FakeEntityActor fakeActor = new FakeEntityActor(npcId, moved::set, (y, p) -> {}, () -> true);
        assertThat(fakeActor.asMovable()).isPresent();
        fakeActor.moveTo(new Vector3d(1.0, 2.0, 3.0), true);
        assertThat(fakeActor.position()).isEqualTo(new Vector3d(1.0, 2.0, 3.0));
    }

    private static class TestFullActor implements Actor, Movable, Animatable, Equippable {
        private final java.util.UUID id = java.util.UUID.randomUUID();
        private Vector3d pos = new Vector3d();
        private ActorPose pose = ActorPose.STANDING;
        private ItemUsageState usage = ItemUsageState.NONE;

        @Override public java.util.UUID id() { return id; }
        @Override public void teleport(Vector3d position) { this.pos = position; }
        @Override public void rotate(float yaw, float pitch) {}
        @Override public boolean isValid() { return true; }
        @Override public Vector3d position() { return pos; }
        @Override public void moveTo(Vector3d position, boolean instant) { this.pos = position; }
        @Override public void setPose(ActorPose pose) { this.pose = pose; }
        @Override public ActorPose pose() { return pose; }
        @Override public void triggerAction(ActorAction action) {}
        @Override public void setItemUsage(ItemUsageState state) { this.usage = state; }
        @Override public ItemUsageState itemUsage() { return usage; }
        @Override public void setEquipment(EquipmentSlot slot, String itemKey) {}
        @Override public String getEquipment(EquipmentSlot slot) { return "AIR"; }
    }

    private static class TestMovable implements Movable {
        private Vector3d position;
        private float yaw;
        private float pitch;
        private float headYaw;

        TestMovable(Vector3d position, float yaw, float pitch, float headYaw) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.headYaw = headYaw;
        }

        @Override
        public Vector3d position() {
            return position;
        }

        @Override
        public void moveTo(Vector3d position, boolean instant) {
            this.position = position;
        }

        @Override
        public void rotate(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public void rotateHead(float headYaw, float headPitch) {
            this.headYaw = headYaw;
        }

        @Override
        public float yaw() {
            return yaw;
        }

        @Override
        public float pitch() {
            return pitch;
        }

        @Override
        public float headYaw() {
            return headYaw;
        }
    }

    private static class TestAnimatable implements Animatable {
        private ActorPose pose = ActorPose.STANDING;
        private ItemUsageState usage = ItemUsageState.NONE;
        ActorAction lastAction;

        @Override
        public void setPose(ActorPose pose) {
            this.pose = pose;
        }

        @Override
        public ActorPose pose() {
            return pose;
        }

        @Override
        public void triggerAction(ActorAction action) {
            this.lastAction = action;
        }

        @Override
        public void setItemUsage(ItemUsageState state) {
            this.usage = state;
        }

        @Override
        public ItemUsageState itemUsage() {
            return usage;
        }
    }

    private static class TestEquippable implements Equippable {
        private final Map<EquipmentSlot, String> items = new EnumMap<>(EquipmentSlot.class);

        @Override
        public void setEquipment(EquipmentSlot slot, String itemKey) {
            items.put(slot, itemKey);
        }

        @Override
        public String getEquipment(EquipmentSlot slot) {
            return items.get(slot);
        }
    }
}
