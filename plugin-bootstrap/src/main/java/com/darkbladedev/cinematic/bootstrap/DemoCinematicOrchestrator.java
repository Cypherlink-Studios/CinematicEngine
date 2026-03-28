package com.darkbladedev.cinematic.bootstrap;

import com.darkbladedev.cinematic.adapters.camera.PlayerCameraOutput;
import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.dsl.registry.SceneLoader;
import com.darkbladedev.cinematic.runtime.TimelinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public final class DemoCinematicOrchestrator implements CinematicService {
    private final TimelinePlayer timelinePlayer;
    private final SceneLoader sceneLoader;
    private final Set<UUID> viewers;
    private final CameraOutput cameraOutput;
    private final Logger logger;
    private final ReentrantLock stateLock;
    private volatile ActiveCinematic activeCinematic;

    public DemoCinematicOrchestrator(TimelinePlayer timelinePlayer, SceneLoader sceneLoader, Logger logger) {
        this.timelinePlayer = Objects.requireNonNull(timelinePlayer, "timelinePlayer");
        this.sceneLoader = Objects.requireNonNull(sceneLoader, "sceneLoader");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.viewers = ConcurrentHashMap.newKeySet();
        this.cameraOutput = new PlayerCameraOutput(() -> activeViewers());
        this.stateLock = new ReentrantLock();
    }

    public void registerViewer(Player player) {
        viewers.add(player.getUniqueId());
    }

    public void unregisterViewer(Player player) {
        viewers.remove(player.getUniqueId());
    }

    @Override
    public Set<String> availableCinematics() {
        return sceneLoader.availableSceneIds();
    }

    @Override
    public CinematicActionResult play(String cinematicName) {
        stateLock.lock();
        try {
            refreshState();
            Scene scene = sceneLoader.load(cinematicName).orElse(null);
            if (scene == null) {
                return CinematicActionResult.failure("La cinemática '" + cinematicName + "' no existe.");
            }
            if (activeCinematic != null) {
                return CinematicActionResult.failure("Ya hay una cinemática en ejecución.");
            }
            timelinePlayer.play(
                    scene,
                    (currentScene, localTick) -> new ServiceTimelineContext(localTick, Map.of(CameraOutput.class, cameraOutput))
            );
            activeCinematic = new ActiveCinematic(cinematicName, timelinePlayer.currentTick(), scene.durationTicks(), false);
            logger.info("Se inició la cinemática '" + cinematicName + "'.");
            return CinematicActionResult.success("Cinemática '" + cinematicName + "' iniciada.");
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public CinematicActionResult stop() {
        stateLock.lock();
        try {
            refreshState();
            if (activeCinematic == null) {
                return CinematicActionResult.failure("No hay una cinemática en ejecución.");
            }
            timelinePlayer.clearActiveScenes();
            timelinePlayer.resume();
            activeCinematic = null;
            logger.info("Se detuvo la cinemática activa.");
            return CinematicActionResult.success("Cinemática detenida.");
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public CinematicActionResult pause() {
        stateLock.lock();
        try {
            refreshState();
            if (activeCinematic == null) {
                return CinematicActionResult.failure("No hay una cinemática en ejecución para pausar.");
            }
            if (activeCinematic.paused()) {
                return CinematicActionResult.failure("La cinemática ya está en pausa.");
            }
            timelinePlayer.pause();
            activeCinematic = activeCinematic.withPaused(true);
            logger.info("Se pausó la cinemática activa.");
            return CinematicActionResult.success("Cinemática pausada.");
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public CinematicActionResult resume() {
        stateLock.lock();
        try {
            refreshState();
            if (activeCinematic == null) {
                return CinematicActionResult.failure("No hay una cinemática en ejecución para reanudar.");
            }
            if (!activeCinematic.paused()) {
                return CinematicActionResult.failure("La cinemática no está en pausa.");
            }
            timelinePlayer.resume();
            activeCinematic = activeCinematic.withPaused(false);
            logger.info("Se reanudó la cinemática activa.");
            return CinematicActionResult.success("Cinemática reanudada.");
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public CinematicActionResult reload() {
        stateLock.lock();
        try {
            refreshState();
            if (activeCinematic != null) {
                return CinematicActionResult.failure("No se puede recargar mientras hay una cinemática activa.");
            }
            int loaded = sceneLoader.reloadAll();
            logger.info("Se recargaron " + loaded + " cinemáticas del plugin.");
            return CinematicActionResult.success("Recarga completada. Cinemáticas cargadas: " + loaded + ".");
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isRunning() {
        stateLock.lock();
        try {
            refreshState();
            return activeCinematic != null;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isPaused() {
        stateLock.lock();
        try {
            refreshState();
            return activeCinematic != null && activeCinematic.paused();
        } finally {
            stateLock.unlock();
        }
    }

    private Iterable<Player> activeViewers() {
        java.util.List<Player> players = new ArrayList<>();
        for (UUID viewerId : viewers) {
            Player player = Bukkit.getPlayer(viewerId);
            if (player != null && player.isOnline() && !player.isDead()) {
                players.add(player);
            }
        }
        return players;
    }

    private void refreshState() {
        if (activeCinematic == null) {
            return;
        }
        long elapsed = timelinePlayer.currentTick() - activeCinematic.startTick();
        boolean finishedByTime = elapsed > activeCinematic.durationTicks();
        if (finishedByTime || !timelinePlayer.hasActiveScenes()) {
            activeCinematic = null;
            timelinePlayer.resume();
        }
    }

    private record ActiveCinematic(String name, long startTick, long durationTicks, boolean paused) {
        private ActiveCinematic withPaused(boolean paused) {
            return new ActiveCinematic(name, startTick, durationTicks, paused);
        }
    }
}
