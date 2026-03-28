package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

public final class ListSubcommand implements CinematicSubcommand {
    private final CinematicService cinematicService;

    public ListSubcommand(CinematicService cinematicService) {
        this.cinematicService = Objects.requireNonNull(cinematicService, "cinematicService");
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
            return CommandResult.success("No hay cinemáticas disponibles.");
        }
        return CommandResult.success("Cinemáticas disponibles: " + String.join(", ", sorted));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
