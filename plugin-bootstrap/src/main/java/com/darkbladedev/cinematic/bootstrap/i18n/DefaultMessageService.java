package com.darkbladedev.cinematic.bootstrap.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class DefaultMessageService implements MessageService {

    private static final List<String> DEFAULT_BUNDLES = List.of("es", "en");

    private final Path localesDirectory;
    private final String defaultLanguage;
    private final boolean perPlayerLocale;
    private final ClassLoader classLoader;
    private final Logger logger;
    private final MiniMessage miniMessage;

    private final Map<String, Map<String, String>> bundleCache = new ConcurrentHashMap<>();
    private final Map<String, String> embeddedBaseBundle = new ConcurrentHashMap<>();

    public DefaultMessageService(Path localesDirectory, String defaultLanguage, boolean perPlayerLocale, Logger logger) {
        this(localesDirectory, defaultLanguage, perPlayerLocale, DefaultMessageService.class.getClassLoader(), logger);
    }

    public DefaultMessageService(Path localesDirectory, String defaultLanguage, boolean perPlayerLocale, ClassLoader classLoader, Logger logger) {
        this.localesDirectory = Objects.requireNonNull(localesDirectory, "localesDirectory");
        this.defaultLanguage = (defaultLanguage == null || defaultLanguage.isBlank()) ? "es" : defaultLanguage.toLowerCase(Locale.ROOT);
        this.perPlayerLocale = perPlayerLocale;
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.logger = logger != null ? logger : Logger.getLogger(DefaultMessageService.class.getName());
        this.miniMessage = MiniMessage.miniMessage();

        init();
    }

    private void init() {
        loadEmbeddedBaseBundle();
        extractDefaultBundles();
        reload();
    }

    @Override
    public void send(CommandSender receiver, String key, Map<String, Object> placeholders) {
        Objects.requireNonNull(receiver, "receiver");
        Component component = renderFor(receiver, key, placeholders);
        receiver.sendMessage(component);
    }

    @Override
    public Component render(String language, String key, Map<String, Object> placeholders) {
        String template = resolveRaw(language, key);
        String prefix = resolveRaw(language, "prefix");

        List<TagResolver> resolvers = new ArrayList<>();
        if (prefix != null && !prefix.startsWith("<red>[Missing translation")) {
            resolvers.add(Placeholder.parsed("prefix", prefix));
        } else {
            resolvers.add(Placeholder.parsed("prefix", ""));
        }

        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Component comp) {
                    resolvers.add(Placeholder.component(name, comp));
                } else {
                    resolvers.add(Placeholder.unparsed(name, value != null ? value.toString() : ""));
                }
            }
        }

        return miniMessage.deserialize(template, TagResolver.resolver(resolvers));
    }

    @Override
    public Component renderFor(CommandSender receiver, String key, Map<String, Object> placeholders) {
        String lang = resolveLanguage(receiver);
        return render(lang, key, placeholders);
    }

    @Override
    public String resolveRaw(String language, String key) {
        Objects.requireNonNull(key, "key");
        String lang = (language == null || language.isBlank()) ? defaultLanguage : language.toLowerCase(Locale.ROOT);

        Map<String, String> targetBundle = bundleCache.get(lang);
        if (targetBundle != null && targetBundle.containsKey(key)) {
            return targetBundle.get(key);
        }

        if (!lang.equals(defaultLanguage)) {
            Map<String, String> fallbackBundle = bundleCache.get(defaultLanguage);
            if (fallbackBundle != null && fallbackBundle.containsKey(key)) {
                return fallbackBundle.get(key);
            }
        }

        if (embeddedBaseBundle.containsKey(key)) {
            return embeddedBaseBundle.get(key);
        }

        return "<red>[Missing translation: " + key + "]</red>";
    }

    @Override
    public String resolveLanguage(CommandSender sender) {
        if (sender == null) {
            return defaultLanguage;
        }
        if (perPlayerLocale && sender instanceof Player player) {
            Locale playerLocale = player.locale();
            if (playerLocale != null && playerLocale.getLanguage() != null && !playerLocale.getLanguage().isBlank()) {
                String candidate = playerLocale.getLanguage().toLowerCase(Locale.ROOT);
                if (bundleCache.containsKey(candidate) || classLoader.getResource("locales/messages_" + candidate + ".yml") != null) {
                    return candidate;
                }
            }
        }
        return defaultLanguage;
    }

    @Override
    public synchronized void reload() {
        bundleCache.clear();
        if (Files.exists(localesDirectory)) {
            try (Stream<Path> stream = Files.list(localesDirectory)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().startsWith("messages_") && p.getFileName().toString().endsWith(".yml"))
                        .forEach(this::loadFileBundle);
            } catch (IOException e) {
                logger.log(Level.WARNING, "No se pudieron listar los archivos de idioma en: " + localesDirectory, e);
            }
        }

        // Si defaultLanguage o bundles requeridos no están en cache, cargar de embedded
        for (String lang : DEFAULT_BUNDLES) {
            if (!bundleCache.containsKey(lang)) {
                loadEmbeddedBundle(lang);
            }
        }
    }

    private void loadFileBundle(Path file) {
        String fileName = file.getFileName().toString();
        String lang = fileName.substring("messages_".length(), fileName.length() - ".yml".length()).toLowerCase(Locale.ROOT);
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            YamlConfiguration config = new YamlConfiguration();
            config.load(reader);
            Map<String, String> messages = flattenConfig(config);
            bundleCache.put(lang, messages);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al cargar archivo de idioma: " + file, e);
        }
    }

    private void loadEmbeddedBundle(String lang) {
        String resourcePath = "locales/messages_" + lang + ".yml";
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            if (in != null) {
                try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(reader);
                    bundleCache.put(lang, flattenConfig(config));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al cargar bundle embebido: " + resourcePath, e);
        }
    }

    private void loadEmbeddedBaseBundle() {
        embeddedBaseBundle.clear();
        String resourcePath = "locales/messages_es.yml";
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            if (in != null) {
                try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(reader);
                    embeddedBaseBundle.putAll(flattenConfig(config));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "No se pudo precargar el bundle base embebido: " + resourcePath, e);
        }
    }

    private void extractDefaultBundles() {
        try {
            if (Files.notExists(localesDirectory)) {
                Files.createDirectories(localesDirectory);
            }
            for (String lang : DEFAULT_BUNDLES) {
                Path dest = localesDirectory.resolve("messages_" + lang + ".yml");
                if (Files.notExists(dest)) {
                    String resourcePath = "locales/messages_" + lang + ".yml";
                    try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
                        if (in != null) {
                            Files.copy(in, dest);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "No se pudieron extraer los archivos de idioma por defecto a: " + localesDirectory, e);
        }
    }

    private Map<String, String> flattenConfig(YamlConfiguration config) {
        Map<String, String> map = new HashMap<>();
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                map.put(key, config.getString(key));
            }
        }
        return map;
    }
}
