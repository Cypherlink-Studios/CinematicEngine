package com.darkbladedev.cinematic.runtime.scheduler;

@FunctionalInterface
public interface ScheduledTask {
    void cancel();
}
