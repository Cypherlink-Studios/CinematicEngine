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

    @Test
    void parsesSceneWithActorsDeclaration() throws IOException {
        Path file = Files.createTempFile("scene-actors-", ".yml");
        Files.writeString(file, """
                id: epic_intro
                duration: 100
                actors:
                  - id: hero
                    type: self_clone
                    initial_position: [100.5, 64.0, 50.5]
                    initial_yaw: 0.0
                    initial_pitch: 5.0
                  - id: villain
                    type: virtual
                    skin: DarkSorcerer
                    initial_position: [110.0, 64.0, 50.0]
                    initial_equipment:
                      main_hand: NETHERITE_SWORD
                      helmet: NETHERITE_HELMET
                tracks: []
                """);
        SceneParser parser = new SnakeYamlSceneParser();

        SceneDTO dto = parser.parse(file);

        assertThat(dto.id()).isEqualTo("epic_intro");
        assertThat(dto.duration()).isEqualTo(100);
        assertThat(dto.actors()).hasSize(2);

        var hero = dto.actors().get(0);
        assertThat(hero.id()).isEqualTo("hero");
        assertThat(hero.type()).isEqualTo("self_clone");
        assertThat(hero.initialPosition()).isEqualTo(new org.joml.Vector3d(100.5, 64.0, 50.5));
        assertThat(hero.initialYaw()).isEqualTo(0.0f);
        assertThat(hero.initialPitch()).isEqualTo(5.0f);

        var villain = dto.actors().get(1);
        assertThat(villain.id()).isEqualTo("villain");
        assertThat(villain.type()).isEqualTo("virtual");
        assertThat(villain.skin()).isEqualTo("DarkSorcerer");
        assertThat(villain.initialEquipment())
                .containsEntry("main_hand", "NETHERITE_SWORD")
                .containsEntry("helmet", "NETHERITE_HELMET");
    }
}
