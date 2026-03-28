package com.darkbladedev.cinematic.bootstrap;

import com.darkbladedev.cinematic.adapters.camera.PlayerCameraOutput;
import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.runtime.TimelinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public final class DemoCinematicOrchestrator implements CinematicService {
    private static final String DEMO_NAME = "demo";
    private final TimelinePlayer timelinePlayer;
    private final Set<UUID> viewers;
    private final CameraOutput cameraOutput;
    private final Logger logger;
    private final ReentrantLock stateLock;
    private volatile ActiveCinematic activeCinematic;

    public DemoCinematicOrchestrator(TimelinePlayer timelinePlayer, Logger logger) {
        this.timelinePlayer = Objects.requireNonNull(timelinePlayer, "timelinePlayer");
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
        return Set.of(DEMO_NAME);
    }

    @Override
    public CinematicActionResult play(String cinematicName) {
        stateLock.lock();
        try {
            refreshState();
            if (!DEMO_NAME.equalsIgnoreCase(cinematicName)) {
                return CinematicActionResult.failure("La cinemática '" + cinematicName + "' no existe.");
            }
            if (activeCinematic != null) {
                return CinematicActionResult.failure("Ya hay una cinemática en ejecución.");
            }
            Scene demoScene = buildDemoScene();
            timelinePlayer.play(
                    demoScene,
                    (scene, localTick) -> new ServiceTimelineContext(localTick, Map.of(CameraOutput.class, cameraOutput))
            );
            activeCinematic = new ActiveCinematic(DEMO_NAME, timelinePlayer.currentTick(), demoScene.durationTicks(), false);
            logger.info("Se inició la cinemática demo.");
            return CinematicActionResult.success("Cinemática demo iniciada.");
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

    private Scene buildDemoScene() {
        World world = Bukkit.getWorlds().getFirst();
        Location spawn = world.getSpawnLocation();
        Vector3d start = new Vector3d(spawn.getX() + 2.0D, spawn.getY() + 2.5D, spawn.getZ() + 2.0D);
        Vector3d middle = new Vector3d(spawn.getX() + 10.0D, spawn.getY() + 4.0D, spawn.getZ() + 2.0D);
        Vector3d end = new Vector3d(spawn.getX() + 18.0D, spawn.getY() + 3.0D, spawn.getZ() - 4.0D);
        CameraTrack track = new CameraTrack(
                "demo_camera_track",
                List.of(
                        new CameraFrame(0L, start, 45.0F, 8.0F, 70.0F, new LinearInterpolator()),
                        new CameraFrame(100L, middle, 120.0F, 0.0F, 75.0F, new LinearInterpolator()),
                        new CameraFrame(200L, end, 180.0F, -6.0F, 80.0F, new EaseInOutInterpolator())
                )
        );
        return new Scene("startup_demo_scene", 200L, List.of(track));
    }

    private Iterable<Player> activeViewers() {
        List<Player> players = new ArrayList<>();
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
