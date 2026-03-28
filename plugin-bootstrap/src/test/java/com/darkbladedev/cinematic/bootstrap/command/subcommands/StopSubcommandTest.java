package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StopSubcommandTest {
    @Test
    void stopDelegatesToService() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.getName()).thenReturn("Tester");
        when(cinematicService.stop()).thenReturn(CinematicActionResult.success("stopped"));
        StopSubcommand subcommand = new StopSubcommand(cinematicService, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
    }

    @Test
    void stopValidatesSyntax() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        StopSubcommand subcommand = new StopSubcommand(cinematicService, Logger.getLogger("test"));

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[]{"extra"}));
    }
}
