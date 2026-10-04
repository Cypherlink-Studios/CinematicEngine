package com.darkbladedev.cinematic.bootstrap;

import java.util.Collections;
import java.util.Map;

public record CinematicActionResult(
        boolean success,
        String message,
        String messageKey,
        Map<String, Object> placeholders
) {
    public CinematicActionResult(boolean success, String message) {
        this(success, message, null, Collections.emptyMap());
    }

    public static CinematicActionResult success(String message) {
        return new CinematicActionResult(true, message, null, Collections.emptyMap());
    }

    public static CinematicActionResult failure(String message) {
        return new CinematicActionResult(false, message, null, Collections.emptyMap());
    }

    public static CinematicActionResult successKey(String messageKey, Map<String, Object> placeholders, String fallbackMessage) {
        return new CinematicActionResult(true, fallbackMessage, messageKey, placeholders != null ? Map.copyOf(placeholders) : Collections.emptyMap());
    }

    public static CinematicActionResult successKey(String messageKey, String fallbackMessage) {
        return successKey(messageKey, Collections.emptyMap(), fallbackMessage);
    }

    public static CinematicActionResult failureKey(String messageKey, Map<String, Object> placeholders, String fallbackMessage) {
        return new CinematicActionResult(false, fallbackMessage, messageKey, placeholders != null ? Map.copyOf(placeholders) : Collections.emptyMap());
    }

    public static CinematicActionResult failureKey(String messageKey, String fallbackMessage) {
        return failureKey(messageKey, Collections.emptyMap(), fallbackMessage);
    }
}
