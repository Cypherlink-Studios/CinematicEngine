package com.darkbladedev.cinematic.dsl.parser;

import com.darkbladedev.cinematic.dsl.dto.SceneDTO;

import java.nio.file.Path;

public interface SceneParser {
    SceneDTO parse(Path file);
}
