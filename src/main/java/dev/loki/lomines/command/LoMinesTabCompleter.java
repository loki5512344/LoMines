package dev.loki.lomines.command;

import dev.loki.lomines.LoMinesPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for LoMines commands.
 * Provides auto-completion suggestions for all subcommands and arguments.
 */
public class LoMinesTabCompleter implements TabCompleter {

    private final LoMinesPlugin plugin;

    // Static command lists for fast lookup
    private static final List<String> ADMIN_COMMANDS = List.of(
            "create", "delete", "reset", "reload", "list", "maskscan",
            "edit", "setteleport", "setspawn", "clearspawn"
    );

    private static final List<String> PLAYER_COMMANDS = List.of(
            "wand", "group", "stats", "top"
    );

    private static final List<String> ALL_COMMANDS = List.of(
            "create", "delete", "reset", "reload", "list", "wand", "group",
            "stats", "top", "maskscan", "edit", "setteleport", "setspawn",
            "clearspawn", "help"
    );

    public LoMinesTabCompleter(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("lm") &&
                !command.getName().equalsIgnoreCase("lomines") &&
                !command.getName().equalsIgnoreCase("mine") &&
                !command.getName().equalsIgnoreCase("mines")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - suggest subcommands
            String partial = args[0].toLowerCase();

            for (String cmd : ALL_COMMANDS) {
                if (cmd.startsWith(partial)) {
                    // Check permission
                    if (hasPermission(sender, cmd)) {
                        completions.add(cmd);
                    }
                }
            }
        } else if (args.length == 2) {
            // Second argument - context-sensitive
            String subcommand = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            switch (subcommand) {
                case "delete", "reset", "maskscan", "edit", "setteleport", "setspawn", "clearspawn" ->
                        completions.addAll(getMineNames(partial));
                case "create" -> {
                    if (partial.isEmpty()) {
                        completions.add("<name>");
                    }
                }
                case "stats" -> {
                    if (sender.hasPermission("lomines.stats.others")) {
                        completions.addAll(getOnlinePlayerNames(partial));
                    }
                }
                case "group" -> {
                    completions.add("add");
                    completions.add("remove");
                    completions.add("clear");
                }
                case "wand" -> {
                    completions.add("give");
                    completions.add("toggle");
                }
            }
        } else if (args.length == 3) {
            // Third argument
            String subcommand = args[0].toLowerCase();
            String partial = args[2].toLowerCase();

            switch (subcommand) {
                case "group" -> {
                    if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                        completions.addAll(getMineNames(partial));
                    }
                }
            }
        }

        return completions;
    }

    private boolean hasPermission(CommandSender sender, String command) {
        return switch (command) {
            case "create", "delete", "reset", "reload", "list", "maskscan", "edit" ->
                    sender.hasPermission("lomines.admin");
            case "setteleport" -> sender.hasPermission("lomines.admin.setteleport");
            case "setspawn", "clearspawn" -> sender.hasPermission("lomines.admin.setspawn");
            case "wand", "group" -> sender.hasPermission("lomines.admin.wand");
            case "stats" -> sender.hasPermission("lomines.stats");
            case "top" -> sender.hasPermission("lomines.stats");
            default -> true;
        };
    }

    private List<String> getMineNames(String partial) {
        return plugin.getMines().getAll().stream()
                .map(mine -> mine.getName())
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames(String partial) {
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}
