package com.darkbladedev.cinematic.adapters.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PacketEventsBridge {

    public boolean isAvailable() {
        try {
            return Bukkit.getPluginManager() != null
                    && Bukkit.getPluginManager().isPluginEnabled("packetevents")
                    && PacketEvents.getAPI() != null
                    && PacketEvents.getAPI().isLoaded()
                    && PacketEvents.getAPI().isInitialized();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void sendPacket(Player player, PacketWrapper<?> packet) {
        if (player == null || !player.isOnline() || packet == null || !isAvailable()) {
            return;
        }
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public void broadcastPacket(Iterable<Player> viewers, PacketWrapper<?> packet) {
        if (viewers == null || packet == null || !isAvailable()) {
            return;
        }
        for (Player viewer : viewers) {
            sendPacket(viewer, packet);
        }
    }
}
