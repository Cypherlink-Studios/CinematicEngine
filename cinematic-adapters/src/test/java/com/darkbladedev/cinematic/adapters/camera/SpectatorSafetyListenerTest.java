package com.darkbladedev.cinematic.adapters.camera;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpectatorSafetyListenerTest {

    @Test
    @DisplayName("SpectatorSafetyListener cancels sneak and remounts packet camera")
    void cancelsSneakAndRemounts() {
        CameraRigManager rigManager = mock(CameraRigManager.class);
        SpectatorSafetyListener listener = new SpectatorSafetyListener(rigManager);

        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);

        PacketCameraRigSession session = mock(PacketCameraRigSession.class);
        when(rigManager.hasSession(uuid)).thenReturn(true);
        when(rigManager.getSession(uuid)).thenReturn(session);

        PlayerToggleSneakEvent sneakEvent = new PlayerToggleSneakEvent(player, true);
        listener.onPlayerToggleSneak(sneakEvent);

        assertThat(sneakEvent.isCancelled()).isTrue();
        verify(session).remount(player);
    }

    @Test
    @DisplayName("SpectatorSafetyListener cancels spectator stop event and remounts")
    void cancelsStopSpectating() {
        CameraRigManager rigManager = mock(CameraRigManager.class);
        SpectatorSafetyListener listener = new SpectatorSafetyListener(rigManager);

        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);

        PacketCameraRigSession session = mock(PacketCameraRigSession.class);
        when(rigManager.hasSession(uuid)).thenReturn(true);
        when(rigManager.getSession(uuid)).thenReturn(session);

        ItemDisplay spectated = mock(ItemDisplay.class);
        PlayerStopSpectatingEntityEvent stopEvent = new PlayerStopSpectatingEntityEvent(player, spectated);
        listener.onPlayerStopSpectating(stopEvent);

        assertThat(stopEvent.isCancelled()).isTrue();
        verify(session).remount(player);
    }

    @Test
    @DisplayName("SpectatorSafetyListener ends session on player quit")
    void endsSessionOnQuit() {
        CameraRigManager rigManager = mock(CameraRigManager.class);
        SpectatorSafetyListener listener = new SpectatorSafetyListener(rigManager);

        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(rigManager.hasSession(uuid)).thenReturn(true);

        PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, (net.kyori.adventure.text.Component) null);
        listener.onPlayerQuit(quitEvent);

        verify(rigManager).endSession(uuid, player);
    }

    @Test
    @DisplayName("SpectatorSafetyListener ignores players without an active rig session")
    void ignoresUntrackedPlayers() {
        CameraRigManager rigManager = mock(CameraRigManager.class);
        SpectatorSafetyListener listener = new SpectatorSafetyListener(rigManager);

        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(rigManager.hasSession(uuid)).thenReturn(false);

        PlayerToggleSneakEvent sneakEvent = new PlayerToggleSneakEvent(player, true);
        listener.onPlayerToggleSneak(sneakEvent);

        assertThat(sneakEvent.isCancelled()).isFalse();
        verify(rigManager, never()).getSession(uuid);
    }
}
