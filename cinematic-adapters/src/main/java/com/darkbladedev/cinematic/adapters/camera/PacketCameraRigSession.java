package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge;
import com.darkbladedev.cinematic.camera.CameraState;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCamera;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Camera rig session implementing {@link CameraMountMode#PACKET_VIRTUAL}.
 * Binds the spectator client to a virtual camera entity using PacketEvents {@link WrapperPlayServerCamera}
 * without mutating or creating world entities on the server.
 */
public final class PacketCameraRigSession extends AbstractCameraRigSession {
    private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger(1_700_000_000);

    private final int virtualEntityId;
    private final UUID virtualEntityUuid;
    private final PacketEventsBridge packetBridge;
    private boolean active = true;

    public PacketCameraRigSession(Player player, CameraState initialState, PacketEventsBridge packetBridge) {
        this(player, initialState, packetBridge, ENTITY_ID_COUNTER.incrementAndGet(), UUID.randomUUID());
    }

    public PacketCameraRigSession(
            Player player,
            CameraState initialState,
            PacketEventsBridge packetBridge,
            int virtualEntityId,
            UUID virtualEntityUuid
    ) {
        super(player);
        this.packetBridge = Objects.requireNonNull(packetBridge, "packetBridge");
        this.virtualEntityId = virtualEntityId;
        this.virtualEntityUuid = Objects.requireNonNull(virtualEntityUuid, "virtualEntityUuid");

        mount(player, initialState);
    }

    private void mount(Player player, CameraState state) {
        player.setGameMode(GameMode.SPECTATOR);

        if (packetBridge.isAvailable()) {
            packetBridge.sendPacket(player, createSpawnPacket(state));
            packetBridge.sendPacket(player, createMountPacket());
        }
    }

    @Override
    public CameraMountMode mountMode() {
        return CameraMountMode.PACKET_VIRTUAL;
    }

    public int virtualEntityId() {
        return virtualEntityId;
    }

    public UUID virtualEntityUuid() {
        return virtualEntityUuid;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Re-mounts the player's camera to the virtual entity (useful when spectator sneak dismount occurs).
     *
     * @param player player to remount
     */
    public void remount(Player player) {
        if (!active || player == null || !player.isOnline()) {
            return;
        }
        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        if (packetBridge.isAvailable()) {
            packetBridge.sendPacket(player, createMountPacket());
        }
    }

    @Override
    public void update(Player player, CameraState state) {
        if (!active || player == null || !player.isOnline()) {
            return;
        }

        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        if (packetBridge.isAvailable()) {
            packetBridge.sendPacket(player, createTeleportPacket(state));
        }
    }

    @Override
    public void restore(Player player) {
        if (!active) {
            return;
        }
        active = false;

        if (player != null && player.isOnline()) {
            if (packetBridge.isAvailable()) {
                // Detach client camera back to player entity
                packetBridge.sendPacket(player, createDismountPacket(player));
                // Despawn virtual entity from client
                packetBridge.sendPacket(player, createDestroyPacket());
            }
            restorePlayerState(player);
        }
    }

    @Override
    public void cleanup() {
        if (!active) {
            return;
        }
        active = false;

        Player player = Bukkit.getServer() != null ? Bukkit.getPlayer(playerId()) : null;
        if (player != null && player.isOnline() && packetBridge.isAvailable()) {
            packetBridge.sendPacket(player, createDismountPacket(player));
            packetBridge.sendPacket(player, createDestroyPacket());
        }
    }

    public WrapperPlayServerSpawnEntity createSpawnPacket(CameraState state) {
        return new WrapperPlayServerSpawnEntity(
                virtualEntityId,
                Optional.of(virtualEntityUuid),
                EntityTypes.ITEM_DISPLAY,
                new Vector3d(state.position().x, state.position().y, state.position().z),
                state.pitch(),
                state.yaw(),
                state.yaw(),
                0,
                Optional.empty()
        );
    }

    public WrapperPlayServerCamera createMountPacket() {
        return new WrapperPlayServerCamera(virtualEntityId);
    }

    public WrapperPlayServerEntityTeleport createTeleportPacket(CameraState state) {
        return new WrapperPlayServerEntityTeleport(
                virtualEntityId,
                new Vector3d(state.position().x, state.position().y, state.position().z),
                state.yaw(),
                state.pitch(),
                false
        );
    }

    public WrapperPlayServerCamera createDismountPacket(Player player) {
        return new WrapperPlayServerCamera(player.getEntityId());
    }

    public WrapperPlayServerDestroyEntities createDestroyPacket() {
        return new WrapperPlayServerDestroyEntities(virtualEntityId);
    }
}
