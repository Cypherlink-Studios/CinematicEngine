package com.darkbladedev.cinematic.dsl.validator.track;

import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.dsl.dto.KeyframeDTO;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.validator.ValidationCollector;

import java.util.Locale;
import java.util.Map;

public final class ActorActionTrackValidator implements TrackValidator {
    @Override
    public void validate(TrackDTO dto, int sceneDuration, ValidationCollector collector) {
        if (!dto.data().containsKey("actor_id")) {
            collector.add("tracks[type=actor_action].actor_id es obligatorio.");
        }
        if (dto.keyframes().isEmpty()) {
            collector.add("tracks[type=actor_action].keyframes no puede estar vacío.");
            return;
        }

        for (int i = 0; i < dto.keyframes().size(); i++) {
            KeyframeDTO frame = dto.keyframes().get(i);
            String basePath = "tracks[type=actor_action].keyframes[" + i + "]";

            if (frame.values().containsKey("pose")) {
                try {
                    ActorPose.valueOf(frame.values().get("pose").toString().trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    collector.add(basePath + ".pose contiene un valor inválido: " + frame.values().get("pose"));
                }
            }

            if (frame.values().containsKey("action")) {
                try {
                    ActorAction.valueOf(frame.values().get("action").toString().trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    collector.add(basePath + ".action contiene un valor inválido: " + frame.values().get("action"));
                }
            }

            if (frame.values().containsKey("item_usage")) {
                try {
                    ItemUsageState.valueOf(frame.values().get("item_usage").toString().trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    collector.add(basePath + ".item_usage contiene un valor inválido: " + frame.values().get("item_usage"));
                }
            }

            if (frame.values().containsKey("equipment")) {
                Object eq = frame.values().get("equipment");
                if (eq instanceof Map<?, ?> map) {
                    for (Object key : map.keySet()) {
                        try {
                            EquipmentSlot.valueOf(key.toString().trim().toUpperCase(Locale.ROOT));
                        } catch (IllegalArgumentException e) {
                            collector.add(basePath + ".equipment contiene un slot inválido: " + key);
                        }
                    }
                } else {
                    collector.add(basePath + ".equipment debe ser un mapa.");
                }
            }
        }
    }
}
