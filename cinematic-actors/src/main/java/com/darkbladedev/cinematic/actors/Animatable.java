package com.darkbladedev.cinematic.actors;

public interface Animatable {
    void setPose(ActorPose pose);

    ActorPose pose();

    void triggerAction(ActorAction action);

    void setItemUsage(ItemUsageState state);

    ItemUsageState itemUsage();
}
