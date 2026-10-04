package com.darkbladedev.cinematic.bootstrap.command;

import java.util.Collections;
import java.util.Map;

public record CommandResult(
        boolean success,
        String message,
        String messageKey,
        Map<String, Object> placeholders
) {
    public CommandResult(boolean success, String message) {
        this(success, message, null, Collections.emptyMap());
    }

    public static CommandResult success(String message) {
        return new CommandResult(true, message, null, Collections.emptyMap());
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message, null, Collections.emptyMap());
    }

    public static CommandResult successKey(String messageKey, Map<String, Object> placeholders, String fallbackMessage) {
        return new CommandResult(true, fallbackMessage, messageKey, placeholders != null ? Map.copyOf(placeholders) : Collections.emptyMap());
    }

    public static CommandResult successKey(String messageKey, String fallbackMessage) {
        return successKey(messageKey, Collections.emptyMap(), fallbackMessage);
    }

    public static CommandResult failureKey(String messageKey, Map<String, Object> placeholders, String fallbackMessage) {
        return new CommandResult(false, fallbackMessage, messageKey, placeholders != null ? Map.copyOf(placeholders) : Collections.emptyMap());
    }

    public static CommandResult failureKey(String messageKey, String fallbackMessage) {
        return failureKey(messageKey, Collections.emptyMap(), fallbackMessage);
    }
}
