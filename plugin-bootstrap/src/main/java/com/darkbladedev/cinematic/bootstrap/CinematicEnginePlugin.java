package com.darkbladedev.cinematic.bootstrap;

import com.darkbladedev.cinematic.adapters.camera.CameraRigManager;
import com.darkbladedev.cinematic.adapters.camera.SpectatorSafetyListener;
import com.darkbladedev.cinematic.adapters.runtime.BukkitTickScheduler;
import com.darkbladedev.cinematic.bootstrap.command.CommandManager;
import com.darkbladedev.cinematic.dsl.mapper.DefaultDslComponents;
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.dsl.mapper.SceneMapper;
import com.darkbladedev.cinematic.dsl.parser.SceneParser;
import com.darkbladedev.cinematic.dsl.parser.SnakeYamlSceneParser;
import com.darkbladedev.cinematic.dsl.registry.SceneLoader;
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.validator.SceneDtoValidator;
import com.darkbladedev.cinematic.runtime.TimelinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class CinematicEnginePlugin extends JavaPlugin {
    private TimelinePlayer timelinePlayer;
    private DemoCinematicOrchestrator demoCinematicOrchestrator;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        timelinePlayer = new TimelinePlayer(new BukkitTickScheduler(this));
        timelinePlayer.start();

        InterpolatorRegistry interpolatorRegistry = DefaultDslComponents.interpolatorRegistry();
        TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
        SceneDtoValidator sceneDtoValidator = DefaultDslComponents.validator(trackRegistry, interpolatorRegistry);
        SceneMapper sceneMapper = DefaultDslComponents.mapper(sceneDtoValidator, trackRegistry);
        SceneParser sceneParser = new SnakeYamlSceneParser();
        Path cinematicDirectory = getDataFolder().toPath().resolve("cinematics");
        SceneLoader sceneLoader = new SceneLoader(cinematicDirectory, sceneParser, sceneMapper);
        int loadedCinematics = sceneLoader.reloadAll();
        getLogger().info("Cinemáticas cargadas: " + loadedCinematics);

        CameraRigManager cameraRigManager = new CameraRigManager(this);
        demoCinematicOrchestrator = new DemoCinematicOrchestrator(timelinePlayer, sceneLoader, getLogger(), cameraRigManager);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            demoCinematicOrchestrator.registerViewer(onlinePlayer);
        }
        getServer().getPluginManager().registerEvents(new SpectatorSafetyListener(cameraRigManager), this);
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
        if (demoCinematicOrchestrator != null) {
            demoCinematicOrchestrator.cleanup();
        }
        if (timelinePlayer != null) {
            timelinePlayer.stop();
        }
    }
}
