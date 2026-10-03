package com.darkbladedev.cinematic.adapters.camera;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Objects;

public final class SpectatorSafetyListener implements Listener {
    private final CameraRigManager rigManager;

    public SpectatorSafetyListener(CameraRigManager rigManager) {
        this.rigManager = Objects.requireNonNull(rigManager, "rigManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking() && rigManager.hasSession(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerStopSpectating(PlayerStopSpectatingEntityEvent event) {
        if (rigManager.hasSession(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (rigManager.hasSession(player.getUniqueId())) {
            rigManager.endSession(player.getUniqueId());
        }
    }
}
