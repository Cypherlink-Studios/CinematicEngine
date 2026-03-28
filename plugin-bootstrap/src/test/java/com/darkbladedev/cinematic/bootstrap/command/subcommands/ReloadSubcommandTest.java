package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReloadSubcommandTest {
    @Test
    void reloadDelegatesToPlugin() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        CommandSender sender = mock(CommandSender.class);
        when(sender.getName()).thenReturn("Tester");
        doNothing().when(plugin).reloadConfig();
        ReloadSubcommand subcommand = new ReloadSubcommand(plugin, Logger.getLogger("test"));

        CommandResult result = subcommand.execute(sender, new String[0]);

        assertTrue(result.success());
    }

    @Test
    void reloadValidatesSyntax() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        CommandSender sender = mock(CommandSender.class);
        ReloadSubcommand subcommand = new ReloadSubcommand(plugin, Logger.getLogger("test"));

        assertThrows(IllegalArgumentException.class, () -> subcommand.execute(sender, new String[]{"extra"}));
    }
}
