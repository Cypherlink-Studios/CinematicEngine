package com.darkbladedev.cinematic.dsl.mapper;

public final class SceneMappingException extends RuntimeException {
    public SceneMappingException(String message) {
        super(message);
    }

    public SceneMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
