package dev.loki.lomines.command.common;

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

    private static final List<String> ALL_COMMANDS = List.of(
            "create", "delete", "reset", "reload", "list", "wand", "group",
            "stats", "top", "maskscan", "edit", "setteleport", "setspawn",
            "clearspawn", "info", "tp", "copy", "regions", "addregion", "removeregion", "help"
    );
    private static final List<String> BOOLEAN_VALUES = List.of("true", "false");
    private static final List<String> TOP_LIMITS = List.of("5", "10", "15", "20", "25", "50");
    private final LoMinesPlugin plugin;

    public LoMinesTabCompleter(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!isLoMinesCommand(command.getName())) {
            return null;
        }

        String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        return switch (args.length) {
            case 1 -> completeSubcommands(sender, partial);
            case 2 -> completeSecondArg(sender, args[0].toLowerCase(), partial);
            case 3 -> completeThirdArg(args[0].toLowerCase(), args[1], partial);
            default -> new ArrayList<>();
        };
    }

    private boolean isLoMinesCommand(String name) {
        return name.equalsIgnoreCase("lm") ||
                name.equalsIgnoreCase("lomines") ||
                name.equalsIgnoreCase("mine") ||
                name.equalsIgnoreCase("mines");
    }

    private List<String> completeSubcommands(CommandSender sender, String partial) {
        List<String> completions = new ArrayList<>();
        for (String cmd : ALL_COMMANDS) {
            if (cmd.startsWith(partial) && hasPermission(sender, cmd)) {
                completions.add(cmd);
            }
        }
        return completions;
    }

    private List<String> completeSecondArg(CommandSender sender, String subcommand, String partial) {
        return switch (subcommand) {
            case "create" -> completeCreate(partial);
            case "delete", "maskscan", "edit", "setteleport", "setspawn", "clearspawn", "info",
                 "regions", "addregion" -> filterStartsWith(getMineNames(), partial);
            case "reset" -> completeReset(partial);
            case "stats" -> completeStats(sender, partial);
            case "top" -> completeTopFirstArg(partial);
            case "tp" -> completeTeleport(sender, partial);
            case "copy" -> completeCopyFirstArg(partial);
            case "removeregion" -> completeRemoveRegion(sender, partial);
            default -> new ArrayList<>();
        };
    }

    private List<String> completeThirdArg(String subcommand, String arg2, String partial) {
        return switch (subcommand) {
            case "reset" -> filterStartsWith(BOOLEAN_VALUES, partial);
            case "top" -> completeTopSecondArg(arg2, partial);
            case "copy" -> filterStartsWith(getMineNames(), partial);
            case "removeregion" -> completeRegionIndex(arg2, partial);
            default -> new ArrayList<>();
        };
    }

    private List<String> completeRegionIndex(String mineName, String partial) {
        var mine = plugin.getMines().find(mineName);
        if (mine.isEmpty()) {
            return new ArrayList<>();
        }
        int regionCount = mine.get().getRegions().size();
        List<String> indices = new ArrayList<>();
        for (int i = 1; i <= regionCount; i++) {
            indices.add(String.valueOf(i));
        }
        return filterStartsWith(indices, partial);
    }

    private List<String> completeCreate(String partial) {
        if (partial.isEmpty()) {
            return new ArrayList<>(List.of("<name>"));
        }
        return new ArrayList<>();
    }

    private List<String> completeReset(String partial) {
        List<String> mines = filterStartsWith(getMineNames(), partial);
        if (partial.isEmpty() || "true".startsWith(partial) || "false".startsWith(partial)) {
            mines.addAll(filterStartsWith(BOOLEAN_VALUES, partial));
        }
        return mines;
    }

    private List<String> completeStats(CommandSender sender, String partial) {
        if (!sender.hasPermission("lomines.stats.others")) {
            return new ArrayList<>();
        }
        return filterStartsWith(getOnlinePlayerNames(), partial);
    }

    private List<String> completeTopFirstArg(String partial) {
        List<String> completions = new ArrayList<>();

        completions.addAll(filterStartsWith(getMineNames(), partial));

        if (isNumericPartial(partial)) {
            completions.addAll(filterStartsWith(TOP_LIMITS, partial));
        }

        return completions;
    }

    private List<String> completeTopSecondArg(String firstArg, String partial) {
        boolean firstArgWasMine = plugin.getMines().find(firstArg).isPresent();

        if (firstArgWasMine && isNumericPartial(partial)) {
            return filterStartsWith(TOP_LIMITS, partial);
        }

        if (!firstArgWasMine && isNumericPartial(firstArg)) {
            return filterStartsWith(getMineNames(), partial);
        }

        return new ArrayList<>();
    }

    private List<String> completeTeleport(CommandSender sender, String partial) {
        if (!sender.hasPermission("lomines.teleport")) {
            return new ArrayList<>();
        }
        return filterStartsWith(getMineNames(), partial);
    }

    private List<String> completeCopyFirstArg(String partial) {
        return filterStartsWith(getMineNames(), partial);
    }

    private List<String> completeRemoveRegion(CommandSender sender, String partial) {
        if (!sender.hasPermission("lomines.admin.regions")) {
            return new ArrayList<>();
        }
        return filterStartsWith(getMineNames(), partial);
    }

    private boolean isNumericPartial(String partial) {
        return partial.isEmpty() || partial.matches("\\d*");
    }

    private List<String> filterStartsWith(List<String> options, String partial) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }

    private List<String> getMineNames() {
        return plugin.getMines().getAll().stream()
                .map(mine -> mine.getName())
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private boolean hasPermission(CommandSender sender, String cmd) {
        return switch (cmd) {
            case "create", "delete", "reload", "list", "maskscan", "edit", "info" ->
                    sender.hasPermission("lomines.admin");
            case "reset" -> sender.hasPermission("lomines.admin.reset");
            case "setteleport" -> sender.hasPermission("lomines.admin.setteleport");
            case "setspawn", "clearspawn" -> sender.hasPermission("lomines.admin.setspawn");
            case "wand", "group" -> sender.hasPermission("lomines.admin.wand");
            case "stats", "top" -> sender.hasPermission("lomines.stats");
            case "tp" -> sender.hasPermission("lomines.teleport");
            case "copy" -> sender.hasPermission("lomines.admin.copy");
            case "regions", "addregion", "removeregion" -> sender.hasPermission("lomines.admin.regions");
            case "help" -> sender.hasPermission("lomines.use");
            default -> true;
        };
    }
}
