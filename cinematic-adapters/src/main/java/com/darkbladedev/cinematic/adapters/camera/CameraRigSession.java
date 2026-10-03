package com.darkbladedev.cinematic.adapters.camera;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class CameraRigSession {
    private final UUID playerId;
    private final Location originalLocation;
    private final GameMode originalGameMode;
    private final boolean originalAllowFlight;
    private final boolean originalFlying;
    private Display rigEntity;

    public CameraRigSession(Player player, Display rigEntity) {
        Objects.requireNonNull(player, "player");
        this.playerId = player.getUniqueId();
        this.originalLocation = player.getLocation().clone();
        this.originalGameMode = player.getGameMode();
        this.originalAllowFlight = player.getAllowFlight();
        this.originalFlying = player.isFlying();
        this.rigEntity = rigEntity;
    }

    public UUID playerId() {
        return playerId;
    }

    public Location originalLocation() {
        return originalLocation.clone();
    }

    public GameMode originalGameMode() {
        return originalGameMode;
    }

    public boolean originalAllowFlight() {
        return originalAllowFlight;
    }

    public boolean originalFlying() {
        return originalFlying;
    }

    public Display rigEntity() {
        return rigEntity;
    }

    public void setRigEntity(Display rigEntity) {
        this.rigEntity = rigEntity;
    }

    public void restore(Player player) {
        if (player != null && player.isOnline()) {
            if (player.getSpectatorTarget() != null && player.getSpectatorTarget().equals(rigEntity)) {
                player.setSpectatorTarget(null);
            }
            player.setGameMode(originalGameMode);
            player.setAllowFlight(originalAllowFlight);
            player.setFlying(originalFlying);
            player.teleport(originalLocation);
        }
        cleanupEntity();
    }

    public void cleanupEntity() {
        if (rigEntity != null && rigEntity.isValid()) {
            rigEntity.remove();
            rigEntity = null;
        }
    }
}
