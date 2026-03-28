package com.darkbladedev.cinematic.bootstrap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public final class PlayerJoinListener implements Listener {
    private final DemoCinematicOrchestrator orchestrator;

    public PlayerJoinListener(DemoCinematicOrchestrator orchestrator) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "orchestrator");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        orchestrator.registerViewer(event.getPlayer());
    }
}
