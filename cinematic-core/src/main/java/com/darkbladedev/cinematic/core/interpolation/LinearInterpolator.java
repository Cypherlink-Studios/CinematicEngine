package com.darkbladedev.cinematic.core.interpolation;

public final class LinearInterpolator implements Interpolator {
    @Override
    public double interpolate(double t) {
        return clamp01(t);
    }

    private double clamp01(double t) {
        return Math.max(0.0D, Math.min(1.0D, t));
    }
}
