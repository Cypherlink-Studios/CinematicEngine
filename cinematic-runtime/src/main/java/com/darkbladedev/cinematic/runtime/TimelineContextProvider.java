package com.darkbladedev.cinematic.runtime;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;

@FunctionalInterface
public interface TimelineContextProvider {
    TimelineContext create(Scene scene, long localTick);
}
