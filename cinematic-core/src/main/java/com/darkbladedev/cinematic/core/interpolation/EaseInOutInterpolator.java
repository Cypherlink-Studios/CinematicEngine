package com.darkbladedev.cinematic.core.interpolation;

public final class EaseInOutInterpolator implements Interpolator {
    @Override
    public double interpolate(double t) {
        double clamped = Math.max(0.0D, Math.min(1.0D, t));
        return clamped * clamped * (3.0D - (2.0D * clamped));
    }
}
