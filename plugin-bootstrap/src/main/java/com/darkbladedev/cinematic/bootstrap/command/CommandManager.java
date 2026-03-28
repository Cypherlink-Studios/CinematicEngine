package com.darkbladedev.cinematic.bootstrap.command;

import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.HelpSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.ListSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.PauseSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.PlaySubcommand;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.ReloadSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.ResumeSubcommand;
import com.darkbladedev.cinematic.bootstrap.command.subcommands.StopSubcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class CommandManager implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final CinematicService cinematicService;
    private final Map<String, CinematicSubcommand> commandLookup;
    private final Map<String, CinematicSubcommand> commandByName;

    public CommandManager(JavaPlugin plugin, CinematicService cinematicService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.cinematicService = Objects.requireNonNull(cinematicService, "cinematicService");
        this.commandLookup = new ConcurrentHashMap<>();
        this.commandByName = new LinkedHashMap<>();
    }

    public void registerDefaults() {
        register(new PlaySubcommand(cinematicService, plugin.getLogger()));
        register(new StopSubcommand(cinematicService, plugin.getLogger()));
        register(new PauseSubcommand(cinematicService, plugin.getLogger()));
        register(new ResumeSubcommand(cinematicService, plugin.getLogger()));
        register(new ListSubcommand(cinematicService));
        register(new ReloadSubcommand(cinematicService, plugin.getLogger()));
        register(new HelpSubcommand(this));
    }

    public void register(CinematicSubcommand subcommand) {
        Objects.requireNonNull(subcommand, "subcommand");
        String canonicalName = normalize(subcommand.name());
        commandByName.put(canonicalName, subcommand);
        commandLookup.put(canonicalName, subcommand);
        for (String alias : subcommand.aliases()) {
            commandLookup.put(normalize(alias), subcommand);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 0) {
                sendGeneralHelp(sender);
                return true;
            }
            CinematicSubcommand subcommand = find(args[0]);
            if (subcommand == null) {
                sender.sendMessage("Subcomando desconocido: " + args[0]);
                sender.sendMessage("Usa /" + label + " help para ver los comandos disponibles.");
                return true;
            }
            if (!hasPermission(sender, subcommand.permission())) {
                sender.sendMessage("No tienes permiso para usar este comando. Permiso: " + subcommand.permission());
                return true;
            }
            String[] subArgs = trimFirst(args);
            CommandResult result = subcommand.execute(sender, subArgs);
            sender.sendMessage(result.message());
            return true;
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(exception.getMessage());
            return true;
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Error ejecutando comando cinematic", exception);
            sender.sendMessage("Ocurrió un error interno al ejecutar el comando.");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filterByPrefix(availableSubcommandNames(sender), args[0]);
        }
        CinematicSubcommand subcommand = find(args[0]);
        if (subcommand == null || !hasPermission(sender, subcommand.permission())) {
            return Collections.emptyList();
        }
        List<String> suggestions = subcommand.tabComplete(sender, trimFirst(args));
        return filterByPrefix(suggestions, args[args.length - 1]);
    }

    public Collection<CinematicSubcommand> allSubcommands() {
        return commandByName.values();
    }

    public CinematicSubcommand find(String rawName) {
        return commandLookup.get(normalize(rawName));
    }

    public void sendGeneralHelp(CommandSender sender) {
        sender.sendMessage("Comandos disponibles:");
        for (CinematicSubcommand subcommand : sortedSubcommands()) {
            if (!hasPermission(sender, subcommand.permission())) {
                continue;
            }
            sender.sendMessage("/cine " + subcommand.usage() + " - " + subcommand.description());
        }
    }

    public void sendSubcommandHelp(CommandSender sender, String commandName) {
        CinematicSubcommand subcommand = find(commandName);
        if (subcommand == null) {
            sender.sendMessage("No existe ayuda para '" + commandName + "'.");
            return;
        }
        if (!hasPermission(sender, subcommand.permission())) {
            sender.sendMessage("No tienes permiso para ver esta ayuda.");
            return;
        }
        sender.sendMessage("Uso: /cine " + subcommand.usage());
        sender.sendMessage("Descripción: " + subcommand.description());
        sender.sendMessage("Permiso: " + subcommand.permission());
    }

    private List<String> availableSubcommandNames(CommandSender sender) {
        List<String> names = new ArrayList<>();
        for (CinematicSubcommand subcommand : sortedSubcommands()) {
            if (hasPermission(sender, subcommand.permission())) {
                names.add(subcommand.name());
            }
        }
        return names;
    }

    private List<CinematicSubcommand> sortedSubcommands() {
        return commandByName.values().stream()
                .sorted(Comparator.comparing(CinematicSubcommand::name))
                .toList();
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return permission == null || permission.isBlank() || sender.hasPermission(permission);
    }

    private String normalize(String input) {
        return input.toLowerCase(Locale.ROOT);
    }

    private String[] trimFirst(String[] input) {
        if (input.length <= 1) {
            return new String[0];
        }
        String[] trimmed = new String[input.length - 1];
        System.arraycopy(input, 1, trimmed, 0, trimmed.length);
        return trimmed;
    }

    private List<String> filterByPrefix(List<String> source, String prefix) {
        String normalizedPrefix = normalize(prefix);
        List<String> result = new ArrayList<>();
        for (String candidate : source) {
            if (normalize(candidate).startsWith(normalizedPrefix)) {
                result.add(candidate);
            }
        }
        return result;
    }
}
