package com.darkbladedev.cinematic.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CinematicActionResultTest {

    @Test
    void createsLegacySuccessAndFailureResults() {
        CinematicActionResult success = CinematicActionResult.success("Operación exitosa");
        assertTrue(success.success());
        assertEquals("Operación exitosa", success.message());
        assertNull(success.messageKey());
        assertTrue(success.placeholders().isEmpty());

        CinematicActionResult failure = CinematicActionResult.failure("Error ocurrido");
        assertFalse(failure.success());
        assertEquals("Error ocurrido", failure.message());
        assertNull(failure.messageKey());
        assertTrue(failure.placeholders().isEmpty());
    }

    @Test
    void createsKeyBasedSuccessAndFailureResults() {
        CinematicActionResult success = CinematicActionResult.successKey(
                "cinematic.play.started",
                Map.of("name", "intro"),
                "Cinemática iniciada"
        );
        assertTrue(success.success());
        assertEquals("Cinemática iniciada", success.message());
        assertEquals("cinematic.play.started", success.messageKey());
        assertEquals("intro", success.placeholders().get("name"));

        CinematicActionResult failure = CinematicActionResult.failureKey(
                "cinematic.play.not_found",
                Map.of("name", "missing"),
                "No existe"
        );
        assertFalse(failure.success());
        assertEquals("No existe", failure.message());
        assertEquals("cinematic.play.not_found", failure.messageKey());
        assertEquals("missing", failure.placeholders().get("name"));
    }

    @Test
    void handlesNullPlaceholdersGracefully() {
        CinematicActionResult result = CinematicActionResult.successKey("key", null, "fallback");
        assertNotNull(result.placeholders());
        assertTrue(result.placeholders().isEmpty());
    }
}
