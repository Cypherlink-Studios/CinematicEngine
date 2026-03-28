package com.darkbladedev.cinematic.dsl.mapper;

import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class InterpolatorRegistry {
    private final Map<String, Supplier<Interpolator>> factories;

    public InterpolatorRegistry() {
        this.factories = new ConcurrentHashMap<>();
    }

    public static InterpolatorRegistry defaultRegistry() {
        InterpolatorRegistry registry = new InterpolatorRegistry();
        registry.register("linear", LinearInterpolator::new);
        registry.register("ease_in_out", EaseInOutInterpolator::new);
        registry.register("easeInOut", EaseInOutInterpolator::new);
        return registry;
    }

    public void register(String key, Supplier<Interpolator> supplier) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(supplier, "supplier");
        factories.put(normalize(key), supplier);
    }

    public Interpolator resolveOrDefault(String key) {
        if (key == null || key.isBlank()) {
            return resolve("linear");
        }
        return resolve(key);
    }

    public Interpolator resolve(String key) {
        Supplier<Interpolator> supplier = factories.get(normalize(key));
        if (supplier == null) {
            throw new SceneMappingException("Interpolador no soportado: " + key);
        }
        return supplier.get();
    }

    public boolean contains(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return factories.containsKey(normalize(key));
    }

    public Set<String> supportedInterpolators() {
        return Set.copyOf(factories.keySet());
    }

    private String normalize(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }
}
