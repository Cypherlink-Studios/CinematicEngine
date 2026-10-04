package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.camera.CameraState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents an active viewer camera session in a cinematic playback.
 */
public interface CameraRigSession {

    /**
     * @return Unique ID of the viewer bound to this session.
     */
    UUID playerId();

    /**
     * @return Player's original world location prior to starting the cinematic.
     */
    Location originalLocation();

    /**
     * @return Player's original GameMode prior to starting the cinematic.
     */
    GameMode originalGameMode();

    /**
     * @return Whether the player was allowed flight prior to starting the cinematic.
     */
    boolean originalAllowFlight();

    /**
     * @return Whether the player was actively flying prior to starting the cinematic.
     */
    boolean originalFlying();

    /**
     * @return Mounting mode employed by this session.
     */
    CameraMountMode mountMode();

    /**
     * Updates the camera transform according to the calculated timeline camera state.
     *
     * @param player viewer being updated
     * @param state  target camera state
     */
    void update(Player player, CameraState state);

    /**
     * Restores the viewer to their original state and cleans up camera rig entities or packets.
     *
     * @param player viewer to restore (may be null if player disconnected)
     */
    void restore(Player player);

    /**
     * Cleans up entities, packets, or tracking without necessarily restoring player states.
     */
    void cleanup();

    /**
     * Returns the underlying world display entity if running in {@link CameraMountMode#SERVER_DISPLAY},
     * or null if running in {@link CameraMountMode#PACKET_VIRTUAL}.
     *
     * @return display entity or null
     */
    default Display rigEntity() {
        return null;
    }
}
