package com.darkbladedev.cinematic.dsl.parser;

import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SnakeYamlSceneParserTest {
    @Test
    void parsesSceneYamlIntoDto() throws IOException {
        Path file = Files.createTempFile("scene-", ".yml");
        Files.writeString(file, """
                id: intro
                duration: 200
                tracks:
                  - type: camera
                    keyframes:
                      - tick: 0
                        position: [0, 70, 0]
                        yaw: 0
                        pitch: 0
                      - tick: 100
                        position: [10, 75, 10]
                        yaw: 90
                        pitch: -10
                        interpolation: ease_in_out
                """);
        SceneParser parser = new SnakeYamlSceneParser();

        SceneDTO dto = parser.parse(file);

        assertThat(dto.id()).isEqualTo("intro");
        assertThat(dto.duration()).isEqualTo(200);
        assertThat(dto.tracks()).hasSize(1);
        assertThat(dto.tracks().getFirst().type()).isEqualTo("camera");
        assertThat(dto.tracks().getFirst().keyframes()).hasSize(2);
    }
}
