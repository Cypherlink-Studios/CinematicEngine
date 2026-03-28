package com.darkbladedev.cinematic.bootstrap;

import java.util.Set;

public interface CinematicService {
    Set<String> availableCinematics();

    CinematicActionResult play(String cinematicName);

    CinematicActionResult stop();

    CinematicActionResult pause();

    CinematicActionResult resume();

    CinematicActionResult reload();

    boolean isRunning();

    boolean isPaused();
}
