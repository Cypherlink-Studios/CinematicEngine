package com.darkbladedev.cinematic.bootstrap;

import com.darkbladedev.cinematic.adapters.runtime.BukkitTickScheduler;
import com.darkbladedev.cinematic.bootstrap.command.CommandManager;
import com.darkbladedev.cinematic.runtime.TimelinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CinematicEnginePlugin extends JavaPlugin {
    private TimelinePlayer timelinePlayer;
    private DemoCinematicOrchestrator demoCinematicOrchestrator;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        timelinePlayer = new TimelinePlayer(new BukkitTickScheduler(this));
        timelinePlayer.start();

        demoCinematicOrchestrator = new DemoCinematicOrchestrator(timelinePlayer, getLogger());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            demoCinematicOrchestrator.registerViewer(onlinePlayer);
        }
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(demoCinematicOrchestrator), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(demoCinematicOrchestrator), this);

        commandManager = new CommandManager(this, demoCinematicOrchestrator);
        commandManager.registerDefaults();
        PluginCommand cinematicCommand = getCommand("cinematic");
        if (cinematicCommand == null) {
            throw new IllegalStateException("El comando raíz 'cinematic' no está registrado en plugin.yml");
        }
        cinematicCommand.setExecutor(commandManager);
        cinematicCommand.setTabCompleter(commandManager);
    }

    @Override
    public void onDisable() {
        if (timelinePlayer != null) {
            timelinePlayer.stop();
        }
    }
}
