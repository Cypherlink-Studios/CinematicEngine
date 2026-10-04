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
        saveDefaultConfig();
        String mountModeStr = getConfig().getString("camera.mount-mode", "PACKET_VIRTUAL");
        com.darkbladedev.cinematic.adapters.camera.CameraMountMode mountMode;
        try {
            mountMode = com.darkbladedev.cinematic.adapters.camera.CameraMountMode.valueOf(mountModeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Modo de cámara '" + mountModeStr + "' desconocido. Usando PACKET_VIRTUAL.");
            mountMode = com.darkbladedev.cinematic.adapters.camera.CameraMountMode.PACKET_VIRTUAL;
        }

        com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge packetEventsBridge = new com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge();
        if (packetEventsBridge.isAvailable()) {
            getLogger().info("PacketEvents 2.14.0 detectado e inicializado para actores virtuales y cámara de paquetes.");
        } else {
            getLogger().warning("PacketEvents no está activo. Se utilizará modo de cámara de reserva.");
        }
        CameraRigManager cameraRigManager = new CameraRigManager(this, mountMode, packetEventsBridge);
        com.darkbladedev.cinematic.adapters.actor.SkinCacheService skinCache = new com.darkbladedev.cinematic.adapters.actor.SkinCacheService(getDataFolder().toPath().resolve("skins"));
        demoCinematicOrchestrator = new DemoCinematicOrchestrator(timelinePlayer, sceneLoader, getLogger(), cameraRigManager, skinCache, packetEventsBridge);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            demoCinematicOrchestrator.registerViewer(onlinePlayer);
        }
        getServer().getPluginManager().registerEvents(new SpectatorSafetyListener(cameraRigManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(demoCinematicOrchestrator), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(demoCinematicOrchestrator), this);

        Path localesDirectory = getDataFolder().toPath().resolve("locales");
        String defaultLanguage = getConfig().getString("locale.default", "es");
        boolean perPlayerLocale = getConfig().getBoolean("locale.per-player", true);
        com.darkbladedev.cinematic.bootstrap.i18n.MessageService messageService =
                new com.darkbladedev.cinematic.bootstrap.i18n.DefaultMessageService(localesDirectory, defaultLanguage, perPlayerLocale, getLogger());

        commandManager = new CommandManager(this, demoCinematicOrchestrator, messageService);
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
