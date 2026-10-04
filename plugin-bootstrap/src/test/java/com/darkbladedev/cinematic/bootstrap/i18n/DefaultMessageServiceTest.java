package com.darkbladedev.cinematic.bootstrap.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultMessageServiceTest {

    @TempDir
    Path tempDir;

    private DefaultMessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new DefaultMessageService(
                tempDir,
                "es",
                true,
                Logger.getLogger(DefaultMessageServiceTest.class.getName())
        );
    }

    @Test
    void extractsDefaultBundlesOnStartup() {
        assertTrue(Files.exists(tempDir.resolve("messages_es.yml")));
        assertTrue(Files.exists(tempDir.resolve("messages_en.yml")));
    }

    @Test
    void resolvesKeysInSpanishAndEnglish() {
        String esMsg = messageService.resolveRaw("es", "command.subcommand.stop.description");
        String enMsg = messageService.resolveRaw("en", "command.subcommand.stop.description");

        assertEquals("Detiene la cinemática actual.", esMsg);
        assertEquals("Stops the active cinematic.", enMsg);
    }

    @Test
    void fallsBackToDefaultLanguageWhenKeyMissingInRequestedLocale() throws IOException {
        Path customDir = tempDir.resolve("custom_test");
        Files.createDirectories(customDir);
        // Create an incomplete French bundle
        Files.writeString(customDir.resolve("messages_fr.yml"), "prefix: '<gold>[FR]</gold> '\n");

        DefaultMessageService frService = new DefaultMessageService(
                customDir,
                "es",
                true,
                Logger.getLogger(DefaultMessageServiceTest.class.getName())
        );

        // Key not in FR, must fallback to ES
        String msg = frService.resolveRaw("fr", "command.subcommand.stop.description");
        assertEquals("Detiene la cinemática actual.", msg);
    }

    @Test
    void returnsMissingPlaceholderWhenKeyNotFoundAnywhere() {
        String missing = messageService.resolveRaw("es", "non.existent.key");
        assertTrue(missing.contains("Missing translation: non.existent.key"));
    }

    @Test
    void rendersComponentWithMiniMessageAndPlaceholders() {
        Component component = messageService.render(
                "es",
                "cinematic.play.not_found",
                Map.of("name", "intro_scene")
        );

        String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        assertEquals("La cinemática 'intro_scene' no existe.", plainText);
    }

    @Test
    void rendersEnglishComponentWhenPlayerHasEnglishLocale() {
        Player player = Mockito.mock(Player.class);
        when(player.locale()).thenReturn(Locale.US);

        Component component = messageService.renderFor(
                player,
                "cinematic.play.not_found",
                Map.of("name", "intro_scene")
        );

        String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        assertEquals("Cinematic 'intro_scene' does not exist.", plainText);
    }

    @Test
    void rendersServerDefaultWhenReceiverIsConsole() {
        ConsoleCommandSender console = Mockito.mock(ConsoleCommandSender.class);

        Component component = messageService.renderFor(
                console,
                "cinematic.play.already_running"
        );

        String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        assertEquals("Ya hay una cinemática en ejecución.", plainText);
    }

    @Test
    void respectsPerPlayerLocaleDisabled() {
        DefaultMessageService fixedService = new DefaultMessageService(
                tempDir,
                "es",
                false,
                Logger.getLogger(DefaultMessageServiceTest.class.getName())
        );

        Player player = Mockito.mock(Player.class);
        when(player.locale()).thenReturn(Locale.US);

        assertEquals("es", fixedService.resolveLanguage(player));
    }

    @Test
    void sendsRenderedComponentToReceiver() {
        CommandSender sender = Mockito.mock(CommandSender.class);
        messageService.send(sender, "cinematic.stop.stopped");

        verify(sender).sendMessage(Mockito.any(Component.class));
    }

    @Test
    void reloadsBundlesFromDisk() throws IOException {
        Path esFile = tempDir.resolve("messages_es.yml");
        String original = Files.readString(esFile);
        String modified = original.replace("Detiene la cinemática actual.", "Detiene la película activa.");
        Files.writeString(esFile, modified);

        messageService.reload();

        String reloaded = messageService.resolveRaw("es", "command.subcommand.stop.description");
        assertEquals("Detiene la película activa.", reloaded);
    }
}
