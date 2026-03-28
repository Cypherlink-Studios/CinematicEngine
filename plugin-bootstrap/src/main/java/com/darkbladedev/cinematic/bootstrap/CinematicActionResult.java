package com.darkbladedev.cinematic.bootstrap;

public record CinematicActionResult(boolean success, String message) {
    public static CinematicActionResult success(String message) {
        return new CinematicActionResult(true, message);
    }

    public static CinematicActionResult failure(String message) {
        return new CinematicActionResult(false, message);
    }
}
