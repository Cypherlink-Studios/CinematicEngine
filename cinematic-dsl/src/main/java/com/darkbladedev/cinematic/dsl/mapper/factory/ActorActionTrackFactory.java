package com.darkbladedev.cinematic.dsl.mapper.factory;

import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.mapper.TypeConversion;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.runtime.ActorActionFrame;
import com.darkbladedev.cinematic.dsl.runtime.ActorActionTrack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ActorActionTrackFactory implements TrackFactory {
    @Override
    public ActorActionTrack create(TrackDTO dto) {
        String trackId = dto.id() == null || dto.id().isBlank() ? "actor_action_track" : dto.id();
        String actorId = TypeConversion.toStringValue(dto.data().get("actor_id"), "tracks[type=actor_action].actor_id");

        List<ActorActionFrame> frames = new ArrayList<>(dto.keyframes().size());
        for (int i = 0; i < dto.keyframes().size(); i++) {
            KeyframeDTO frame = dto.keyframes().get(i);
            ActorPose pose = null;
            if (frame.values().containsKey("pose")) {
                String str = frame.values().get("pose").toString().trim().toUpperCase(Locale.ROOT);
                pose = ActorPose.valueOf(str);
            }

            ActorAction action = null;
            if (frame.values().containsKey("action")) {
                String str = frame.values().get("action").toString().trim().toUpperCase(Locale.ROOT);
                action = ActorAction.valueOf(str);
            }

            ItemUsageState itemUsage = null;
            if (frame.values().containsKey("item_usage")) {
                String str = frame.values().get("item_usage").toString().trim().toUpperCase(Locale.ROOT);
                itemUsage = ItemUsageState.valueOf(str);
            }

            Map<EquipmentSlot, String> equipment = new EnumMap<>(EquipmentSlot.class);
            if (frame.values().get("equipment") instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        EquipmentSlot slot = EquipmentSlot.valueOf(entry.getKey().toString().trim().toUpperCase(Locale.ROOT));
                        equipment.put(slot, entry.getValue().toString());
                    }
                }
            }

            Float headYaw = null;
            if (frame.values().get("head_yaw") instanceof Number n) {
                headYaw = n.floatValue();
            }
            Float headPitch = null;
            if (frame.values().get("head_pitch") instanceof Number n) {
                headPitch = n.floatValue();
            }

            frames.add(new ActorActionFrame(frame.tick(), pose, action, itemUsage, equipment, headYaw, headPitch));
        }
        return new ActorActionTrack(trackId, actorId, frames);
    }
}
