package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.core.model.Keyframe;

public record EffectFrame(long tick, SoundEffect sound, ParticleEffect particle) implements Keyframe {
}
