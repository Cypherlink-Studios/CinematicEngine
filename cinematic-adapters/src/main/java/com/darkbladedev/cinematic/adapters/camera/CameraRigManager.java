package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge;
import com.darkbladedev.cinematic.camera.CameraState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active cinematic camera sessions across viewers, supporting dual mounting strategies:
 * {@link CameraMountMode#PACKET_VIRTUAL} (prioritized packet-only) and
 * {@link CameraMountMode#SERVER_DISPLAY} (server-side display entity).
 */
public final class CameraRigManager {
    private final Plugin plugin;
    private final PacketEventsBridge packetBridge;
    private final Map<UUID, CameraRigSession> activeSessions = new ConcurrentHashMap<>();
    private volatile CameraMountMode defaultMode;

    public CameraRigManager(Plugin plugin) {
        this(plugin, CameraMountMode.PACKET_VIRTUAL, new PacketEventsBridge());
    }

    public CameraRigManager(Plugin plugin, CameraMountMode defaultMode) {
        this(plugin, defaultMode, new PacketEventsBridge());
    }

    public CameraRigManager(Plugin plugin, CameraMountMode defaultMode, PacketEventsBridge packetBridge) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.defaultMode = Objects.requireNonNull(defaultMode, "defaultMode");
        this.packetBridge = Objects.requireNonNull(packetBridge, "packetBridge");
    }

    public CameraMountMode defaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(CameraMountMode defaultMode) {
        this.defaultMode = Objects.requireNonNull(defaultMode, "defaultMode");
    }

    public PacketEventsBridge packetBridge() {
        return packetBridge;
    }

    public Plugin plugin() {
        return plugin;
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

    /**
     * Applies camera transform using the default mount mode.
     */
    public void apply(Iterable<Player> viewers, CameraState state) {
        apply(viewers, state, defaultMode);
    }

    /**
     * Applies camera transform specifying an explicit mount mode for this invocation.
     */
    public void apply(Iterable<Player> viewers, CameraState state, CameraMountMode mode) {
        Set<UUID> currentViewerIds = ConcurrentHashMap.newKeySet();
        for (Player viewer : viewers) {
            if (viewer == null || !viewer.isOnline() || viewer.isDead()) {
                continue;
            }
            currentViewerIds.add(viewer.getUniqueId());
            CameraRigSession session = activeSessions.computeIfAbsent(
                    viewer.getUniqueId(),
                    id -> mountViewer(viewer, state, mode)
            );
            session.update(viewer, state);
        }

        // Clean up viewers that are no longer in the viewers list
        for (UUID trackedId : activeSessions.keySet()) {
            if (!currentViewerIds.contains(trackedId)) {
                endSession(trackedId);
            }
        }
    }

    public CameraRigSession mountViewer(Player player, CameraState state) {
        return mountViewer(player, state, defaultMode);
    }

    public CameraRigSession mountViewer(Player player, CameraState state, CameraMountMode mode) {
        CameraMountMode effectiveMode = mode != null ? mode : defaultMode;

        if (effectiveMode == CameraMountMode.PACKET_VIRTUAL) {
            if (packetBridge.isAvailable()) {
                return new PacketCameraRigSession(player, state, packetBridge);
            }
            // Graceful fallback to SERVER_DISPLAY if PacketEvents is not present
            return new DisplayCameraRigSession(player, state, plugin);
        }

        return new DisplayCameraRigSession(player, state, plugin);
    }

    public void endSession(UUID playerId) {
        Player player = Bukkit.getServer() != null ? Bukkit.getPlayer(playerId) : null;
        endSession(playerId, player);
    }

    public void endSession(Player player) {
        if (player != null) {
            endSession(player.getUniqueId(), player);
        }
    }

    public void endSession(UUID playerId, Player player) {
        CameraRigSession session = activeSessions.remove(playerId);
        if (session != null) {
            session.restore(player);
        }
    }

    public void endAllSessions() {
        for (UUID playerId : activeSessions.keySet()) {
            endSession(playerId);
        }
    }
}
