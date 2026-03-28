package com.darkbladedev.cinematic.bootstrap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public final class PlayerQuitListener implements Listener {
    private final DemoCinematicOrchestrator orchestrator;

    public PlayerQuitListener(DemoCinematicOrchestrator orchestrator) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "orchestrator");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        orchestrator.unregisterViewer(event.getPlayer());
    }
}
