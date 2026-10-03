package com.darkbladedev.cinematic.camera.targeting;

import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import org.joml.Vector3d;

public interface LookAtStrategy {
    CameraOrientation calculateOrientation(
            long tick,
            Vector3d currentPosition,
            Vector3d velocityDirection,
            float fallbackYaw,
            float fallbackPitch,
            TimelineContext context
    );

    /**
     * Helper to compute Minecraft yaw and pitch aiming from origin to target.
     */
    static CameraOrientation computeDirection(Vector3d origin, Vector3d target, float fallbackYaw, float fallbackPitch) {
        double dx = target.x - origin.x;
        double dy = target.y - origin.y;
        double dz = target.z - origin.z;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq < 1e-8D) {
            return new CameraOrientation(fallbackYaw, fallbackPitch);
        }

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float pitch;
        if (distXZ < 1e-8D) {
            pitch = dy > 0.0D ? -90.0F : 90.0F;
            return new CameraOrientation(fallbackYaw, pitch);
        }

        pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        return new CameraOrientation(wrapDegrees(yaw), pitch);
    }

    static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }
}
