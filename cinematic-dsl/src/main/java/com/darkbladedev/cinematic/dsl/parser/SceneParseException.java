package com.darkbladedev.cinematic.dsl.parser;

public final class SceneParseException extends RuntimeException {
    public SceneParseException(String message) {
        super(message);
    }

    public SceneParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
