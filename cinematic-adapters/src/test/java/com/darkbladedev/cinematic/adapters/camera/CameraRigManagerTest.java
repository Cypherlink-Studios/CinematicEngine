package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge;
import com.darkbladedev.cinematic.camera.CameraState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3d;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraRigManagerTest {

    @BeforeAll
    static void setup() {
        com.darkbladedev.cinematic.adapters.packet.PacketEventsTestHelper.init();
    }

    @SuppressWarnings("unchecked")
    private Player createMockPlayer() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getEntityId()).thenReturn(101);
        when(player.isOnline()).thenReturn(true);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getAllowFlight()).thenReturn(false);
        when(player.isFlying()).thenReturn(false);

        World world = mock(World.class);
        Location loc = new Location(world, 0.0, 64.0, 0.0, 0f, 0f);
        when(player.getLocation()).thenReturn(loc);
        when(player.getWorld()).thenReturn(world);

        ItemDisplay mockDisplay = mock(ItemDisplay.class);
        when(mockDisplay.isValid()).thenReturn(true);
        when(world.spawn(any(Location.class), eq(ItemDisplay.class), any(Consumer.class))).thenAnswer(invocation -> {
            Consumer<ItemDisplay> consumer = invocation.getArgument(2);
            if (consumer != null) {
                consumer.accept(mockDisplay);
            }
            return mockDisplay;
        });

        return player;
    }

    @Test
    @DisplayName("CameraRigManager defaults to PACKET_VIRTUAL and mounts PacketCameraRigSession when PacketEvents is available")
    void defaultsToPacketVirtualMode() {
        Plugin plugin = mock(Plugin.class);
        PacketEventsBridge bridge = mock(PacketEventsBridge.class);
        when(bridge.isAvailable()).thenReturn(true);

        CameraRigManager manager = new CameraRigManager(plugin, CameraMountMode.PACKET_VIRTUAL, bridge);
        assertThat(manager.defaultMode()).isEqualTo(CameraMountMode.PACKET_VIRTUAL);

        Player player = createMockPlayer();
        CameraState state = new CameraState(new Vector3d(10, 65, 10), 0f, 0f, 70.0f);

        CameraRigSession session = manager.mountViewer(player, state);
        assertThat(session).isInstanceOf(PacketCameraRigSession.class);
        assertThat(session.mountMode()).isEqualTo(CameraMountMode.PACKET_VIRTUAL);
    }

    @Test
    @DisplayName("CameraRigManager gracefully falls back to DisplayCameraRigSession when PacketEvents is unavailable")
    void fallsBackToDisplayRigWhenPacketEventsUnavailable() {
        Plugin plugin = mock(Plugin.class);
        PacketEventsBridge bridge = mock(PacketEventsBridge.class);
        when(bridge.isAvailable()).thenReturn(false);

        CameraRigManager manager = new CameraRigManager(plugin, CameraMountMode.PACKET_VIRTUAL, bridge);
        Player player = createMockPlayer();
        CameraState state = new CameraState(new Vector3d(10, 65, 10), 0f, 0f, 70.0f);

        CameraRigSession session = manager.mountViewer(player, state);
        assertThat(session).isInstanceOf(DisplayCameraRigSession.class);
        assertThat(session.mountMode()).isEqualTo(CameraMountMode.SERVER_DISPLAY);
    }

    @Test
    @DisplayName("CameraRigManager supports explicit SERVER_DISPLAY mode and dynamic mode switching")
    void supportsServerDisplayModeAndSwitching() {
        Plugin plugin = mock(Plugin.class);
        PacketEventsBridge bridge = mock(PacketEventsBridge.class);
        when(bridge.isAvailable()).thenReturn(true);

        CameraRigManager manager = new CameraRigManager(plugin, CameraMountMode.SERVER_DISPLAY, bridge);
        assertThat(manager.defaultMode()).isEqualTo(CameraMountMode.SERVER_DISPLAY);

        Player player = createMockPlayer();
        CameraState state = new CameraState(new Vector3d(10, 65, 10), 0f, 0f, 70.0f);

        CameraRigSession displaySession = manager.mountViewer(player, state);
        assertThat(displaySession).isInstanceOf(DisplayCameraRigSession.class);
        assertThat(displaySession.mountMode()).isEqualTo(CameraMountMode.SERVER_DISPLAY);

        // Switch to PACKET_VIRTUAL
        manager.setDefaultMode(CameraMountMode.PACKET_VIRTUAL);
        assertThat(manager.defaultMode()).isEqualTo(CameraMountMode.PACKET_VIRTUAL);

        Player player2 = createMockPlayer();
        CameraRigSession packetSession = manager.mountViewer(player2, state);
        assertThat(packetSession).isInstanceOf(PacketCameraRigSession.class);
        assertThat(packetSession.mountMode()).isEqualTo(CameraMountMode.PACKET_VIRTUAL);
    }

    @Test
    @DisplayName("CameraRigManager applies transformations and manages active session map")
    void managesActiveSessions() {
        Plugin plugin = mock(Plugin.class);
        PacketEventsBridge bridge = mock(PacketEventsBridge.class);
        when(bridge.isAvailable()).thenReturn(true);

        CameraRigManager manager = new CameraRigManager(plugin, CameraMountMode.PACKET_VIRTUAL, bridge);
        Player player = createMockPlayer();
        CameraState state = new CameraState(new Vector3d(10, 65, 10), 0f, 0f, 70.0f);

        manager.apply(List.of(player), state);
        assertThat(manager.hasSession(player.getUniqueId())).isTrue();
        assertThat(manager.activeSessions()).hasSize(1);

        manager.endSession(player.getUniqueId());
        assertThat(manager.hasSession(player.getUniqueId())).isFalse();
        assertThat(manager.activeSessions()).isEmpty();
    }
}
