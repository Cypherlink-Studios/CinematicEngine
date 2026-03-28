package com.darkbladedev.cinematic.dsl.registry;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.dsl.mapper.DefaultDslComponents;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.SceneMapper;
import com.darkbladedev.cinematic.dsl.parser.SceneParser;
import com.darkbladedev.cinematic.dsl.parser.SnakeYamlSceneParser;
import com.darkbladedev.cinematic.dsl.validator.SceneDtoValidator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SceneLoaderTest {
    @Test
    void reloadAndLoadFromDirectory() throws IOException {
        Path directory = Files.createTempDirectory("cinematics");
        Path file = directory.resolve("intro.yml");
        Files.writeString(file, """
                id: intro
                duration: 120
                tracks:
                  - type: camera
                    keyframes:
                      - tick: 0
                        position: [0, 70, 0]
                        yaw: 0
                        pitch: 0
                      - tick: 120
                        position: [12, 72, 4]
                        yaw: 90
                        pitch: -8
                """);
        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator validator = DefaultDslComponents.validator(trackRegistry, interpolatorRegistry);
        SceneMapper mapper = DefaultDslComponents.mapper(validator, trackRegistry);
        SceneParser parser = new SnakeYamlSceneParser();
        SceneLoader loader = new SceneLoader(directory, parser, mapper);

        int loaded = loader.reloadAll();
        Scene scene = loader.load("intro").orElseThrow();

        assertThat(loaded).isEqualTo(1);
        assertThat(loader.availableSceneIds()).containsExactly("intro");
        assertThat(scene.id()).isEqualTo("intro");
    }
}
