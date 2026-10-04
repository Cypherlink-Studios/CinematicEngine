package com.darkbladedev.cinematic.actors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.UUID;

public final class PlayerActor implements Actor, Movable, Animatable, Equippable {
    private final Player player;
    private ActorPose currentPose = ActorPose.STANDING;
    private ItemUsageState currentItemUsage = ItemUsageState.NONE;

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

    // --- Movable ---
    @Override
    public Vector3d position() {
        Location loc = player.getLocation();
        return new Vector3d(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public void moveTo(Vector3d position, boolean instant) {
        teleport(position);
    }

    @Override
    public void rotateHead(float headYaw, float headPitch) {
        rotate(headYaw, headPitch);
    }

    @Override
    public float yaw() {
        return player.getLocation().getYaw();
    }

    @Override
    public float pitch() {
        return player.getLocation().getPitch();
    }

    @Override
    public float headYaw() {
        return player.getEyeLocation().getYaw();
    }

    // --- Animatable ---
    @Override
    public void setPose(ActorPose pose) {
        this.currentPose = Objects.requireNonNull(pose, "pose");
        try {
            Pose bukkitPose = switch (pose) {
                case STANDING -> Pose.STANDING;
                case CROUCHING -> Pose.SNEAKING;
                case SWIMMING -> Pose.SWIMMING;
                case SLEEPING -> Pose.SLEEPING;
                case FALL_FLYING -> Pose.FALL_FLYING;
                case SPIN_ATTACK -> Pose.SPIN_ATTACK;
            };
            player.setPose(bukkitPose, true);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public ActorPose pose() {
        return currentPose;
    }

    @Override
    public void triggerAction(ActorAction action) {
        Objects.requireNonNull(action, "action");
        try {
            switch (action) {
                case SWING_MAIN_HAND -> player.swingMainHand();
                case SWING_OFF_HAND -> player.swingOffHand();
                case HURT -> player.playHurtAnimation(player.getLocation().getYaw());
                case CRITICAL_HIT, MAGIC_CRITICAL -> {}
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setItemUsage(ItemUsageState state) {
        this.currentItemUsage = Objects.requireNonNull(state, "state");
    }

    @Override
    public ItemUsageState itemUsage() {
        return currentItemUsage;
    }

    // --- Equippable ---
    @Override
    public void setEquipment(EquipmentSlot slot, String itemKey) {
        Objects.requireNonNull(slot, "slot");
        if (itemKey == null || itemKey.isBlank() || "AIR".equalsIgnoreCase(itemKey)) {
            setBukkitItem(slot, null);
            return;
        }
        Material mat = Material.matchMaterial(itemKey);
        if (mat != null) {
            setBukkitItem(slot, new ItemStack(mat));
        }
    }

    @Override
    public String getEquipment(EquipmentSlot slot) {
        ItemStack stack = getBukkitItem(slot);
        return stack == null ? "AIR" : stack.getType().name();
    }

    private void setBukkitItem(EquipmentSlot slot, ItemStack item) {
        try {
            switch (slot) {
                case MAIN_HAND -> player.getInventory().setItemInMainHand(item);
                case OFF_HAND -> player.getInventory().setItemInOffHand(item);
                case HELMET -> player.getInventory().setHelmet(item);
                case CHESTPLATE -> player.getInventory().setChestplate(item);
                case LEGGINGS -> player.getInventory().setLeggings(item);
                case BOOTS -> player.getInventory().setBoots(item);
            }
        } catch (Throwable ignored) {
        }
    }

    private ItemStack getBukkitItem(EquipmentSlot slot) {
        try {
            return switch (slot) {
                case MAIN_HAND -> player.getInventory().getItemInMainHand();
                case OFF_HAND -> player.getInventory().getItemInOffHand();
                case HELMET -> player.getInventory().getHelmet();
                case CHESTPLATE -> player.getInventory().getChestplate();
                case LEGGINGS -> player.getInventory().getLeggings();
                case BOOTS -> player.getInventory().getBoots();
            };
        } catch (Throwable ignored) {
            return null;
        }
    }
}
