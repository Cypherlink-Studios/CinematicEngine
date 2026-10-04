package com.darkbladedev.cinematic.dsl.parser;

import com.darkbladedev.cinematic.dsl.dto.ActorDTO;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import org.joml.Vector3d;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SnakeYamlSceneParser implements SceneParser {
    private static final String KEY_ID = "id";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_ACTORS = "actors";
    private static final String KEY_TRACKS = "tracks";
    private static final String KEY_TYPE = "type";
    private static final String KEY_KEYFRAMES = "keyframes";
    private static final String KEY_TICK = "tick";
    private static final String KEY_INTERPOLATION = "interpolation";
    private final Yaml yaml;

    public SnakeYamlSceneParser() {
        LoaderOptions options = new LoaderOptions();
        options.setCodePointLimit(2_000_000);
        this.yaml = new Yaml(new SafeConstructor(options));
    }

    @Override
    public SceneDTO parse(Path file) {
        Objects.requireNonNull(file, "file");
        try (InputStream inputStream = Files.newInputStream(file)) {
            Object root = yaml.load(inputStream);
            if (!(root instanceof Map<?, ?> rootMap)) {
                throw new SceneParseException("El archivo debe tener un objeto YAML en la raíz: " + file);
            }
            String id = asString(rootMap.get(KEY_ID), KEY_ID, file);
            int duration = asInt(rootMap.get(KEY_DURATION), KEY_DURATION, file);
            List<ActorDTO> actors = parseActors(rootMap.get(KEY_ACTORS), file);
            List<TrackDTO> tracks = parseTracks(rootMap.get(KEY_TRACKS), file);
            Map<String, Object> metadata = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rootMap.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    continue;
                }
                if (KEY_ID.equals(key) || KEY_DURATION.equals(key) || KEY_TRACKS.equals(key) || KEY_ACTORS.equals(key)) {
                    continue;
                }
                metadata.put(key, entry.getValue());
            }
            return new SceneDTO(id, duration, actors, tracks, metadata);
        } catch (IOException exception) {
            throw new SceneParseException("No se pudo leer el archivo YAML: " + file, exception);
        } catch (YAMLException exception) {
            throw new SceneParseException("Formato YAML inválido en archivo: " + file, exception);
        }
    }

    private List<ActorDTO> parseActors(Object rawActors, Path file) {
        if (rawActors == null) {
            return List.of();
        }
        if (!(rawActors instanceof List<?> rawList)) {
            throw new SceneParseException("La propiedad 'actors' debe ser una lista en: " + file);
        }
        List<ActorDTO> actors = new ArrayList<>(rawList.size());
        for (int index = 0; index < rawList.size(); index++) {
            Object rawActor = rawList.get(index);
            if (!(rawActor instanceof Map<?, ?> actorMap)) {
                throw new SceneParseException("Actor inválido en índice " + index + " en archivo: " + file);
            }
            String actorId = asString(actorMap.get(KEY_ID), "actors[" + index + "].id", file);
            String type = optionalString(actorMap.get("type"), "actors[" + index + "].type", file);
            if (type == null) {
                type = "virtual";
            }
            String skin = optionalString(actorMap.get("skin"), "actors[" + index + "].skin", file);

            Object rawPos = actorMap.get("initial_position") != null ? actorMap.get("initial_position") : actorMap.get("position");
            Vector3d initialPos = parseVector3d(rawPos, "actors[" + index + "].initial_position", file);

            Float initialYaw = parseOptionalFloat(actorMap.get("initial_yaw") != null ? actorMap.get("initial_yaw") : actorMap.get("yaw"));
            Float initialPitch = parseOptionalFloat(actorMap.get("initial_pitch") != null ? actorMap.get("initial_pitch") : actorMap.get("pitch"));

            Map<String, String> initialEquipment = new LinkedHashMap<>();
            Object rawEquip = actorMap.get("initial_equipment") != null ? actorMap.get("initial_equipment") : actorMap.get("equipment");
            if (rawEquip instanceof Map<?, ?> equipMap) {
                for (Map.Entry<?, ?> entry : equipMap.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        initialEquipment.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : actorMap.entrySet()) {
                if (entry.getKey() instanceof String k) {
                    if (!KEY_ID.equals(k) && !"type".equals(k) && !"skin".equals(k)
                            && !"initial_position".equals(k) && !"position".equals(k)
                            && !"initial_yaw".equals(k) && !"yaw".equals(k)
                            && !"initial_pitch".equals(k) && !"pitch".equals(k)
                            && !"initial_equipment".equals(k) && !"equipment".equals(k)) {
                        data.put(k, entry.getValue());
                    }
                }
            }
            actors.add(new ActorDTO(actorId, type, skin, initialPos, initialYaw, initialPitch, initialEquipment, data));
        }
        return actors;
    }

    private Vector3d parseVector3d(Object value, String field, Path file) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list && list.size() >= 3) {
            if (list.get(0) instanceof Number x && list.get(1) instanceof Number y && list.get(2) instanceof Number z) {
                return new Vector3d(x.doubleValue(), y.doubleValue(), z.doubleValue());
            }
        }
        throw new SceneParseException("Vector3d inválido en '" + field + "' en archivo: " + file);
    }

    private Float parseOptionalFloat(Object value) {
        if (value instanceof Number n) {
            return n.floatValue();
        }
        return null;
    }

    private List<TrackDTO> parseTracks(Object rawTracks, Path file) {
        if (!(rawTracks instanceof List<?> rawList)) {
            throw new SceneParseException("La propiedad 'tracks' debe ser una lista en: " + file);
        }
        List<TrackDTO> tracks = new ArrayList<>(rawList.size());
        for (int index = 0; index < rawList.size(); index++) {
            Object rawTrack = rawList.get(index);
            if (!(rawTrack instanceof Map<?, ?> trackMap)) {
                throw new SceneParseException("Track inválido en índice " + index + " en archivo: " + file);
            }
            String trackId = optionalString(trackMap.get(KEY_ID), "tracks[" + index + "].id", file);
            String type = asString(trackMap.get(KEY_TYPE), "tracks[" + index + "].type", file);
            List<KeyframeDTO> keyframes = parseKeyframes(trackMap.get(KEY_KEYFRAMES), index, file);
            Map<String, Object> data = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : trackMap.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    continue;
                }
                if (KEY_ID.equals(key) || KEY_TYPE.equals(key) || KEY_KEYFRAMES.equals(key)) {
                    continue;
                }
                if ("data".equalsIgnoreCase(key) && entry.getValue() instanceof Map<?, ?> nestedMap) {
                    for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                        if (nestedEntry.getKey() instanceof String nestedKey) {
                            data.put(nestedKey, nestedEntry.getValue());
                        }
                    }
                    data.put(key, entry.getValue());
                } else {
                    data.put(key, entry.getValue());
                }
            }
            tracks.add(new TrackDTO(trackId, type, data, keyframes));
        }
        return tracks;
    }

    private List<KeyframeDTO> parseKeyframes(Object rawKeyframes, int trackIndex, Path file) {
        if (!(rawKeyframes instanceof List<?> rawList)) {
            throw new SceneParseException("La propiedad 'keyframes' debe ser una lista en track " + trackIndex + " de: " + file);
        }
        List<KeyframeDTO> keyframes = new ArrayList<>(rawList.size());
        for (int index = 0; index < rawList.size(); index++) {
            Object rawFrame = rawList.get(index);
            if (!(rawFrame instanceof Map<?, ?> frameMap)) {
                throw new SceneParseException("Keyframe inválido en tracks[" + trackIndex + "].keyframes[" + index + "] de: " + file);
            }
            long tick = asLong(frameMap.get(KEY_TICK), "tracks[" + trackIndex + "].keyframes[" + index + "].tick", file);
            String interpolation = optionalString(
                    frameMap.get(KEY_INTERPOLATION),
                    "tracks[" + trackIndex + "].keyframes[" + index + "].interpolation",
                    file
            );
            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : frameMap.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    continue;
                }
                if (KEY_TICK.equals(key) || KEY_INTERPOLATION.equals(key)) {
                    continue;
                }
                values.put(key, entry.getValue());
            }
            keyframes.add(new KeyframeDTO(tick, interpolation, values));
        }
        return keyframes;
    }

    private String asString(Object value, String field, Path file) {
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new SceneParseException("Campo requerido inválido '" + field + "' en archivo: " + file);
    }

    private String optionalString(Object value, String field, Path file) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new SceneParseException("Campo de texto inválido '" + field + "' en archivo: " + file);
    }

    private int asInt(Object value, String field, Path file) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new SceneParseException("Campo numérico inválido '" + field + "' en archivo: " + file);
    }

    private long asLong(Object value, String field, Path file) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new SceneParseException("Campo numérico inválido '" + field + "' en archivo: " + file);
    }
}
