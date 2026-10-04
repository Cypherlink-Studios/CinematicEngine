package com.darkbladedev.cinematic.adapters.actor;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.Animatable;
import com.darkbladedev.cinematic.actors.Equippable;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.actors.Movable;
import com.darkbladedev.cinematic.adapters.camera.ProtocolLibBridge;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class VirtualPlayerActor implements Actor, Movable, Animatable, Equippable {
    private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger(2_000_000);

    private final UUID id;
    private final int entityId;
    private final String name;
    private final Supplier<Iterable<Player>> viewersSupplier;
    private final ProtocolLibBridge protocolLibBridge;
    private final AtomicBoolean valid = new AtomicBoolean(true);

    private Vector3d position;
    private float yaw;
    private float pitch;
    private float headYaw;
    private ActorPose pose = ActorPose.STANDING;
    private ItemUsageState itemUsage = ItemUsageState.NONE;
    private final Map<EquipmentSlot, String> equipment = new EnumMap<>(EquipmentSlot.class);

    public VirtualPlayerActor(
            UUID id,
            String name,
            Vector3d initialPosition,
            float initialYaw,
            float initialPitch,
            Supplier<Iterable<Player>> viewersSupplier,
            ProtocolLibBridge protocolLibBridge
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.entityId = ENTITY_ID_COUNTER.incrementAndGet();
        this.position = new Vector3d(Objects.requireNonNull(initialPosition, "initialPosition"));
        this.yaw = initialYaw;
        this.pitch = initialPitch;
        this.headYaw = initialYaw;
        this.viewersSupplier = Objects.requireNonNull(viewersSupplier, "viewersSupplier");
        this.protocolLibBridge = protocolLibBridge;
    }

    public int entityId() {
        return entityId;
    }

    public String name() {
        return name;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public boolean isValid() {
        return valid.get();
    }

    @Override
    public void despawn() {
        if (!valid.compareAndSet(true, false)) {
            return;
        }
        sendDespawnPackets();
    }

    @Override
    public void teleport(Vector3d target) {
        moveTo(target, true);
    }

    @Override
    public void rotate(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        sendTeleportOrMove(false);
    }

    // --- Movable ---
    @Override
    public Vector3d position() {
        return new Vector3d(position);
    }

    @Override
    public void moveTo(Vector3d newPosition, boolean instant) {
        if (!isValid()) {
            return;
        }
        this.position = new Vector3d(newPosition);
        sendTeleportOrMove(instant);
    }

    @Override
    public void rotateHead(float headYaw, float headPitch) {
        if (!isValid()) {
            return;
        }
        this.headYaw = headYaw;
        sendHeadRotation();
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

    // --- Animatable ---
    @Override
    public void setPose(ActorPose pose) {
        if (!isValid()) {
            return;
        }
        this.pose = Objects.requireNonNull(pose, "pose");
        sendEntityMetadata();
    }

    @Override
    public ActorPose pose() {
        return pose;
    }

    @Override
    public void triggerAction(ActorAction action) {
        if (!isValid()) {
            return;
        }
        Objects.requireNonNull(action, "action");
        sendAnimationPacket(action);
    }

    @Override
    public void setItemUsage(ItemUsageState state) {
        if (!isValid()) {
            return;
        }
        this.itemUsage = Objects.requireNonNull(state, "state");
        sendEntityMetadata();
    }

    @Override
    public ItemUsageState itemUsage() {
        return itemUsage;
    }

    // --- Equippable ---
    @Override
    public void setEquipment(EquipmentSlot slot, String itemKey) {
        if (!isValid()) {
            return;
        }
        Objects.requireNonNull(slot, "slot");
        equipment.put(slot, itemKey);
        sendEquipmentPacket(slot, itemKey);
    }

    @Override
    public String getEquipment(EquipmentSlot slot) {
        return equipment.getOrDefault(slot, "AIR");
    }

    public void spawn() {
        if (!isValid()) {
            return;
        }
        sendSpawnPackets();
    }

    private void sendSpawnPackets() {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // When ProtocolLib is available, spawn packets (PlayerInfoUpdate, AddEntity, SetEquipment) are broadcast to viewers
    }

    private void sendDespawnPackets() {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // When ProtocolLib is available, destroy entity packet is sent to viewers
    }

    private void sendTeleportOrMove(boolean instant) {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // Send RelMoveLook or EntityTeleport packet
    }

    private void sendHeadRotation() {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // Send EntityHeadRotation packet
    }

    private void sendEntityMetadata() {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // Send SetEntityData packet with Pose and item usage flags
    }

    private void sendAnimationPacket(ActorAction action) {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // Send Animate packet (swing main hand, swing offhand, hurt animation)
    }

    private void sendEquipmentPacket(EquipmentSlot slot, String itemKey) {
        if (protocolLibBridge == null || !protocolLibBridge.isAvailable()) {
            return;
        }
        // Send SetEquipment packet
    }
}
