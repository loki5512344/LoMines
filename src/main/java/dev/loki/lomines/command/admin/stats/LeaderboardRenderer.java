package dev.loki.lomines.command.admin.stats;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.data.stats.model.Leaderboard;
import dev.loki.lomines.data.stats.model.LeaderboardEntry;
import dev.loki.lomines.util.ErrorHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class LeaderboardRenderer {

    private LeaderboardRenderer() {
    }

    public static void sendTopRows(CommandSender sender, List<LeaderboardEntry> rows) {
        if (rows.isEmpty()) {
            sender.sendMessage(Component.text("Пока нет данных.", NamedTextColor.GRAY));
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardEntry e = rows.get(i);
            String name = formatPlayerName(e.playerId());
            sender.sendMessage(Component.text(
                    (i + 1) + ". " + name + " \u2014 " + e.count(),
                    NamedTextColor.WHITE));
        }
    }

    public static String formatPlayerName(UUID id) {
        String name = Bukkit.getOfflinePlayer(id).getName();
        return name != null ? name : id.toString();
    }

    public static int clampLimit(int limit) {
        return Math.max(1, Math.min(50, limit));
    }

    public static Integer tryParsePositiveInt(String s) {
        try {
            int v = Integer.parseInt(s);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static UUID resolvePlayerUuid(CommandSender sender, String targetName, ErrorHandler errorHandler) {
        String name = targetName == null ? "" : targetName.trim();
        if (name.isEmpty()) {
            if (!(sender instanceof Player self)) {
                errorHandler.sendError(sender, "Укажите игрока: /lm stats <ник>");
                return null;
            }
            return self.getUniqueId();
        } else {
            if (!sender.hasPermission("lomines.stats.others")) {
                errorHandler.handlePermissionDenied(sender, "смотреть чужую статистику");
                return null;
            }
            Player online = Bukkit.getPlayerExact(name);
            if (online != null) {
                return online.getUniqueId();
            } else {
                OfflinePlayer off = Bukkit.getOfflinePlayer(name);
                return off.getUniqueId();
            }
        }
    }

    public static void renderTopCommand(CommandSender sender, String arg1, String arg2, Mines mines, LoMinesPlugin plugin) {
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
                sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
                return;
            }
            Mine m = found.get();
            String key = m.getName();
            List<LeaderboardEntry> rows = lb.getTopByMine(key, limit);
            sender.sendMessage(Component.text("=== Топ по шахте \u00ab" + key + "\u00bb ===", NamedTextColor.GOLD));
            sendTopRows(sender, rows);
            return;
        }
        List<LeaderboardEntry> rows = lb.getTopTotal(limit);
        sender.sendMessage(Component.text("=== Общий топ ===", NamedTextColor.GOLD));
        sendTopRows(sender, rows);
    }
}
