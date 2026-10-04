package com.darkbladedev.cinematic.dsl.registry;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.mapper.SceneMapper;
import com.darkbladedev.cinematic.dsl.parser.SceneParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class SceneLoader {
    private final Path cinematicsDirectory;
    private final SceneParser parser;
    private final SceneMapper mapper;
    private final Map<String, Scene> sceneCache;
    private final Map<String, SceneDTO> dtoCache;
    private final Map<String, Path> sceneFiles;

    public SceneLoader(Path cinematicsDirectory, SceneParser parser, SceneMapper mapper) {
        this.cinematicsDirectory = Objects.requireNonNull(cinematicsDirectory, "cinematicsDirectory");
        this.parser = Objects.requireNonNull(parser, "parser");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.sceneCache = new ConcurrentHashMap<>();
        this.dtoCache = new ConcurrentHashMap<>();
        this.sceneFiles = new ConcurrentHashMap<>();
    }

    public synchronized int reloadAll() {
        ensureDirectory();
        Map<String, Scene> loadedScenes = new HashMap<>();
        Map<String, SceneDTO> loadedDtos = new HashMap<>();
        Map<String, Path> filesById = new HashMap<>();
        for (Path file : discoverYamlFiles()) {
            SceneDTO dto = parser.parse(file);
            Scene scene = mapper.map(dto);
            String normalizedId = normalize(scene.id());
            loadedScenes.put(normalizedId, scene);
            loadedDtos.put(normalizedId, dto);
            filesById.put(normalizedId, file);
        }
        sceneCache.clear();
        sceneCache.putAll(loadedScenes);
        dtoCache.clear();
        dtoCache.putAll(loadedDtos);
        sceneFiles.clear();
        sceneFiles.putAll(filesById);
        return loadedScenes.size();
    }

    public synchronized Optional<Scene> load(String id) {
        String normalizedId = normalize(id);
        Scene cached = sceneCache.get(normalizedId);
        if (cached != null) {
            return Optional.of(cached);
        }
        Path file = resolveSceneFile(id);
        if (file == null) {
            return Optional.empty();
        }
        SceneDTO dto = parser.parse(file);
        Scene scene = mapper.map(dto);
        sceneCache.put(normalizedId, scene);
        dtoCache.put(normalizedId, dto);
        sceneFiles.put(normalizedId, file);
        return Optional.of(scene);
    }

    public synchronized Optional<SceneDTO> loadDto(String id) {
        String normalizedId = normalize(id);
        SceneDTO cached = dtoCache.get(normalizedId);
        if (cached != null) {
            return Optional.of(cached);
        }
        Path file = resolveSceneFile(id);
        if (file == null) {
            return Optional.empty();
        }
        SceneDTO dto = parser.parse(file);
        Scene scene = mapper.map(dto);
        sceneCache.put(normalizedId, scene);
        dtoCache.put(normalizedId, dto);
        sceneFiles.put(normalizedId, file);
        return Optional.of(dto);
    }

    public synchronized Set<String> availableSceneIds() {
        return Set.copyOf(sceneCache.keySet());
    }

    private Path resolveSceneFile(String id) {
        Path known = sceneFiles.get(normalize(id));
        if (known != null && Files.exists(known)) {
            return known;
        }
        Path yml = cinematicsDirectory.resolve(id + ".yml");
        if (Files.exists(yml)) {
            return yml;
        }
        Path yaml = cinematicsDirectory.resolve(id + ".yaml");
        if (Files.exists(yaml)) {
            return yaml;
        }
        return null;
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(cinematicsDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo crear el directorio de cinemáticas: " + cinematicsDirectory, exception);
        }
    }

    private Set<Path> discoverYamlFiles() {
        try (Stream<Path> stream = Files.list(cinematicsDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isYaml)
                    .collect(java.util.stream.Collectors.toSet());
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudieron listar archivos en: " + cinematicsDirectory, exception);
        }
    }

    private boolean isYaml(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private String normalize(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
