package com.darkbladedev.cinematic.dsl.runtime;

import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.core.model.Keyframe;

import java.util.Map;

public record ActorActionFrame(
        long tick,
        ActorPose pose,
        ActorAction action,
        ItemUsageState itemUsage,
        Map<EquipmentSlot, String> equipment,
        Float headYaw,
        Float headPitch
) implements Keyframe {
    public ActorActionFrame {
        equipment = equipment == null ? Map.of() : Map.copyOf(equipment);
    }
}
