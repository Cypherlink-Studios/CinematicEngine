package com.darkbladedev.cinematic.adapters.actor;

import com.darkbladedev.cinematic.actors.Actor;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.Movable;
import com.darkbladedev.cinematic.adapters.camera.ProtocolLibBridge;
import com.darkbladedev.cinematic.camera.targeting.ActorPositionLookup;
import com.darkbladedev.cinematic.dsl.dto.ActorDTO;
import com.darkbladedev.cinematic.dsl.dto.SceneDTO;
import com.darkbladedev.cinematic.dsl.runtime.ActorResolver;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class SceneActorSession implements ActorResolver, ActorPositionLookup {
    private final SceneDTO scene;
    private final Supplier<Iterable<Player>> viewersSupplier;
    private final SkinCacheService skinCache;
    private final ProtocolLibBridge protocolLibBridge;
    private final Map<String, Actor> actors = new ConcurrentHashMap<>();
    private volatile boolean active = true;

    public SceneActorSession(
            SceneDTO scene,
            Supplier<Iterable<Player>> viewersSupplier,
            SkinCacheService skinCache,
            ProtocolLibBridge protocolLibBridge
    ) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.viewersSupplier = Objects.requireNonNull(viewersSupplier, "viewersSupplier");
        this.skinCache = skinCache;
        this.protocolLibBridge = protocolLibBridge;
        initializeActors();
    }

    private void initializeActors() {
        if (scene.actors() == null || scene.actors().isEmpty()) {
            return;
        }

        for (ActorDTO dto : scene.actors()) {
            String type = dto.type() == null ? "virtual" : dto.type().toLowerCase(Locale.ROOT);
            Vector3d initPos = dto.initialPosition() != null ? dto.initialPosition() : new Vector3d(0, 64, 0);
            float yaw = dto.initialYaw() != null ? dto.initialYaw() : 0.0f;
            float pitch = dto.initialPitch() != null ? dto.initialPitch() : 0.0f;

            if ("self_clone".equals(type)) {
                Player primaryViewer = getFirstActiveViewer();
                String cloneName = primaryViewer != null ? primaryViewer.getName() : "Clone_" + dto.id();
                UUID cloneId = primaryViewer != null ? primaryViewer.getUniqueId() : UUID.randomUUID();

                VirtualPlayerActor cloneActor = new VirtualPlayerActor(
                        cloneId,
                        cloneName,
                        initPos,
                        yaw,
                        pitch,
                        viewersSupplier,
                        protocolLibBridge
                );

                if (primaryViewer != null && primaryViewer.getInventory() != null) {
                    try {
                        if (primaryViewer.getInventory().getItemInMainHand() != null) {
                            cloneActor.setEquipment(EquipmentSlot.MAIN_HAND, primaryViewer.getInventory().getItemInMainHand().getType().name());
                        }
                        if (primaryViewer.getInventory().getItemInOffHand() != null) {
                            cloneActor.setEquipment(EquipmentSlot.OFF_HAND, primaryViewer.getInventory().getItemInOffHand().getType().name());
                        }
                    } catch (Throwable ignored) {
                    }
                }

                // If explicit initial equipment was also configured, apply overrides
                applyInitialEquipment(cloneActor, dto.initialEquipment());

                cloneActor.spawn();
                actors.put(dto.id(), cloneActor);

            } else if ("virtual".equals(type)) {
                String actorName = dto.skin() != null && !dto.skin().isBlank() ? dto.skin() : dto.id();
                VirtualPlayerActor virtualActor = new VirtualPlayerActor(
                        UUID.randomUUID(),
                        actorName,
                        initPos,
                        yaw,
                        pitch,
                        viewersSupplier,
                        protocolLibBridge
                );

                applyInitialEquipment(virtualActor, dto.initialEquipment());
                virtualActor.spawn();
                actors.put(dto.id(), virtualActor);

            } else if ("persistent".equals(type)) {
                // Persistent actors wrap existing world entities or NPCs
                actors.put(dto.id(), new VirtualPlayerActor(
                        UUID.randomUUID(),
                        dto.id(),
                        initPos,
                        yaw,
                        pitch,
                        viewersSupplier,
                        protocolLibBridge
                ));
            }
        }
    }

    private void applyInitialEquipment(VirtualPlayerActor actor, Map<String, String> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : equipment.entrySet()) {
            try {
                EquipmentSlot slot = EquipmentSlot.valueOf(entry.getKey().trim().toUpperCase(Locale.ROOT));
                actor.setEquipment(slot, entry.getValue());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private Player getFirstActiveViewer() {
        Iterable<Player> iterable = viewersSupplier.get();
        if (iterable == null) {
            return null;
        }
        for (Player p : iterable) {
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Optional<Actor> resolve(String actorId) {
        if (actorId == null || !active) {
            return Optional.empty();
        }
        return Optional.ofNullable(actors.get(actorId));
    }

    @Override
    public Optional<Vector3d> findPosition(String actorId) {
        return resolve(actorId)
                .flatMap(Actor::asMovable)
                .map(Movable::position);
    }

    public Map<String, Actor> activeActors() {
        return Collections.unmodifiableMap(actors);
    }

    public boolean isActive() {
        return active;
    }

    public void cleanup() {
        active = false;
        for (Actor actor : actors.values()) {
            try {
                actor.despawn();
            } catch (Throwable ignored) {
            }
        }
        actors.clear();
    }
}
