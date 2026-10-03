package com.darkbladedev.cinematic.core.spline;

import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Centripetal Catmull-Rom spline implementation providing continuous C1
 * curves through 3D waypoints without self-intersections or cusp overshoots.
 */
public final class CatmullRomSpline {
    public static final double DEFAULT_CENTRIPETAL_ALPHA = 0.5D;
    private static final double EPSILON = 1e-9D;

    private final List<Vector3d> points;
    private final double alpha;

    public CatmullRomSpline(List<Vector3d> points) {
        this(points, DEFAULT_CENTRIPETAL_ALPHA);
    }

    public CatmullRomSpline(List<Vector3d> points, double alpha) {
        Objects.requireNonNull(points, "points");
        if (points.isEmpty()) {
            throw new IllegalArgumentException("CatmullRomSpline requires at least one control point");
        }
        this.points = points.stream().map(Vector3d::new).toList();
        this.alpha = alpha;
    }

    public List<Vector3d> points() {
        return points;
    }

    public double alpha() {
        return alpha;
    }

    /**
     * Evaluates the spline along normalized range [0.0, 1.0].
     */
    public Vector3d evaluate(double t) {
        if (points.size() == 1) {
            return new Vector3d(points.getFirst());
        }
        if (t <= 0.0D) {
            return new Vector3d(points.getFirst());
        }
        if (t >= 1.0D) {
            return new Vector3d(points.getLast());
        }

        int segmentCount = points.size() - 1;
        double scaled = t * segmentCount;
        int index = (int) Math.floor(scaled);
        if (index >= segmentCount) {
            index = segmentCount - 1;
        }
        double localProgress = scaled - index;

        Vector3d p1 = points.get(index);
        Vector3d p2 = points.get(index + 1);
        Vector3d p0 = index > 0 ? points.get(index - 1) : new Vector3d(p1).mul(2.0D).sub(p2);
        Vector3d p3 = (index + 2 < points.size()) ? points.get(index + 2) : new Vector3d(p2).mul(2.0D).sub(p1);

        return interpolate(p0, p1, p2, p3, localProgress, alpha);
    }

    /**
     * Evaluates the velocity tangent along normalized range [0.0, 1.0].
     */
    public Vector3d evaluateTangent(double t) {
        double delta = 1e-4D;
        double t1 = Math.max(0.0D, t - delta);
        double t2 = Math.min(1.0D, t + delta);
        if (t2 - t1 < EPSILON) {
            return new Vector3d(0.0D, 0.0D, 0.0D);
        }
        Vector3d pos1 = evaluate(t1);
        Vector3d pos2 = evaluate(t2);
        return new Vector3d(pos2).sub(pos1).div(t2 - t1);
    }

    /**
     * Interpolates between p1 and p2 using p0 and p3 as tangent guides.
     */
    public static Vector3d interpolate(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double progress, double alpha) {
        if (progress <= 0.0D) {
            return new Vector3d(p1);
        }
        if (progress >= 1.0D) {
            return new Vector3d(p2);
        }

        double d01 = p0.distance(p1);
        double d12 = p1.distance(p2);
        double d23 = p2.distance(p3);

        // If p1 and p2 are coincident, return p1 safely
        if (d12 < EPSILON) {
            return new Vector3d(p1);
        }

        double t0 = 0.0D;
        double t1 = t0 + Math.pow(Math.max(d01, EPSILON), alpha);
        double t2 = t1 + Math.pow(d12, alpha);
        double t3 = t2 + Math.pow(Math.max(d23, EPSILON), alpha);

        double t = t1 + progress * (t2 - t1);

        Vector3d a1 = interpolateLinear(p0, p1, t0, t1, t);
        Vector3d a2 = interpolateLinear(p1, p2, t1, t2, t);
        Vector3d a3 = interpolateLinear(p2, p3, t2, t3, t);

        Vector3d b1 = interpolateLinear(a1, a2, t0, t2, t);
        Vector3d b2 = interpolateLinear(a2, a3, t1, t3, t);

        return interpolateLinear(b1, b2, t1, t2, t);
    }

    public static Vector3d interpolate(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double progress) {
        return interpolate(p0, p1, p2, p3, progress, DEFAULT_CENTRIPETAL_ALPHA);
    }

    private static Vector3d interpolateLinear(Vector3d pA, Vector3d pB, double tA, double tB, double t) {
        double dt = tB - tA;
        if (Math.abs(dt) < EPSILON) {
            return new Vector3d(pA);
        }
        double weightB = (t - tA) / dt;
        double weightA = 1.0D - weightB;
        return new Vector3d(
                pA.x * weightA + pB.x * weightB,
                pA.y * weightA + pB.y * weightB,
                pA.z * weightA + pB.z * weightB
        );
    }
}
