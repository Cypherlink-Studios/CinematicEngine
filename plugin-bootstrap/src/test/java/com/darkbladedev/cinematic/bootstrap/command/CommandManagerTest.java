package com.darkbladedev.cinematic.bootstrap.command;

import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.i18n.MessageService;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandManagerTest {

    private JavaPlugin plugin;
    private CinematicService cinematicService;
    private MessageService messageService;
    private CommandManager commandManager;
    private CommandSender sender;
    private Command command;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        cinematicService = Mockito.mock(CinematicService.class);
        messageService = Mockito.mock(MessageService.class);
        when(messageService.resolveLanguage(any())).thenReturn("es");
        when(messageService.resolveRaw(any(), any())).thenReturn("Mocked description");

        commandManager = new CommandManager(plugin, cinematicService, messageService);
        commandManager.registerDefaults();

        sender = Mockito.mock(CommandSender.class);
        command = Mockito.mock(Command.class);
    }

    @Test
    void sendsLocalizedUnknownSubcommandMessage() {
        when(sender.hasPermission(any(String.class))).thenReturn(true);

        boolean handled = commandManager.onCommand(sender, command, "cine", new String[]{"nonexistent"});
        assertTrue(handled);

        verify(messageService).send(eq(sender), eq("command.error.unknown_subcommand"), eq(Map.of("subcommand", "nonexistent")));
        verify(messageService).send(eq(sender), eq("command.help.hint"), eq(Map.of("label", "cine")));
    }

    @Test
    void sendsLocalizedPermissionErrorWhenDenied() {
        when(sender.hasPermission("cinematic.play")).thenReturn(false);

        boolean handled = commandManager.onCommand(sender, command, "cine", new String[]{"play", "demo"});
        assertTrue(handled);

        verify(messageService).send(eq(sender), eq("command.error.no_permission"), eq(Map.of("permission", "cinematic.play")));
    }

    @Test
    void sendsLocalizedResultWhenSubcommandReturnsKey() {
        when(sender.hasPermission("cinematic.play")).thenReturn(true);
        when(cinematicService.play("demo")).thenReturn(
                CinematicActionResult.successKey("cinematic.play.started", Map.of("name", "demo"), "Started")
        );

        boolean handled = commandManager.onCommand(sender, command, "cine", new String[]{"play", "demo"});
        assertTrue(handled);

        verify(messageService).send(eq(sender), eq("cinematic.play.started"), eq(Map.of("name", "demo")));
    }

    @Test
    void sendsGeneralHelpUsingMessageService() {
        when(sender.hasPermission(any(String.class))).thenReturn(true);

        commandManager.sendGeneralHelp(sender);

        verify(messageService).send(eq(sender), eq("command.help.header"));
    }

    @Test
    void sendsSubcommandHelpUsingMessageService() {
        when(sender.hasPermission(any(String.class))).thenReturn(true);

        commandManager.sendSubcommandHelp(sender, "play");

        verify(messageService).send(eq(sender), eq("command.help.detail.usage"), any());
        verify(messageService).send(eq(sender), eq("command.help.detail.description"), any());
        verify(messageService).send(eq(sender), eq("command.help.detail.permission"), any());
    }

    @Test
    void reloadsMessageServiceWhenReloadExecuted() {
        when(sender.hasPermission("cinematic.reload")).thenReturn(true);
        when(cinematicService.reload()).thenReturn(
                CinematicActionResult.successKey("cinematic.reload.success", Map.of("count", 2), "Reloaded")
        );

        boolean handled = commandManager.onCommand(sender, command, "cine", new String[]{"reload"});
        assertTrue(handled);

        verify(messageService).reload();
        verify(messageService).send(eq(sender), eq("cinematic.reload.success"), eq(Map.of("count", 2)));
    }
}
