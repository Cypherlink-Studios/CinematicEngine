package com.darkbladedev.cinematic.runtime;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.runtime.scheduler.ScheduledTask;
import com.darkbladedev.cinematic.runtime.scheduler.TickScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TimelinePlayer {
    private final TickScheduler scheduler;
    private final List<ActiveScene> activeScenes;
    private long currentTick;
    private ScheduledTask scheduledTask;
    private boolean paused;

    public TimelinePlayer(TickScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.activeScenes = new ArrayList<>();
    }

    public synchronized void start() {
        if (scheduledTask != null) {
            return;
        }
        scheduledTask = scheduler.scheduleRepeating(this::advanceTick, 1L);
    }

    public synchronized void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
        activeScenes.clear();
        paused = false;
    }

    public synchronized void play(Scene scene, TimelineContext context) {
        play(scene, (currentScene, localTick) -> context);
    }

    public synchronized void play(Scene scene, TimelineContextProvider contextProvider) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(contextProvider, "contextProvider");
        activeScenes.add(new ActiveScene(scene, currentTick, contextProvider));
    }

    public synchronized long currentTick() {
        return currentTick;
    }

    public synchronized int activeScenes() {
        return activeScenes.size();
    }

    public synchronized boolean hasActiveScenes() {
        return !activeScenes.isEmpty();
    }

    public synchronized void clearActiveScenes() {
        activeScenes.clear();
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    private synchronized void advanceTick() {
        if (paused) {
            return;
        }
        currentTick++;
        if (activeScenes.isEmpty()) {
            return;
        }
        List<ActiveScene> finishedScenes = new ArrayList<>();
        for (ActiveScene activeScene : activeScenes) {
            long localTick = currentTick - activeScene.startTick();
            if (localTick > activeScene.scene().durationTicks()) {
                finishedScenes.add(activeScene);
                continue;
            }
            TimelineContext context = activeScene.contextProvider().create(activeScene.scene(), localTick);
            activeScene.scene().evaluate(localTick, context);
        }
        if (!finishedScenes.isEmpty()) {
            activeScenes.removeAll(finishedScenes);
        }
    }

    private record ActiveScene(Scene scene, long startTick, TimelineContextProvider contextProvider) {
    }
}
