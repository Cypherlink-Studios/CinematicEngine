package com.darkbladedev.cinematic.testing.support;

import com.darkbladedev.cinematic.adapters.runtime.ServiceTimelineContext;
import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.runtime.TimelineContextProvider;
import com.darkbladedev.cinematic.runtime.TimelinePlayer;
import com.darkbladedev.cinematic.runtime.scheduler.ScheduledTask;
import com.darkbladedev.cinematic.runtime.scheduler.TickScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TestTimelineRunner {
    private final ManualTickScheduler scheduler;
    private final TimelinePlayer timelinePlayer;

    public TestTimelineRunner() {
        this.scheduler = new ManualTickScheduler();
        this.timelinePlayer = new TimelinePlayer(scheduler);
        this.timelinePlayer.start();
    }

    public List<FrameSnapshot> runCamera(Scene scene, long ticksToAdvance) {
        List<FrameSnapshot> snapshots = new ArrayList<>();
        CameraOutput output = cameraState -> snapshots.add(FrameSnapshot.of(timelinePlayer.currentTick(), cameraState));
        TimelineContextProvider contextProvider = (ignoredScene, localTick) ->
                new ServiceTimelineContext(localTick, Map.of(CameraOutput.class, output));
        timelinePlayer.play(scene, contextProvider);
        advanceTicks(ticksToAdvance);
        return List.copyOf(snapshots);
    }

    public void play(Scene scene, TimelineContextProvider contextProvider) {
        timelinePlayer.play(scene, contextProvider);
    }

    public void advanceTicks(long ticks) {
        for (long i = 0; i < ticks; i++) {
            scheduler.advanceOneTick();
        }
    }

    public long currentTick() {
        return timelinePlayer.currentTick();
    }

    public void stop() {
        timelinePlayer.stop();
    }

    public static TimelineContext emptyContext(long tick) {
        return new ServiceTimelineContext(tick, Map.of());
    }

    private static final class ManualTickScheduler implements TickScheduler {
        private Runnable scheduledRunnable;
        private boolean cancelled;

        @Override
        public ScheduledTask scheduleRepeating(Runnable task, long intervalTicks) {
            Objects.requireNonNull(task, "task");
            this.scheduledRunnable = task;
            this.cancelled = false;
            return () -> cancelled = true;
        }

        void advanceOneTick() {
            if (cancelled) {
                return;
            }
            Optional.ofNullable(scheduledRunnable).ifPresent(Runnable::run);
        }
    }
}
