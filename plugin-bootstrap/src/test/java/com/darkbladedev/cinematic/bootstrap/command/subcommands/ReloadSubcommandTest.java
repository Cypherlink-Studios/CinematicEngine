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

class ReloadSubcommandTest {
    @Test
    void reloadDelegatesToService() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.getName()).thenReturn("Tester");
        when(cinematicService.reload()).thenReturn(CinematicActionResult.success("ok"));
        ReloadSubcommand subcommand = new ReloadSubcommand(cinematicService, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
    }

    @Test
    void reloadValidatesSyntax() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        ReloadSubcommand subcommand = new ReloadSubcommand(cinematicService, Logger.getLogger("test"));

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[]{"extra"}));
    }
}
