package com.darkbladedev.cinematic.adapters.actor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinCacheService {
    public record SkinData(String value, String signature) {
        public SkinData {
            Objects.requireNonNull(value, "value");
        }
    }

    private final Path cacheDirectory;
    private final Map<String, SkinData> memoryCache = new ConcurrentHashMap<>();

    public SkinCacheService() {
        this(null);
    }

    public SkinCacheService(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        if (this.cacheDirectory != null) {
            try {
                Files.createDirectories(this.cacheDirectory);
            } catch (IOException ignored) {
            }
        }
    }

    public Optional<SkinData> getCached(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }
        String key = playerName.toLowerCase(Locale.ROOT);
        SkinData inMemory = memoryCache.get(key);
        if (inMemory != null) {
            return Optional.of(inMemory);
        }

        if (cacheDirectory != null) {
            Path file = cacheDirectory.resolve(key + ".skin");
            if (Files.exists(file)) {
                try {
                    List<String> lines = Files.readAllLines(file);
                    if (!lines.isEmpty()) {
                        String value = lines.get(0).trim();
                        String signature = lines.size() > 1 ? lines.get(1).trim() : null;
                        SkinData data = new SkinData(value, signature);
                        memoryCache.put(key, data);
                        return Optional.of(data);
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return Optional.empty();
    }

    public void cache(String playerName, SkinData data) {
        if (playerName == null || playerName.isBlank() || data == null) {
            return;
        }
        String key = playerName.toLowerCase(Locale.ROOT);
        memoryCache.put(key, data);

        if (cacheDirectory != null) {
            Path file = cacheDirectory.resolve(key + ".skin");
            try {
                String content = data.value() + "\n" + (data.signature() != null ? data.signature() : "") + "\n";
                Files.writeString(file, content);
            } catch (IOException ignored) {
            }
        }
    }

    public Optional<SkinData> resolve(String playerName) {
        Optional<SkinData> cached = getCached(playerName);
        if (cached.isPresent()) {
            return cached;
        }
        // In real online servers with Mojang session access, an HTTP lookup would run here.
        // For offline, cached, or fallback environments, return empty if not found in cache.
        return Optional.empty();
    }
}
