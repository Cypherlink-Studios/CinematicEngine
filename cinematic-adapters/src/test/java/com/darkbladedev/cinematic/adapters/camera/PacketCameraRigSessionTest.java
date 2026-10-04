package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge;
import com.darkbladedev.cinematic.camera.CameraState;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCamera;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.joml.Vector3d;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacketCameraRigSessionTest {

    @BeforeAll
    static void setupPacketEvents() {
        com.darkbladedev.cinematic.adapters.packet.PacketEventsTestHelper.init();
    }

    private Player createMockPlayer() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getEntityId()).thenReturn(42);
        when(player.isOnline()).thenReturn(true);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getAllowFlight()).thenReturn(false);
        when(player.isFlying()).thenReturn(false);

        World world = mock(World.class);
        Location loc = new Location(world, 10.0, 64.0, 10.0, 0f, 0f);
        when(player.getLocation()).thenReturn(loc);
        when(player.getWorld()).thenReturn(world);
        return player;
    }

    @Test
    @DisplayName("PacketCameraRigSession creates valid spawn, camera mount, teleport, and restore packets")
    void packetCameraRigLifecyclePackets() {
        Player player = createMockPlayer();
        PacketEventsBridge packetBridge = mock(PacketEventsBridge.class);
        when(packetBridge.isAvailable()).thenReturn(true);

        CameraState initialState = new CameraState(new Vector3d(50.0, 70.0, 50.0), 90.0f, -15.0f, 70.0f);
        PacketCameraRigSession session = new PacketCameraRigSession(player, initialState, packetBridge);

        assertThat(session.mountMode()).isEqualTo(CameraMountMode.PACKET_VIRTUAL);
        assertThat(session.isActive()).isTrue();
        assertThat(session.playerId()).isEqualTo(player.getUniqueId());
        verify(player).setGameMode(GameMode.SPECTATOR);

        // Verify spawn packet construction
        WrapperPlayServerSpawnEntity spawnPacket = session.createSpawnPacket(initialState);
        assertThat(spawnPacket.getEntityId()).isEqualTo(session.virtualEntityId());
        assertThat(spawnPacket.getEntityType()).isEqualTo(EntityTypes.ITEM_DISPLAY);
        assertThat(spawnPacket.getPosition().getX()).isEqualTo(50.0);
        assertThat(spawnPacket.getPosition().getY()).isEqualTo(70.0);
        assertThat(spawnPacket.getPosition().getZ()).isEqualTo(50.0);

        // Verify mount packet construction
        WrapperPlayServerCamera mountPacket = session.createMountPacket();
        assertThat(mountPacket.getCameraId()).isEqualTo(session.virtualEntityId());

        // Verify motion teleport packet
        CameraState nextState = new CameraState(new Vector3d(55.0, 72.0, 52.0), 100.0f, -10.0f, 70.0f);
        WrapperPlayServerEntityTeleport tpPacket = session.createTeleportPacket(nextState);
        assertThat(tpPacket.getEntityId()).isEqualTo(session.virtualEntityId());
        assertThat(tpPacket.getPosition().getX()).isEqualTo(55.0);

        // Verify update dispatch
        session.update(player, nextState);
        verify(packetBridge).sendPacket(Mockito.eq(player), any(WrapperPlayServerEntityTeleport.class));

        // Verify dismount packet construction
        WrapperPlayServerCamera dismountPacket = session.createDismountPacket(player);
        assertThat(dismountPacket.getCameraId()).isEqualTo(player.getEntityId());

        // Verify destroy packet construction
        WrapperPlayServerDestroyEntities destroyPacket = session.createDestroyPacket();
        assertThat(destroyPacket.getEntityIds()).containsExactly(session.virtualEntityId());

        // Verify restore logic
        session.restore(player);
        assertThat(session.isActive()).isFalse();
        verify(player).setGameMode(GameMode.SURVIVAL);
        verify(player).setAllowFlight(false);
        verify(player).setFlying(false);
        verify(player).teleport(any(Location.class));
    }

    @Test
    @DisplayName("PacketCameraRigSession remount dispatches mount camera packet")
    void remountSendsMountPacket() {
        Player player = createMockPlayer();
        PacketEventsBridge packetBridge = mock(PacketEventsBridge.class);
        when(packetBridge.isAvailable()).thenReturn(true);

        CameraState initialState = new CameraState(new Vector3d(0.0, 64.0, 0.0), 0.0f, 0.0f, 70.0f);
        PacketCameraRigSession session = new PacketCameraRigSession(player, initialState, packetBridge);

        session.remount(player);
        verify(packetBridge, Mockito.atLeast(2)).sendPacket(Mockito.eq(player), any(WrapperPlayServerCamera.class));
    }
}
