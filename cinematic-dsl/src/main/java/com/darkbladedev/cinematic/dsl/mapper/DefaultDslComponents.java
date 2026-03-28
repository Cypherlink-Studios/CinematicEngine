package com.darkbladedev.cinematic.dsl.mapper;

import com.darkbladedev.cinematic.dsl.mapper.factory.ActorTrackFactory;
import com.darkbladedev.cinematic.dsl.mapper.factory.CameraTrackFactory;
import com.darkbladedev.cinematic.dsl.mapper.factory.EffectTrackFactory;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.validator.SceneDtoValidator;
import com.darkbladedev.cinematic.dsl.validator.track.ActorTrackValidator;
import com.darkbladedev.cinematic.dsl.validator.track.CameraTrackValidator;
import com.darkbladedev.cinematic.dsl.validator.track.EffectTrackValidator;

public final class DefaultDslComponents {
    private DefaultDslComponents() {
    }

    public static InterpolatorRegistry interpolatorRegistry() {
        return InterpolatorRegistry.defaultRegistry();
    }

    public static TrackRegistry trackRegistry(InterpolatorRegistry interpolatorRegistry) {
        TrackRegistry registry = new TrackRegistry();
        registry.register("camera", new CameraTrackFactory(interpolatorRegistry), new CameraTrackValidator());
        registry.register("actor", new ActorTrackFactory(interpolatorRegistry), new ActorTrackValidator());
        registry.register("effect", new EffectTrackFactory(), new EffectTrackValidator());
        return registry;
    }

    public static SceneDtoValidator validator(TrackRegistry trackRegistry, InterpolatorRegistry interpolatorRegistry) {
        return new SceneDtoValidator(trackRegistry, interpolatorRegistry);
    }

    public static SceneMapper mapper(SceneDtoValidator validator, TrackRegistry trackRegistry) {
        return new SceneMapper(validator, trackRegistry);
    }
}
