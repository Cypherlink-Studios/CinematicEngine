package com.darkbladedev.cinematic.bootstrap.i18n;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * Service responsible for localization, bundle loading, and Adventure MiniMessage rendering.
 */
public interface MessageService {

    /**
     * Sends a localized message to the recipient with contextual placeholders.
     *
     * @param receiver     the command sender receiving the message
     * @param key          the translation message key
     * @param placeholders the contextual placeholders to substitute
     */
    void send(CommandSender receiver, String key, Map<String, Object> placeholders);

    /**
     * Sends a localized message to the recipient without extra placeholders.
     */
    default void send(CommandSender receiver, String key) {
        send(receiver, key, Map.of());
    }

    /**
     * Renders a message component for a specific language tag with placeholders.
     */
    Component render(String language, String key, Map<String, Object> placeholders);

    /**
     * Renders a message component for a specific language tag.
     */
    default Component render(String language, String key) {
        return render(language, key, Map.of());
    }

    /**
     * Renders a message component for a specific receiver using their resolved locale.
     */
    Component renderFor(CommandSender receiver, String key, Map<String, Object> placeholders);

    /**
     * Renders a message component for a specific receiver using their resolved locale.
     */
    default Component renderFor(CommandSender receiver, String key) {
        return renderFor(receiver, key, Map.of());
    }

    /**
     * Resolves the raw message template string for a specific language tag and key.
     */
    String resolveRaw(String language, String key);

    /**
     * Resolves the effective language tag for a command sender.
     */
    String resolveLanguage(CommandSender sender);

    /**
     * Reloads message bundles from disk.
     */
    void reload();
}
