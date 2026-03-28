package com.darkbladedev.cinematic.adapters.camera;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;

public final class ProtocolLibBridge {
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
    }

    public ProtocolManager protocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }
}
