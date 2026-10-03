package com.darkbladedev.cinematic.testing.spline;

import com.darkbladedev.cinematic.core.spline.CatmullRomSpline;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import com.darkbladedev.cinematic.testing.support.VectorAssertions;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatmullRomSplineTest {

    @Test
    void splinePassesExactlyThroughControlPoints() {
        Vector3d p0 = new Vector3d(0.0D, 64.0D, 0.0D);
        Vector3d p1 = new Vector3d(10.0D, 70.0D, 5.0D);
        Vector3d p2 = new Vector3d(25.0D, 68.0D, -10.0D);
        Vector3d p3 = new Vector3d(40.0D, 65.0D, 0.0D);

        CatmullRomSpline spline = new CatmullRomSpline(List.of(p0, p1, p2, p3));

        VectorAssertions.assertVectorEquals(p0, spline.evaluate(0.0D), NumericTolerance.DOUBLE_EPSILON);
        VectorAssertions.assertVectorEquals(p1, spline.evaluate(1.0D / 3.0D), 1e-4D);
        VectorAssertions.assertVectorEquals(p2, spline.evaluate(2.0D / 3.0D), 1e-4D);
        VectorAssertions.assertVectorEquals(p3, spline.evaluate(1.0D), NumericTolerance.DOUBLE_EPSILON);
    }

    @Test
    void splineHandlesSinglePointAndTwoPoints() {
        Vector3d single = new Vector3d(5.0D, 10.0D, 15.0D);
        CatmullRomSpline singleSpline = new CatmullRomSpline(List.of(single));
        VectorAssertions.assertVectorEquals(single, singleSpline.evaluate(0.5D), NumericTolerance.DOUBLE_EPSILON);

        Vector3d start = new Vector3d(0.0D, 0.0D, 0.0D);
        Vector3d end = new Vector3d(10.0D, 10.0D, 10.0D);
        CatmullRomSpline twoPointSpline = new CatmullRomSpline(List.of(start, end));
        Vector3d mid = twoPointSpline.evaluate(0.5D);
        assertThat(mid.x).isCloseTo(5.0D, org.assertj.core.data.Offset.offset(0.1D));
    }

    @Test
    void identicalConsecutivePointsDoNotCauseNanOrDivideByZero() {
        Vector3d p0 = new Vector3d(10.0D, 64.0D, 10.0D);
        Vector3d p1 = new Vector3d(10.0D, 64.0D, 10.0D); // identical
        Vector3d p2 = new Vector3d(20.0D, 65.0D, 20.0D);

        CatmullRomSpline spline = new CatmullRomSpline(List.of(p0, p1, p2));

        for (double t = 0.0D; t <= 1.0D; t += 0.1D) {
            Vector3d result = spline.evaluate(t);
            assertThat(Double.isNaN(result.x)).isFalse();
            assertThat(Double.isNaN(result.y)).isFalse();
            assertThat(Double.isNaN(result.z)).isFalse();
            assertThat(Double.isInfinite(result.x)).isFalse();
        }
    }

    @Test
    void evaluateTangentProducesContinuousMotionDirection() {
        Vector3d p0 = new Vector3d(0.0D, 64.0D, 0.0D);
        Vector3d p1 = new Vector3d(10.0D, 64.0D, 0.0D);
        Vector3d p2 = new Vector3d(20.0D, 64.0D, 10.0D);

        CatmullRomSpline spline = new CatmullRomSpline(List.of(p0, p1, p2));
        Vector3d tangent = spline.evaluateTangent(0.2D);

        assertThat(tangent.x).isGreaterThan(0.0D);
        assertThat(Double.isNaN(tangent.x)).isFalse();
    }
}
