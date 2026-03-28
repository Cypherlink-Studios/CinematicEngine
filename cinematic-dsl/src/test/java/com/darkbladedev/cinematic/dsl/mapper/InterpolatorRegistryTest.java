package com.darkbladedev.cinematic.dsl.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterpolatorRegistryTest {
    @Test
    void resolvesBuiltInInterpolators() {
        InterpolatorRegistry registry = InterpolatorRegistry.defaultRegistry();

        assertThat(registry.resolve("linear").interpolate(0.5D)).isEqualTo(0.5D);
        assertThat(registry.resolve("ease_in_out").interpolate(0.5D)).isEqualTo(0.5D);
    }

    @Test
    void rejectsUnknownInterpolator() {
        InterpolatorRegistry registry = InterpolatorRegistry.defaultRegistry();

        assertThatThrownBy(() -> registry.resolve("unknown"))
                .isInstanceOf(SceneMappingException.class)
                .hasMessageContaining("Interpolador no soportado");
    }
}
