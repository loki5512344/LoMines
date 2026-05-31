package dev.loki.lomines.command;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.core.Mines;
import dev.loki.lomines.data.stats.Leaderboard;
import dev.loki.lomines.data.stats.LeaderboardEntry;
import dev.loki.lomines.data.stats.PlayerStats;
import dev.loki.lomines.data.stats.StatsManager;
import dev.loki.lomines.util.ErrorHandler;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Statistics commands for player mining stats and leaderboards.
 * Provides commands for viewing stats and top players.
 */
@Command(value = "lm", permission = "lomines.use")
public class StatsCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public StatsCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    private static void sendTopRows(CommandSender sender, List<LeaderboardEntry> rows) {
        if (rows.isEmpty()) {
            sender.sendMessage(Component.text("Пока нет данных.", NamedTextColor.GRAY));
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardEntry e = rows.get(i);
            String name = formatPlayerName(e.playerId());
            sender.sendMessage(Component.text(
                    (i + 1) + ". " + name + " — " + e.count(),
                    NamedTextColor.WHITE));
        }
    }

    private static String formatPlayerName(UUID id) {
        String name = Bukkit.getOfflinePlayer(id).getName();
        return name != null ? name : id.toString();
    }

    private static int clampLimit(int limit) {
        return Math.max(1, Math.min(50, limit));
    }

    private static Integer tryParsePositiveInt(String s) {
        try {
            int v = Integer.parseInt(s);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Shows mining stats for self or another player.
     * Usage: /lm stats [player]
     */
    @Subcommand(value = "stats", permission = "lomines.stats")
    public void stats(CommandSender sender, @Arg(value = "player", optional = true, defaultValue = "") String targetName) {
        if (isStatsDisabled(sender)) {
            return;
        }
        String name = targetName == null ? "" : targetName.trim();
        UUID uuid;
        if (name.isEmpty()) {
            if (!(sender instanceof Player self)) {
                errorHandler.sendError(sender, "Укажите игрока: /lm stats <ник>");
                return;
            }
            uuid = self.getUniqueId();
        } else {
            if (!sender.hasPermission("lomines.stats.others")) {
                errorHandler.handlePermissionDenied(sender, "смотреть чужую статистику");
                return;
            }
            Player online = Bukkit.getPlayerExact(name);
            if (online != null) {
                uuid = online.getUniqueId();
            } else {
                OfflinePlayer off = Bukkit.getOfflinePlayer(name);
                uuid = off.getUniqueId();
            }
        }
        StatsManager sm = plugin.getStatsManager();
        PlayerStats ps = sm.getOrCreate(uuid);
        String displayName = formatPlayerName(uuid);
        sender.sendMessage(Component.text("=== Статистика: " + displayName + " ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Всего блоков: " + ps.getTotalBlocks(), NamedTextColor.WHITE));
        int pos = sm.getLeaderboard().getPosition(uuid);
        if (pos > 0) {
            sender.sendMessage(Component.text("Место в общем топе: #" + pos, NamedTextColor.GRAY));
        }
        Map<String, Long> byMine = ps.getMineStatsSnapshot();
        if (byMine.isEmpty()) {
            return;
        }
        sender.sendMessage(Component.text("По шахтам:", NamedTextColor.GRAY));
        byMine.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(12)
                .forEach(e -> sender.sendMessage(Component.text(
                        "  " + e.getKey() + ": " + e.getValue(), NamedTextColor.DARK_AQUA)));
    }

    /**
     * Leaderboard: global or per-mine.
     * Usage: /lm top — глобально 10; /lm top 25 — глобально 25; /lm top <шахта> — по шахте;
     * /lm top <шахта> <лимит>
     */
    @Subcommand(value = "top", permission = "lomines.stats")
    public void top(
            CommandSender sender,
            @Arg(value = "arg1", optional = true, defaultValue = "") String arg1,
            @Arg(value = "arg2", optional = true, defaultValue = "") String arg2
    ) {
        if (isStatsDisabled(sender)) {
            return;
        }
        String a = arg1 == null ? "" : arg1.trim();
        String b = arg2 == null ? "" : arg2.trim();
        int limit;
        String mineName;
        if (a.isEmpty()) {
            limit = clampLimit(10);
            mineName = null;
        } else if (b.isEmpty()) {
            Integer asLimit = tryParsePositiveInt(a);
            if (asLimit != null) {
                limit = clampLimit(asLimit);
                mineName = null;
            } else {
                limit = clampLimit(10);
                mineName = a;
            }
        } else {
            mineName = a;
            Integer limParsed = tryParsePositiveInt(b);
            limit = clampLimit(limParsed != null ? limParsed : 10);
        }
        Leaderboard lb = plugin.getStatsManager().getLeaderboard();
        if (mineName != null) {
            var found = mines.find(mineName);
            if (found.isEmpty()) {
                errorHandler.handleNotFound(sender, "Шахта", mineName);
                return;
            }
            Mine m = found.get();
            String key = m.getName();
            List<LeaderboardEntry> rows = lb.getTopByMine(key, limit);
            sender.sendMessage(Component.text("=== Топ по шахте «" + key + "» ===", NamedTextColor.GOLD));
            sendTopRows(sender, rows);
            return;
        }
        List<LeaderboardEntry> rows = lb.getTopTotal(limit);
        sender.sendMessage(Component.text("=== Общий топ ===", NamedTextColor.GOLD));
        sendTopRows(sender, rows);
    }

    private boolean isStatsDisabled(CommandSender sender) {
        if (!plugin.getConfig().getBoolean("statistics-enabled", true)) {
            sender.sendMessage(Component.text("Статистика отключена в config.yml.", NamedTextColor.RED));
            return true;
        }
        return false;
    }
}
