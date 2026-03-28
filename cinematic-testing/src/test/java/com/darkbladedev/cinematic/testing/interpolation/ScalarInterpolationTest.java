package com.darkbladedev.cinematic.testing.interpolation;

import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import com.darkbladedev.cinematic.core.interpolation.Interpolator;
import com.darkbladedev.cinematic.core.interpolation.LinearInterpolator;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class ScalarInterpolationTest {
    private final Interpolator linearInterpolator = new LinearInterpolator();
    private final Interpolator easeInOutInterpolator = new EaseInOutInterpolator();

    @Test
    void linearInterpolationRespectsBoundariesAndMidpoint() {
        assertThat(lerp(0.0D, 10.0D, 0.0D, linearInterpolator))
                .isCloseTo(0.0D, offset(NumericTolerance.DOUBLE_EPSILON));
        assertThat(lerp(0.0D, 10.0D, 1.0D, linearInterpolator))
                .isCloseTo(10.0D, offset(NumericTolerance.DOUBLE_EPSILON));
        assertThat(lerp(0.0D, 10.0D, 0.5D, linearInterpolator))
                .isCloseTo(5.0D, offset(NumericTolerance.DOUBLE_EPSILON));
    }

    @Test
    void scalarInterpolationClampsOutOfRangeT() {
        assertThat(lerp(3.0D, 7.0D, -0.4D, linearInterpolator))
                .isCloseTo(3.0D, offset(NumericTolerance.DOUBLE_EPSILON));
        assertThat(lerp(3.0D, 7.0D, 1.9D, linearInterpolator))
                .isCloseTo(7.0D, offset(NumericTolerance.DOUBLE_EPSILON));
        assertThat(lerp(3.0D, 7.0D, -0.4D, easeInOutInterpolator))
                .isCloseTo(3.0D, offset(NumericTolerance.DOUBLE_EPSILON));
        assertThat(lerp(3.0D, 7.0D, 1.9D, easeInOutInterpolator))
                .isCloseTo(7.0D, offset(NumericTolerance.DOUBLE_EPSILON));
    }

    @Test
    void easeInOutIsSmoothSymmetricAndWithoutOvershoot() {
        double previous = -1.0D;
        for (int step = 0; step <= 100; step++) {
            double t = step / 100.0D;
            double value = easeInOutInterpolator.interpolate(t);
            assertThat(value).isGreaterThanOrEqualTo(0.0D).isLessThanOrEqualTo(1.0D);
            assertThat(value).isGreaterThanOrEqualTo(previous - NumericTolerance.DOUBLE_EPSILON);
            previous = value;

            double symmetric = easeInOutInterpolator.interpolate(1.0D - t);
            assertThat(value + symmetric).isCloseTo(1.0D, offset(1.0E-9D));
        }

        double h = 1.0E-4D;
        double leftSlope = (easeInOutInterpolator.interpolate(0.5D) - easeInOutInterpolator.interpolate(0.5D - h)) / h;
        double rightSlope = (easeInOutInterpolator.interpolate(0.5D + h) - easeInOutInterpolator.interpolate(0.5D)) / h;
        assertThat(Math.abs(leftSlope - rightSlope)).isLessThan(1.0E-3D);
    }

    private double lerp(double from, double to, double t, Interpolator interpolator) {
        double progress = interpolator.interpolate(t);
        return from + ((to - from) * progress);
    }
}
