package com.darkbladedev.cinematic.adapters.camera;

import com.darkbladedev.cinematic.camera.CameraOutput;
import com.darkbladedev.cinematic.camera.CameraState;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class PlayerCameraOutput implements CameraOutput {
    private final Supplier<Iterable<Player>> viewersSupplier;
    private final CameraRigManager rigManager;
    private final CameraMountMode mountMode;

    public PlayerCameraOutput(Supplier<Iterable<Player>> viewersSupplier) {
        this(viewersSupplier, null, null);
    }

    public PlayerCameraOutput(Supplier<Iterable<Player>> viewersSupplier, CameraRigManager rigManager) {
        this(viewersSupplier, rigManager, null);
    }

    public PlayerCameraOutput(
            Supplier<Iterable<Player>> viewersSupplier,
            CameraRigManager rigManager,
            CameraMountMode mountMode
    ) {
        this.viewersSupplier = Objects.requireNonNull(viewersSupplier, "viewersSupplier");
        this.rigManager = rigManager;
        this.mountMode = mountMode;
    }

    public Optional<CameraRigManager> rigManager() {
        return Optional.ofNullable(rigManager);
    }

    public Optional<CameraMountMode> mountMode() {
        return Optional.ofNullable(mountMode);
    }

    @Override
    public void apply(CameraState state) {
        if (rigManager != null) {
            if (mountMode != null) {
                rigManager.apply(viewersSupplier.get(), state, mountMode);
            } else {
                rigManager.apply(viewersSupplier.get(), state);
            }
            return;
        }

        for (Player player : viewersSupplier.get()) {
            if (player == null || !player.isOnline() || player.isDead()) {
                continue;
            }
            Location destination = new Location(
                    player.getWorld(),
                    state.position().x,
                    state.position().y,
                    state.position().z,
                    state.yaw(),
                    state.pitch()
            );
            player.teleport(destination);
        }
    }
}
