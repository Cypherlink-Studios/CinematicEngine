package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.Actor;

import java.util.Optional;

public interface ActorResolver {
    Optional<Actor> resolve(String actorId);
}
