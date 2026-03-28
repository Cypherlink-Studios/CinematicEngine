package com.darkbladedev.cinematic.core.model;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;

public interface Track {
    String id();

    long startTick();

    long endTick();

    default boolean isActiveAt(long tick) {
        return tick >= startTick() && tick <= endTick();
    }

    void evaluate(long tick, TimelineContext context);
}
