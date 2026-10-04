package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import com.darkbladedev.cinematic.bootstrap.i18n.MessageService;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class ResumeSubcommand implements CinematicSubcommand {
    private final CinematicService cinematicService;
    private final MessageService messageService;
    private final Logger logger;

    public ResumeSubcommand(CinematicService cinematicService, Logger logger) {
        this(cinematicService, null, logger);
    }

    public ResumeSubcommand(CinematicService cinematicService, MessageService messageService, Logger logger) {
        this.cinematicService = Objects.requireNonNull(cinematicService, "cinematicService");
        this.messageService = messageService;
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String name() {
        return "resume";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String permission() {
        return "cinematic.resume";
    }

    @Override
    public String usage() {
        return "resume";
    }

    @Override
    public String description() {
        return "Reanuda una cinemática pausada.";
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Sintaxis inválida. Uso correcto: /cine resume");
        }
        CinematicActionResult result = cinematicService.resume();
        logger.info("Solicitud de resume por " + sender.getName());
        if (result.messageKey() != null) {
            return result.success()
                    ? CommandResult.successKey(result.messageKey(), result.placeholders(), result.message())
                    : CommandResult.failureKey(result.messageKey(), result.placeholders(), result.message());
        }
        return result.success() ? CommandResult.success(result.message()) : CommandResult.failure(result.message());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
