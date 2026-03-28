package com.darkbladedev.cinematic.bootstrap.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CinematicSubcommand {
    String name();

    List<String> aliases();

    String permission();

    String usage();

    String description();

    CommandResult execute(CommandSender sender, String[] args);

    List<String> tabComplete(CommandSender sender, String[] args);
}
