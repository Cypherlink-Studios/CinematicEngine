package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListSubcommandTest {
    @Test
    void listReturnsAvailableCinematics() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(cinematicService.availableCinematics()).thenReturn(Set.of("demo"));
        ListSubcommand subcommand = new ListSubcommand(cinematicService);

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
        assertTrue(result.message().contains("demo"));
    }

    @Test
    void listValidatesSyntax() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        ListSubcommand subcommand = new ListSubcommand(cinematicService);

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[]{"extra"}));
    }

    @Test
    void listHandlesEmptyCatalog() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(cinematicService.availableCinematics()).thenReturn(Set.of());
        ListSubcommand subcommand = new ListSubcommand(cinematicService);

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
        assertTrue(result.message().contains("No hay cinemáticas"));
    }
}
