package com.darkbladedev.cinematic.runtime.scheduler;

public interface TickScheduler {
    ScheduledTask scheduleRepeating(Runnable task, long intervalTicks);
}
