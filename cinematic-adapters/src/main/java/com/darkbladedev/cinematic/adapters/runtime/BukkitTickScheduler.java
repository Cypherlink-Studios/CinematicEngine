package com.darkbladedev.cinematic.adapters.runtime;

import com.darkbladedev.cinematic.runtime.scheduler.ScheduledTask;
import com.darkbladedev.cinematic.runtime.scheduler.TickScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class BukkitTickScheduler implements TickScheduler {
    private final Plugin plugin;

    public BukkitTickScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public ScheduledTask scheduleRepeating(Runnable task, long intervalTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, 1L, intervalTicks);
        return bukkitTask::cancel;
    }
}
