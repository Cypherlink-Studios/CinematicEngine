package com.darkbladedev.cinematic.adapters.camera;

/**
 * Defines the mounting strategy for cinematic camera rigs.
 */
public enum CameraMountMode {
    /**
     * Prioritized packet-only camera using PacketEvents {@code WrapperPlayServerCamera}.
     * Mounts the viewer directly to a clientbound virtual entity without spawning or ticking
     * any entities in the Minecraft server world.
     */
    PACKET_VIRTUAL,

    /**
     * Server-side Display Entity rig using Paper's {@code ItemDisplay}.
     * Spawns an entity into the server world with client-side interpolation; ideal for
     * long-distance streaming across chunks.
     */
    SERVER_DISPLAY
}
