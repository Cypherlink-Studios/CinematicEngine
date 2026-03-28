package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CommandManager;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HelpSubcommandTest {
    @Test
    void helpWithoutArgsShowsGeneralHelp() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(sender.hasPermission("cinematic.help")).thenReturn(true);
        when(cinematicService.availableCinematics()).thenReturn(Set.of("demo"));
        CommandManager manager = new CommandManager(plugin, cinematicService);
        manager.registerDefaults();
        HelpSubcommand subcommand = new HelpSubcommand(manager);

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
        verify(sender).sendMessage("Comandos disponibles:");
    }

    @Test
    void helpWithInvalidSyntaxThrows() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        CinematicService cinematicService = mock(CinematicService.class);
        CommandManager manager = new CommandManager(plugin, cinematicService);
        HelpSubcommand subcommand = new HelpSubcommand(manager);
        CommandSender sender = mock(CommandSender.class);

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[]{"play", "extra"}));
    }

    @Test
    void helpWithSpecificSubcommandShowsDetailedHelp() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        CinematicService cinematicService = mock(CinematicService.class);
        CommandSender sender = mock(CommandSender.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(sender.hasPermission("cinematic.help")).thenReturn(true);
        when(sender.hasPermission("cinematic.play")).thenReturn(true);
        CommandManager manager = new CommandManager(plugin, cinematicService);
        manager.registerDefaults();
        HelpSubcommand subcommand = new HelpSubcommand(manager);

        CommandResult result = subcommand.execute(sender, new String[]{"play"});

        assertTrue(result.success());
        verify(sender).sendMessage("Uso: /cine play <nombre>");
    }

    @Test
    void helpTabCompleteReturnsSubcommands() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        CinematicService cinematicService = mock(CinematicService.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        CommandManager manager = new CommandManager(plugin, cinematicService);
        manager.registerDefaults();
        HelpSubcommand subcommand = new HelpSubcommand(manager);
        CommandSender sender = mock(CommandSender.class);

        List<String> suggestions = subcommand.tabComplete(sender, new String[]{""});

        assertEquals(List.of("help", "list", "pause", "play", "reload", "resume", "stop"), suggestions);
    }
}
