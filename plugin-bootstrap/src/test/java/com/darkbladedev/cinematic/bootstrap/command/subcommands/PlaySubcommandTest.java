package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaySubcommandTest {
    @Test
    void playDemoRequiresSpecificPermission() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("cinematic.demo.play")).thenReturn(false);
        when(cinematicService.availableCinematics()).thenReturn(Set.of("demo"));
        PlaySubcommand subcommand = new PlaySubcommand(cinematicService, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[]{"demo"});

        assertFalse(result.success());
    }

    @Test
    void playReturnsServiceResultWhenPermissionIsValid() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("cinematic.demo.play")).thenReturn(true);
        when(sender.getName()).thenReturn("Tester");
        when(cinematicService.play("demo")).thenReturn(CinematicActionResult.success("ok"));
        PlaySubcommand subcommand = new PlaySubcommand(cinematicService, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[]{"demo"});

        assertTrue(result.success());
    }

    @Test
    void playValidatesSyntax() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        PlaySubcommand subcommand = new PlaySubcommand(cinematicService, Logger.getLogger("test"));

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[0]));
    }

    @Test
    void playNonDemoDelegatesWithoutExtraPermissionCheck() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.getName()).thenReturn("Tester");
        when(cinematicService.play("intro")).thenReturn(CinematicActionResult.success("ok"));
        PlaySubcommand subcommand = new PlaySubcommand(cinematicService, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[]{"intro"});

        assertTrue(result.success());
    }

    @Test
    void tabCompleteReturnsAvailableCinematics() {
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(cinematicService.availableCinematics()).thenReturn(Set.of("demo", "intro"));
        PlaySubcommand subcommand = new PlaySubcommand(cinematicService, Logger.getLogger("test"));

        List<String> suggestions = subcommand.tabComplete(sender, new String[]{""});

        assertEquals(List.of("demo", "intro"), suggestions);
    }
}
