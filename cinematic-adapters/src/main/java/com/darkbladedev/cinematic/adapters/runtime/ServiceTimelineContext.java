package com.darkbladedev.cinematic.adapters.runtime;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ServiceTimelineContext implements TimelineContext {
    private final long currentTick;
    private final Map<Class<?>, Object> services;

    public ServiceTimelineContext(long currentTick, Map<Class<?>, Object> services) {
        this.currentTick = currentTick;
        this.services = Map.copyOf(Objects.requireNonNull(services, "services"));
    }

    @Override
    public long currentTick() {
        return currentTick;
    }

    @Override
    public <T> Optional<T> getService(Class<T> serviceType) {
        Object service = services.get(serviceType);
        if (service == null) {
            return Optional.empty();
        }
        return Optional.of(serviceType.cast(service));
    }
}
