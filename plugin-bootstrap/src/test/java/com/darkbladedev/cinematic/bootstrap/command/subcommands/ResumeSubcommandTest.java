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

class ResumeSubcommandTest {
    @Test
    void resumeDelegatesToService() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.getName()).thenReturn("Tester");
        when(cinematicService.resume()).thenReturn(CinematicActionResult.success("resumed"));
        ResumeSubcommand subcommand = new ResumeSubcommand(cinematicService, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
    }

    @Test
    void resumeValidatesSyntax() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        ResumeSubcommand subcommand = new ResumeSubcommand(cinematicService, Logger.getLogger("test"));

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[]{"extra"}));
    }
}
