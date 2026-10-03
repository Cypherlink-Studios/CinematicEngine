package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.camera.CameraState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CameraRigManager {
    private final Plugin plugin;
    private final Map<UUID, CameraRigSession> activeSessions = new ConcurrentHashMap<>();

    public CameraRigManager(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public boolean hasSession(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    public CameraRigSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public Map<UUID, CameraRigSession> activeSessions() {
        return activeSessions;
    }

    public void apply(Iterable<Player> viewers, CameraState state) {
        Set<UUID> currentViewerIds = ConcurrentHashMap.newKeySet();
        for (Player viewer : viewers) {
            if (viewer == null || !viewer.isOnline() || viewer.isDead()) {
                continue;
            }
            currentViewerIds.add(viewer.getUniqueId());
            CameraRigSession session = activeSessions.computeIfAbsent(viewer.getUniqueId(), id -> mountViewer(viewer, state));
            updateRig(viewer, session, state);
        }

        // Clean up viewers that are no longer in the viewers list
        for (UUID trackedId : activeSessions.keySet()) {
            if (!currentViewerIds.contains(trackedId)) {
                endSession(trackedId);
            }
        }
    }

    private CameraRigSession mountViewer(Player player, CameraState state) {
        Location initialLocation = new Location(
                player.getWorld(),
                state.position().x,
                state.position().y,
                state.position().z,
                state.yaw(),
                state.pitch()
        );

        ItemDisplay rigEntity = player.getWorld().spawn(initialLocation, ItemDisplay.class, display -> {
            display.setVisibleByDefault(false);
            display.setTeleportDuration(1);
            display.setGravity(false);
            display.setInvulnerable(true);
            display.setPersistent(false);
        });

        player.showEntity(plugin, rigEntity);

        CameraRigSession session = new CameraRigSession(player, rigEntity);
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(rigEntity);
        return session;
    }

    private void updateRig(Player player, CameraRigSession session, CameraState state) {
        Display rig = session.rigEntity();
        if (rig == null || !rig.isValid()) {
            Location loc = new Location(
                    player.getWorld(),
                    state.position().x,
                    state.position().y,
                    state.position().z,
                    state.yaw(),
                    state.pitch()
            );
            rig = player.getWorld().spawn(loc, ItemDisplay.class, display -> {
                display.setVisibleByDefault(false);
                display.setTeleportDuration(1);
                display.setGravity(false);
                display.setInvulnerable(true);
                display.setPersistent(false);
            });
            player.showEntity(plugin, rig);
            session.setRigEntity(rig);
            player.setSpectatorTarget(rig);
        }

        Location targetLocation = new Location(
                player.getWorld(),
                state.position().x,
                state.position().y,
                state.position().z,
                state.yaw(),
                state.pitch()
        );
        rig.teleport(targetLocation);

        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        if (player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(rig)) {
            player.setSpectatorTarget(rig);
        }
    }

    public void endSession(UUID playerId) {
        CameraRigSession session = activeSessions.remove(playerId);
        if (session != null) {
            Player player = Bukkit.getPlayer(playerId);
            session.restore(player);
        }
    }

    public void endAllSessions() {
        for (UUID playerId : activeSessions.keySet()) {
            endSession(playerId);
        }
    }
}
