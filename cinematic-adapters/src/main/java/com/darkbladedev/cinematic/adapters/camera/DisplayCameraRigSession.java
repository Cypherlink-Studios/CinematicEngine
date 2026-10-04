package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.camera.CameraState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Camera rig session implementing {@link CameraMountMode#SERVER_DISPLAY}.
 * Spawns an {@link ItemDisplay} entity into the Minecraft world with client-side interpolation.
 */
public final class DisplayCameraRigSession extends AbstractCameraRigSession {
    private final Plugin plugin;
    private Display rigEntity;

    public DisplayCameraRigSession(Player player, CameraState initialState, Plugin plugin) {
        super(player);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        mount(player, initialState);
    }

    public DisplayCameraRigSession(Player player, Display rigEntity) {
        super(player);
        this.plugin = null;
        this.rigEntity = rigEntity;
    }

    private void mount(Player player, CameraState state) {
        Location initialLocation = new Location(
                player.getWorld(),
                state.position().x,
                state.position().y,
                state.position().z,
                state.yaw(),
                state.pitch()
        );

        this.rigEntity = player.getWorld().spawn(initialLocation, ItemDisplay.class, display -> {
            display.setVisibleByDefault(false);
            display.setTeleportDuration(1);
            display.setGravity(false);
            display.setInvulnerable(true);
            display.setPersistent(false);
        });

        if (plugin != null) {
            player.showEntity(plugin, rigEntity);
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(rigEntity);
    }

    @Override
    public CameraMountMode mountMode() {
        return CameraMountMode.SERVER_DISPLAY;
    }

    @Override
    public Display rigEntity() {
        return rigEntity;
    }

    public void setRigEntity(Display rigEntity) {
        this.rigEntity = rigEntity;
    }

    @Override
    public void update(Player player, CameraState state) {
        if (rigEntity == null || !rigEntity.isValid()) {
            mount(player, state);
            return;
        }

        Location targetLocation = new Location(
                player.getWorld(),
                state.position().x,
                state.position().y,
                state.position().z,
                state.yaw(),
                state.pitch()
        );
        rigEntity.teleport(targetLocation);

        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        if (player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(rigEntity)) {
            player.setSpectatorTarget(rigEntity);
        }
    }

    @Override
    public void restore(Player player) {
        if (player != null && player.isOnline()) {
            if (player.getSpectatorTarget() != null && player.getSpectatorTarget().equals(rigEntity)) {
                player.setSpectatorTarget(null);
            }
            restorePlayerState(player);
        }
        cleanup();
    }

    @Override
    public void cleanup() {
        if (rigEntity != null && rigEntity.isValid()) {
            rigEntity.remove();
            rigEntity = null;
        }
    }
}
