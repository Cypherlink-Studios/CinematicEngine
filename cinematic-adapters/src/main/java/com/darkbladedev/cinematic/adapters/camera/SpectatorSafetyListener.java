package com.darkbladedev.cinematic.adapters.camera;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Objects;

/**
 * Listener that prevents players in active camera rigs from dismounting or exiting spectator view,
 * covering both packet-only virtual camera sessions and world display entity sessions.
 */
public final class SpectatorSafetyListener implements Listener {
    private final CameraRigManager rigManager;

    public SpectatorSafetyListener(CameraRigManager rigManager) {
        this.rigManager = Objects.requireNonNull(rigManager, "rigManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking() && rigManager.hasSession(player.getUniqueId())) {
            event.setCancelled(true);
            CameraRigSession session = rigManager.getSession(player.getUniqueId());
            if (session instanceof PacketCameraRigSession packetSession) {
                packetSession.remount(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerStopSpectating(PlayerStopSpectatingEntityEvent event) {
        Player player = event.getPlayer();
        if (rigManager.hasSession(player.getUniqueId())) {
            event.setCancelled(true);
            CameraRigSession session = rigManager.getSession(player.getUniqueId());
            if (session instanceof PacketCameraRigSession packetSession) {
                packetSession.remount(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (rigManager.hasSession(player.getUniqueId())) {
            rigManager.endSession(player.getUniqueId(), player);
        }
    }
}
