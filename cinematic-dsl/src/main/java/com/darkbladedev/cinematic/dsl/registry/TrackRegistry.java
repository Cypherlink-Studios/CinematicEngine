package com.darkbladedev.cinematic.dsl.registry;

import com.darkbladedev.cinematic.dsl.validator.TrackValidator;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TrackRegistry {
    private final Map<String, TrackFactory> factories;
    private final Map<String, TrackValidator> validators;

    public TrackRegistry() {
        this.factories = new ConcurrentHashMap<>();
        this.validators = new ConcurrentHashMap<>();
    }

    public void register(String type, TrackFactory factory, TrackValidator validator) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(validator, "validator");
        String normalizedType = normalize(type);
        factories.put(normalizedType, factory);
        validators.put(normalizedType, validator);
    }

    public Optional<TrackFactory> factoryFor(String type) {
        return Optional.ofNullable(factories.get(normalize(type)));
    }

    public Optional<TrackValidator> validatorFor(String type) {
        return Optional.ofNullable(validators.get(normalize(type)));
    }

    public boolean contains(String type) {
        return factories.containsKey(normalize(type));
    }

    public Set<String> supportedTypes() {
        return Set.copyOf(factories.keySet());
    }

    private String normalize(String rawType) {
        return rawType == null ? "" : rawType.trim().toLowerCase(Locale.ROOT);
    }
}
