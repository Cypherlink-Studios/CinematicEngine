package com.darkbladedev.cinematic.core.interpolation;

@FunctionalInterface
public interface Interpolator {
    double interpolate(double t);
}
