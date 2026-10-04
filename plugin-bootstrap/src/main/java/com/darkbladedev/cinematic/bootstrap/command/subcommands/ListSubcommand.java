package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import com.darkbladedev.cinematic.bootstrap.i18n.MessageService;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ListSubcommand implements CinematicSubcommand {
    private final CinematicService cinematicService;
    private final MessageService messageService;

    public ListSubcommand(CinematicService cinematicService) {
        this(cinematicService, null);
    }

    public ListSubcommand(CinematicService cinematicService, MessageService messageService) {
        this.cinematicService = Objects.requireNonNull(cinematicService, "cinematicService");
        this.messageService = messageService;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String permission() {
        return "cinematic.list";
    }

    @Override
    public String usage() {
        return "list";
    }

    @Override
    public String description() {
        return "Muestra todas las cinemáticas disponibles.";
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Sintaxis inválida. Uso correcto: /cine list");
        }
        List<String> sorted = cinematicService.availableCinematics().stream().sorted().toList();
        if (sorted.isEmpty()) {
            return CommandResult.successKey(
                    "command.subcommand.list.empty",
                    Map.of(),
                    "No hay cinemáticas disponibles."
            );
        }
        String cinematicsJoined = String.join(", ", sorted);
        return CommandResult.successKey(
                "command.subcommand.list.success",
                Map.of("cinematics", cinematicsJoined),
                "Cinemáticas disponibles: " + cinematicsJoined
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
