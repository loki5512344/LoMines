package dev.loki.lomines.command.admin.stats;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.data.stats.model.PlayerStats;
import dev.loki.lomines.data.stats.service.StatsManager;
import dev.loki.lomines.util.ErrorHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class StatsCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public StatsCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    public void handle(CommandSender sender, String subcommand, String[] args) {
        switch (subcommand.toLowerCase()) {
            case "stats" -> {
                if (isStatsDisabled(sender)) {
                    return;
                }
                String targetName = args.length > 0 ? args[0] : "";
                UUID uuid = LeaderboardRenderer.resolvePlayerUuid(sender, targetName, errorHandler);
                if (uuid == null) {
                    return;
                }

                StatsManager sm = plugin.getStatsManager();
                PlayerStats ps = sm.getOrCreate(uuid);
                String displayName = LeaderboardRenderer.formatPlayerName(uuid);
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
            case "top" -> {
                if (isStatsDisabled(sender)) {
                    return;
                }
                String arg1 = args.length > 0 ? args[0] : "";
                String arg2 = args.length > 1 ? args[1] : "";
                LeaderboardRenderer.renderTopCommand(sender, arg1, arg2, mines, plugin);
            }
            default -> {}
        }
    }

    private boolean isStatsDisabled(CommandSender sender) {
        if (!plugin.getConfig().getBoolean("statistics-enabled", true)) {
            sender.sendMessage(Component.text("Статистика отключена в config.yml.", NamedTextColor.RED));
            return true;
        }
        return false;
    }
}
