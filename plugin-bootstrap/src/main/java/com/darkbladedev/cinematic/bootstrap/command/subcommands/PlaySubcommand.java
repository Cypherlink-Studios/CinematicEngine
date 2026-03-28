package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class PlaySubcommand implements CinematicSubcommand {
    private final CinematicService cinematicService;
    private final Logger logger;

    public PlaySubcommand(CinematicService cinematicService, Logger logger) {
        this.cinematicService = Objects.requireNonNull(cinematicService, "cinematicService");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String name() {
        return "play";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String permission() {
        return "cinematic.play";
    }

    @Override
    public String usage() {
        return "play <nombre>";
    }

    @Override
    public String description() {
        return "Inicia una cinemática por nombre.";
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Sintaxis inválida. Uso correcto: /cine play <nombre>");
        }
        String cinematicName = args[0];
        if ("demo".equalsIgnoreCase(cinematicName) && !sender.hasPermission("cinematic.demo.play")) {
            return CommandResult.failure("No tienes permiso para ejecutar la cinemática demo. Permiso: cinematic.demo.play");
        }
        CinematicActionResult result = cinematicService.play(cinematicName);
        logger.info("Solicitud de play por " + sender.getName() + " para cinemática '" + cinematicName + "'");
        return result.success() ? CommandResult.success(result.message()) : CommandResult.failure(result.message());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return cinematicService.availableCinematics().stream().sorted().toList();
        }
        return List.of();
    }
}
