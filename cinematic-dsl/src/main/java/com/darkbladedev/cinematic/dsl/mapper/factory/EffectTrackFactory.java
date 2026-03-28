package com.darkbladedev.cinematic.dsl.mapper.factory;

import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.TypeConversion;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.runtime.EffectFrame;
import com.darkbladedev.cinematic.dsl.runtime.EffectTrack;
import com.darkbladedev.cinematic.dsl.runtime.ParticleEffect;
import com.darkbladedev.cinematic.dsl.runtime.SoundEffect;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EffectTrackFactory implements TrackFactory {
    @Override
    public EffectTrack create(TrackDTO dto) {
        String trackId = dto.id() == null || dto.id().isBlank() ? "effect_track" : dto.id();
        List<EffectFrame> frames = new ArrayList<>(dto.keyframes().size());
        for (int index = 0; index < dto.keyframes().size(); index++) {
            KeyframeDTO frame = dto.keyframes().get(index);
            String basePath = "tracks[type=effect].keyframes[" + index + "]";
            SoundEffect soundEffect = toSoundEffect(frame.values().get("play_sound"), basePath + ".play_sound");
            ParticleEffect particleEffect = toParticleEffect(frame.values().get("particle"), basePath + ".particle");
            frames.add(new EffectFrame(frame.tick(), soundEffect, particleEffect));
        }
        return new EffectTrack(trackId, frames);
    }

    private SoundEffect toSoundEffect(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof String soundName) {
            return new SoundEffect(soundName, 1.0F, 1.0F);
        }
        Map<String, Object> map = TypeConversion.toMap(value, field);
        String name = TypeConversion.toStringValue(map.get("name"), field + ".name");
        float volume = map.containsKey("volume") ? TypeConversion.toFloat(map.get("volume"), field + ".volume") : 1.0F;
        float pitch = map.containsKey("pitch") ? TypeConversion.toFloat(map.get("pitch"), field + ".pitch") : 1.0F;
        return new SoundEffect(name, volume, pitch);
    }

    private ParticleEffect toParticleEffect(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof String particleType) {
            return new ParticleEffect(particleType, 1, new Vector3d(), 0.0F);
        }
        Map<String, Object> map = TypeConversion.toMap(value, field);
        String type = TypeConversion.toStringValue(map.get("type"), field + ".type");
        int count = map.containsKey("count") ? (int) TypeConversion.toDouble(map.get("count"), field + ".count") : 1;
        Vector3d offset = map.containsKey("offset")
                ? TypeConversion.toVector3(map.get("offset"), field + ".offset")
                : new Vector3d();
        float speed = map.containsKey("speed") ? TypeConversion.toFloat(map.get("speed"), field + ".speed") : 0.0F;
        return new ParticleEffect(type, count, offset, speed);
    }
}
