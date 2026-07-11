package dev.loki.lomines.command.common;

import dev.loki.lomines.LoMinesPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class LoMinesTabCompleter implements TabCompleter {

    private static final List<String> ALL_COMMANDS = List.of(
            "create", "delete", "reset", "reload", "list", "wand", "group",
            "stats", "top", "maskscan", "edit", "setteleport", "setspawn",
            "clearspawn", "info", "tp", "copy", "regions", "addregion", "removeregion", "help"
    );

    private final LoMinesPlugin plugin;
    private final SubcommandCompleter completer;
    private final PermissionPredicate permission;

    public LoMinesTabCompleter(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.completer = new SubcommandCompleter(plugin);
        this.permission = new PermissionPredicate();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!isLoMinesCommand(command.getName())) {
            return null;
        }

        String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        return switch (args.length) {
            case 1 -> completeSubcommands(sender, partial);
            case 2 -> completer.completeSecondArg(sender, args[0].toLowerCase(), partial);
            case 3 -> completer.completeThirdArg(args[0].toLowerCase(), args[1], partial);
            default -> new ArrayList<>();
        };
    }

    private boolean isLoMinesCommand(String name) {
        return "lm".equalsIgnoreCase(name) ||
                "lomines".equalsIgnoreCase(name) ||
                "mine".equalsIgnoreCase(name) ||
                "mines".equalsIgnoreCase(name);
    }

    private List<String> completeSubcommands(CommandSender sender, String partial) {
        List<String> completions = new ArrayList<>();
        for (String cmd : ALL_COMMANDS) {
            if (cmd.startsWith(partial) && permission.hasPermission(sender, cmd)) {
                completions.add(cmd);
            }
        }
        return completions;
    }
}

