package dev.loki.lomines.command.common;

import dev.loki.lomines.LoMinesPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SubcommandCompleter {

    private static final List<String> BOOLEAN_VALUES = List.of("true", "false");
    private static final List<String> TOP_LIMITS = List.of("5", "10", "15", "20", "25", "50");

    private final LoMinesPlugin plugin;

    public SubcommandCompleter(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> completeSecondArg(CommandSender sender, String subcommand, String partial) {
        return switch (subcommand) {
            case "create" -> completeCreate(partial);
            case "delete", "maskscan", "edit", "setteleport", "setspawn", "clearspawn", "info",
                    "regions", "addregion" -> filterStartsWith(getMineNames(), partial);
            case "reset" -> completeReset(partial);
            case "stats" -> completeStats(sender, partial);
            case "top" -> completeTopFirstArg(partial);
            case "tp" -> completeTeleport(sender, partial);
            case "copy" -> filterStartsWith(getMineNames(), partial);
            case "removeregion" -> filterStartsWith(getMineNames(), partial);
            default -> new ArrayList<>();
        };
    }

    public List<String> completeThirdArg(String subcommand, String arg2, String partial) {
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

    private boolean isNumericPartial(String partial) {
        return partial.isEmpty() || partial.matches("\\d*");
    }

    public List<String> filterStartsWith(List<String> options, String partial) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }

    public List<String> getMineNames() {
        return plugin.getMines().getAll().stream()
                .map(mine -> mine.getName())
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
