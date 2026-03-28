package com.darkbladedev.cinematic.core.runtime;

import java.util.Optional;

public interface TimelineContext {
    long currentTick();

    <T> Optional<T> getService(Class<T> serviceType);
}
