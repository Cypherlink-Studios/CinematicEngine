package com.darkbladedev.cinematic.adapters.actor;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.Animatable;
import com.darkbladedev.cinematic.actors.Equippable;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.actors.Movable;
import com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation.EntityAnimationType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.Action;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final PacketEventsBridge packetEventsBridge;
    private final SkinCacheService skinCache;
    private final AtomicBoolean valid = new AtomicBoolean(true);

    private Vector3d position;
    private Vector3d lastSentPosition;
    private float yaw;
    private float pitch;
    private float headYaw;
    private float lastSentYaw;
    private float lastSentPitch;
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
            PacketEventsBridge packetEventsBridge
    ) {
        this(id, name, initialPosition, initialYaw, initialPitch, viewersSupplier, packetEventsBridge, null);
    }

    public VirtualPlayerActor(
            UUID id,
            String name,
            Vector3d initialPosition,
            float initialYaw,
            float initialPitch,
            Supplier<Iterable<Player>> viewersSupplier,
            PacketEventsBridge packetEventsBridge,
            SkinCacheService skinCache
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.entityId = ENTITY_ID_COUNTER.incrementAndGet();
        this.position = new Vector3d(Objects.requireNonNull(initialPosition, "initialPosition"));
        this.lastSentPosition = new Vector3d(this.position);
        this.yaw = initialYaw;
        this.pitch = initialPitch;
        this.headYaw = initialYaw;
        this.lastSentYaw = initialYaw;
        this.lastSentPitch = initialPitch;
        this.viewersSupplier = Objects.requireNonNull(viewersSupplier, "viewersSupplier");
        this.packetEventsBridge = packetEventsBridge;
        this.skinCache = skinCache;
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

    // --- Packet Generation Methods ---

    public WrapperPlayServerPlayerInfoUpdate createPlayerInfoUpdatePacket() {
        UserProfile profile = new UserProfile(id, name);
        if (skinCache != null) {
            skinCache.getCached(name).ifPresent(skinData -> {
                profile.setTextureProperties(List.of(
                        new TextureProperty("textures", skinData.value(), skinData.signature())
                ));
            });
        }
        PlayerInfo playerInfo = new PlayerInfo(profile, false, 0, GameMode.SURVIVAL, null, null);
        EnumSet<Action> actions = EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_LISTED);
        return new WrapperPlayServerPlayerInfoUpdate(actions, List.of(playerInfo));
    }

    public WrapperPlayServerPlayerInfoRemove createPlayerInfoRemovePacket() {
        return new WrapperPlayServerPlayerInfoRemove(id);
    }

    public WrapperPlayServerSpawnEntity createSpawnPacket() {
        return new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(id),
                EntityTypes.PLAYER,
                new com.github.retrooper.packetevents.util.Vector3d(position.x, position.y, position.z),
                pitch,
                yaw,
                headYaw,
                0,
                Optional.empty()
        );
    }

    public WrapperPlayServerEntityHeadLook createHeadLookPacket() {
        return new WrapperPlayServerEntityHeadLook(entityId, headYaw);
    }

    public WrapperPlayServerEntityMetadata createMetadataPacket() {
        byte flags = 0;
        if (pose == ActorPose.CROUCHING) {
            flags |= 0x02;
        }
        if (pose == ActorPose.SWIMMING) {
            flags |= 0x10;
        }
        if (pose == ActorPose.FALL_FLYING) {
            flags |= (byte) 0x80;
        }

        byte handState = 0;
        if (itemUsage == ItemUsageState.BLOCKING
                || itemUsage == ItemUsageState.BOW_PULL
                || itemUsage == ItemUsageState.EATING
                || itemUsage == ItemUsageState.DRINKING
                || itemUsage == ItemUsageState.CROSSBOW_LOAD) {
            handState |= 0x01;
        }
        if (pose == ActorPose.SPIN_ATTACK) {
            handState |= 0x04;
        }

        List<EntityData<?>> metadata = new ArrayList<>();
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
        metadata.add(new EntityData<>(6, EntityDataTypes.ENTITY_POSE, toEntityPose(pose)));
        metadata.add(new EntityData<>(8, EntityDataTypes.BYTE, handState));
        metadata.add(new EntityData<>(17, EntityDataTypes.BYTE, (byte) 0x7F)); // All skin parts enabled

        return new WrapperPlayServerEntityMetadata(entityId, metadata);
    }

    public WrapperPlayServerEntityTeleport createTeleportPacket() {
        return new WrapperPlayServerEntityTeleport(
                entityId,
                new com.github.retrooper.packetevents.util.Vector3d(position.x, position.y, position.z),
                yaw,
                pitch,
                true
        );
    }

    public WrapperPlayServerEntityRelativeMoveAndRotation createRelMovePacket(short dx, short dy, short dz) {
        return new WrapperPlayServerEntityRelativeMoveAndRotation(
                entityId,
                dx,
                dy,
                dz,
                yaw,
                pitch,
                true
        );
    }

    public WrapperPlayServerEntityAnimation createAnimationPacket(ActorAction action) {
        return new WrapperPlayServerEntityAnimation(entityId, toAnimationType(action));
    }

    public WrapperPlayServerEntityEquipment createEquipmentPacket(EquipmentSlot slot, String itemKey) {
        ItemStack itemStack = toItemStack(itemKey);
        Equipment equipmentEntry = new Equipment(toPacketEventsSlot(slot), itemStack);
        return new WrapperPlayServerEntityEquipment(entityId, List.of(equipmentEntry));
    }

    public WrapperPlayServerDestroyEntities createDestroyPacket() {
        return new WrapperPlayServerDestroyEntities(entityId);
    }

    // --- Packet Dispatch Methods ---

    private void sendSpawnPackets() {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            return;
        }
        Iterable<Player> viewers = viewersSupplier.get();
        packetEventsBridge.broadcastPacket(viewers, createPlayerInfoUpdatePacket());
        packetEventsBridge.broadcastPacket(viewers, createSpawnPacket());
        packetEventsBridge.broadcastPacket(viewers, createHeadLookPacket());
        packetEventsBridge.broadcastPacket(viewers, createMetadataPacket());
        for (Map.Entry<EquipmentSlot, String> entry : equipment.entrySet()) {
            packetEventsBridge.broadcastPacket(viewers, createEquipmentPacket(entry.getKey(), entry.getValue()));
        }
    }

    private void sendDespawnPackets() {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            return;
        }
        Iterable<Player> viewers = viewersSupplier.get();
        packetEventsBridge.broadcastPacket(viewers, createDestroyPacket());
        packetEventsBridge.broadcastPacket(viewers, createPlayerInfoRemovePacket());
    }

    private void sendTeleportOrMove(boolean instant) {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            lastSentPosition.set(position);
            lastSentYaw = yaw;
            lastSentPitch = pitch;
            return;
        }
        double dx = position.x - lastSentPosition.x;
        double dy = position.y - lastSentPosition.y;
        double dz = position.z - lastSentPosition.z;
        boolean canRelative = !instant
                && Math.abs(dx) <= 8.0
                && Math.abs(dy) <= 8.0
                && Math.abs(dz) <= 8.0;

        Iterable<Player> viewers = viewersSupplier.get();
        if (canRelative) {
            short sdx = (short) (dx * 4096.0);
            short sdy = (short) (dy * 4096.0);
            short sdz = (short) (dz * 4096.0);
            packetEventsBridge.broadcastPacket(viewers, createRelMovePacket(sdx, sdy, sdz));
        } else {
            packetEventsBridge.broadcastPacket(viewers, createTeleportPacket());
        }
        lastSentPosition.set(position);
        lastSentYaw = yaw;
        lastSentPitch = pitch;
    }

    private void sendHeadRotation() {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            return;
        }
        packetEventsBridge.broadcastPacket(viewersSupplier.get(), createHeadLookPacket());
    }

    private void sendEntityMetadata() {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            return;
        }
        packetEventsBridge.broadcastPacket(viewersSupplier.get(), createMetadataPacket());
    }

    private void sendAnimationPacket(ActorAction action) {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            return;
        }
        packetEventsBridge.broadcastPacket(viewersSupplier.get(), createAnimationPacket(action));
    }

    private void sendEquipmentPacket(EquipmentSlot slot, String itemKey) {
        if (packetEventsBridge == null || !packetEventsBridge.isAvailable()) {
            return;
        }
        packetEventsBridge.broadcastPacket(viewersSupplier.get(), createEquipmentPacket(slot, itemKey));
    }

    // --- Type Conversions ---

    static EntityPose toEntityPose(ActorPose pose) {
        if (pose == null) {
            return EntityPose.STANDING;
        }
        return switch (pose) {
            case STANDING -> EntityPose.STANDING;
            case CROUCHING -> EntityPose.CROUCHING;
            case SWIMMING -> EntityPose.SWIMMING;
            case SLEEPING -> EntityPose.SLEEPING;
            case FALL_FLYING -> EntityPose.FALL_FLYING;
            case SPIN_ATTACK -> EntityPose.SPIN_ATTACK;
        };
    }

    static EntityAnimationType toAnimationType(ActorAction action) {
        if (action == null) {
            return EntityAnimationType.SWING_MAIN_ARM;
        }
        return switch (action) {
            case SWING_MAIN_HAND -> EntityAnimationType.SWING_MAIN_ARM;
            case SWING_OFF_HAND -> EntityAnimationType.SWING_OFF_HAND;
            case HURT -> EntityAnimationType.HURT;
            case CRITICAL_HIT -> EntityAnimationType.CRITICAL_HIT;
            case MAGIC_CRITICAL -> EntityAnimationType.MAGIC_CRITICAL_HIT;
        };
    }

    static com.github.retrooper.packetevents.protocol.player.EquipmentSlot toPacketEventsSlot(EquipmentSlot slot) {
        if (slot == null) {
            return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND;
        }
        return switch (slot) {
            case MAIN_HAND -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.OFF_HAND;
            case HELMET -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.HELMET;
            case CHESTPLATE -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.CHEST_PLATE;
            case LEGGINGS -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.LEGGINGS;
            case BOOTS -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.BOOTS;
        };
    }

    static ItemStack toItemStack(String itemKey) {
        if (itemKey == null || itemKey.isBlank() || "AIR".equalsIgnoreCase(itemKey)) {
            return ItemStack.EMPTY;
        }
        String key = itemKey.toLowerCase(Locale.ROOT).trim();
        if (!key.startsWith("minecraft:")) {
            key = "minecraft:" + key;
        }
        ItemType type = ItemTypes.getByName(key);
        if (type == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.builder().type(type).amount(1).build();
    }
}
