package com.darkbladedev.cinematic.actors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.UUID;

public final class PlayerActor implements Actor {
    private final Player player;

    public PlayerActor(Player player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public UUID id() {
        return player.getUniqueId();
    }

    @Override
    public void teleport(Vector3d position) {
        Location current = player.getLocation();
        Location target = new Location(player.getWorld(), position.x, position.y, position.z, current.getYaw(), current.getPitch());
        player.teleport(target);
    }

    @Override
    public void rotate(float yaw, float pitch) {
        Location current = player.getLocation();
        Location target = new Location(player.getWorld(), current.getX(), current.getY(), current.getZ(), yaw, pitch);
        player.teleport(target);
    }

    @Override
    public boolean isValid() {
        return player.isOnline() && !player.isDead();
    }
}
