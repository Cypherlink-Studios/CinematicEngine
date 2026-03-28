package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class PauseSubcommand implements CinematicSubcommand {
    private final CinematicService cinematicService;
    private final Logger logger;

    public PauseSubcommand(CinematicService cinematicService, Logger logger) {
        this.cinematicService = Objects.requireNonNull(cinematicService, "cinematicService");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String name() {
        return "pause";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String permission() {
        return "cinematic.pause";
    }

    @Override
    public String usage() {
        return "pause";
    }

    @Override
    public String description() {
        return "Pausa la cinemática actual.";
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Sintaxis inválida. Uso correcto: /cine pause");
        }
        CinematicActionResult result = cinematicService.pause();
        logger.info("Solicitud de pause por " + sender.getName());
        return result.success() ? CommandResult.success(result.message()) : CommandResult.failure(result.message());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
