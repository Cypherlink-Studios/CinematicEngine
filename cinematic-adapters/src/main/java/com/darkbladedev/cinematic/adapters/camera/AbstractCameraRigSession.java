package com.darkbladedev.cinematic.adapters.camera;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Base implementation of {@link CameraRigSession} capturing and restoring initial player state.
 */
public abstract class AbstractCameraRigSession implements CameraRigSession {
    private final UUID playerId;
    private final Location originalLocation;
    private final GameMode originalGameMode;
    private final boolean originalAllowFlight;
    private final boolean originalFlying;

    protected AbstractCameraRigSession(Player player) {
        Objects.requireNonNull(player, "player");
        this.playerId = player.getUniqueId();
        this.originalLocation = player.getLocation().clone();
        this.originalGameMode = player.getGameMode();
        this.originalAllowFlight = player.getAllowFlight();
        this.originalFlying = player.isFlying();
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public Location originalLocation() {
        return originalLocation.clone();
    }

    @Override
    public GameMode originalGameMode() {
        return originalGameMode;
    }

    @Override
    public boolean originalAllowFlight() {
        return originalAllowFlight;
    }

    @Override
    public boolean originalFlying() {
        return originalFlying;
    }

    /**
     * Restores the viewer's GameMode, flight abilities, flying status, and location.
     *
     * @param player player to restore
     */
    protected void restorePlayerState(Player player) {
        if (player != null && player.isOnline()) {
            player.setGameMode(originalGameMode);
            player.setAllowFlight(originalAllowFlight);
            player.setFlying(originalFlying);
            player.teleport(originalLocation);
        }
    }
}
