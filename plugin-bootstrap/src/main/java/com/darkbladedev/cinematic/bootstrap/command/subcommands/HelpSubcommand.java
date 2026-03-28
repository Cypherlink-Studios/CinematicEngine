package com.darkbladedev.cinematic.bootstrap.command.subcommands;

import com.darkbladedev.cinematic.bootstrap.command.CinematicSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.CommandManager;
import com.darkbladedev.cinematic.bootstrap.command.CommandResult;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

public final class HelpSubcommand implements CinematicSubcommand {
    private final CommandManager commandManager;

    public HelpSubcommand(CommandManager commandManager) {
        this.commandManager = Objects.requireNonNull(commandManager, "commandManager");
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public List<String> aliases() {
        return List.of("?");
    }

    @Override
    public String permission() {
        return "cinematic.help";
    }

    @Override
    public String usage() {
        return "help [subcomando]";
    }

    @Override
    public String description() {
        return "Muestra ayuda general o detallada de un subcomando.";
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            commandManager.sendGeneralHelp(sender);
            return CommandResult.success("Ayuda mostrada.");
        }
        if (args.length == 1) {
            commandManager.sendSubcommandHelp(sender, args[0]);
            return CommandResult.success("Ayuda de subcomando mostrada.");
        }
        throw new IllegalArgumentException("Sintaxis inválida. Uso correcto: /cine help [subcomando]");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return commandManager.allSubcommands().stream()
                    .map(CinematicSubcommand::name)
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
