package com.darkbladedev.cinematic.testing.support;

import org.joml.Vector3d;

import static org.assertj.core.api.Assertions.assertThat;

public final class VectorAssertions {
    private VectorAssertions() {
    }

    public static void assertVectorEquals(Vector3d expected, Vector3d actual, double epsilon) {
        assertThat(actual.x).isCloseTo(expected.x, org.assertj.core.data.Offset.offset(epsilon));
        assertThat(actual.y).isCloseTo(expected.y, org.assertj.core.data.Offset.offset(epsilon));
        assertThat(actual.z).isCloseTo(expected.z, org.assertj.core.data.Offset.offset(epsilon));
    }
}
