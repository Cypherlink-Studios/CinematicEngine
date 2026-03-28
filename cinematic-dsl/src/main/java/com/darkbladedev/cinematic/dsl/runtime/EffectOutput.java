package com.darkbladedev.cinematic.dsl.runtime;

public interface EffectOutput {
    void playSound(SoundEffect effect);

    void spawnParticle(ParticleEffect effect);
}
