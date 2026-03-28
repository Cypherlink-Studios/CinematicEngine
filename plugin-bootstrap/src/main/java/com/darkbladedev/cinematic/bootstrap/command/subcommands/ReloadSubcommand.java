package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class ReloadSubcommand implements CinematicSubcommand {
    private final JavaPlugin plugin;
    private final Logger logger;

    public ReloadSubcommand(JavaPlugin plugin, Logger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String permission() {
        return "cinematic.reload";
    }

    @Override
    public String usage() {
        return "reload";
    }

    @Override
    public String description() {
        return "Recarga la configuración del plugin.";
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Sintaxis inválida. Uso correcto: /cine reload");
        }
        plugin.reloadConfig();
        logger.info("Configuración recargada por " + sender.getName());
        return CommandResult.success("Configuración recargada correctamente.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
