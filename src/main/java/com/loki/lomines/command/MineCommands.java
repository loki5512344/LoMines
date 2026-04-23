package com.loki.lomines.command;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.core.Mine;
import com.loki.lomines.core.Mines;
import com.loki.lomines.data.ConfigParseException;
import com.loki.lomines.data.Leaderboard;
import com.loki.lomines.data.LeaderboardEntry;
import com.loki.lomines.data.PlayerStats;
import com.loki.lomines.data.StatsManager;
import com.loki.lomines.wand.GroupWandItem;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.scheduler.Scheduler;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main command class for mine management.
 * Provides commands for creating, deleting, resetting, reloading, and listing mines.
 */
@Command(value = "lm", permission = "lomines.use")
public class MineCommands {
    
    private final LoMinesPlugin plugin;
    private final Mines mines;
    
    public MineCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
    }
    
    @Subcommand(value = "wand", permission = "lomines.admin.wand")
    public void wand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Только для игрока."));
            return;
        }
        player.getInventory().addItem(GroupWandItem.create(plugin));
        player.sendMessage(Component.text(
            "Палочка группы: ЛКМ/ПКМ по блоку — углы, Shift+ПКМ — меню (9 шахт). Задайте /lm group <префикс>",
            NamedTextColor.GREEN));
    }
    
    @Subcommand(value = "group", permission = "lomines.admin.wand")
    public void group(CommandSender sender, @Arg("prefix") String prefix) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Префикс задаётся в игре."));
            return;
        }
        plugin.getGroupWandManager().getSession(player.getUniqueId()).setBaseName(prefix);
        String p = prefix.trim();
        player.sendMessage(Component.text("Префикс: " + p + " → файлы " + p + "_1.yml … " + p + "_9.yml", NamedTextColor.AQUA));
    }
    
    /**
     * Creates a new mine with the given name.
     * Usage: /lm create <name>
     */
    @Subcommand(value = "create", permission = "lomines.admin.create")
    public void create(CommandSender sender, @Arg("name") String name) {
        try {
            mines.create(name);
            sender.sendMessage(Component.text("Successfully created mine: " + name));
            sender.sendMessage(Component.text(
                "Edit selection & contents in plugins/LoMines/mines/" + name + ".yml — "
                    + "for shape-based ore (not full cuboid), paint mask.marker blocks, then: /lm maskscan " + name));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Mine already exists: " + name));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Failed to create mine: " + e.getMessage()));
            plugin.loLogger().error("Failed to create mine " + name, e);
        }
    }
    
    /**
     * Deletes an existing mine.
     * Usage: /lm delete <name>
     */
    @Subcommand(value = "delete", permission = "lomines.admin.delete")
    public void delete(CommandSender sender, @Arg("name") String name) {
        try {
            mines.delete(name);
            sender.sendMessage(Component.text("Successfully deleted mine: " + name));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Mine not found: " + name));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Failed to delete mine: " + e.getMessage()));
            plugin.loLogger().error("Failed to delete mine " + name, e);
        }
    }
    
    /**
     * Resets a mine by refilling all its regions.
     * Usage: /lm reset <mine> [silent]
     */
    @Subcommand(value = "reset", permission = "lomines.admin.reset")
    public void reset(
        CommandSender sender,
        @Arg("mine") String mineName,
        @Arg(value = "silent", optional = true, defaultValue = "false") boolean silent
    ) {
        try {
            Mine mine = mines.get(mineName);
            mine.reset(silent);
            sender.sendMessage(Component.text("Resetting mine: " + mineName));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Mine not found: " + mineName));
        }
    }
    
    /**
     * Reloads all mine configurations from disk.
     * Usage: /lm reload
     */
    @Subcommand(value = "reload", permission = "lomines.admin.reload")
    public void reload(CommandSender sender) {
        try {
            // Stop all existing mines
            mines.getAll().forEach(Mine::stop);
            
            // Reload all mines
            mines.loadAll();
            
            int count = mines.getAll().size();
            sender.sendMessage(Component.text("Successfully reloaded " + count + " mine(s)"));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Failed to reload mines: " + e.getMessage()));
            plugin.loLogger().error("Failed to reload mines", e);
        }
    }
    
    /**
     * Lists all active mines.
     * Usage: /lm list
     */
    @Subcommand(value = "list", permission = "lomines.admin.list")
    public void list(CommandSender sender) {
        Collection<Mine> allMines = mines.getAll();
        
        if (allMines.isEmpty()) {
            sender.sendMessage(Component.text("No mines found"));
            return;
        }
        
        sender.sendMessage(Component.text("=== Mines (" + allMines.size() + ") ==="));
        
        for (Mine mine : allMines) {
            String status = formatMineStatus(mine);
            sender.sendMessage(Component.text("- " + mine.getName() + " " + status));
        }
    }
    
    /**
     * Formats mine status information for display.
     */
    private String formatMineStatus(Mine mine) {
        int blocks = mine.getBlocks();
        int total = mine.getTotalVolume();
        double percent = mine.getPercentFilled();
        
        return String.format("(%d/%d blocks, %.1f%%)", blocks, total, percent);
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
                sender.sendMessage(Component.text("Укажите игрока: /lm stats <ник>", NamedTextColor.RED));
                return;
            }
            uuid = self.getUniqueId();
        } else {
            if (!sender.hasPermission("lomines.stats.others")) {
                sender.sendMessage(Component.text("Нет прав смотреть чужую статистику.", NamedTextColor.RED));
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
     * Usage: /lm top — глобально 10; /lm top 25 — глобально 25; /lm top &lt;шахта&gt; — по шахте;
     * /lm top &lt;шахта&gt; &lt;лимит&gt;
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
                sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
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
    
    private static void sendTopRows(CommandSender sender, List<LeaderboardEntry> rows) {
        if (rows.isEmpty()) {
            sender.sendMessage(Component.text("Пока нет данных.", NamedTextColor.GRAY));
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardEntry e = rows.get(i);
            String name = formatPlayerName(e.getPlayerId());
            sender.sendMessage(Component.text(
                (i + 1) + ". " + name + " — " + e.getCount(),
                NamedTextColor.WHITE));
        }
    }
    
    private static String formatPlayerName(UUID id) {
        String name = Bukkit.getOfflinePlayer(id).getName();
        return name != null ? name : id.toString();
    }
    
    private boolean isStatsDisabled(CommandSender sender) {
        if (!plugin.getConfig().getBoolean("statistics-enabled", true)) {
            sender.sendMessage(Component.text("Статистика отключена в config.yml.", NamedTextColor.RED));
            return true;
        }
        return false;
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
     * Scans selection for marker blocks ({@code mask.marker}, default pink concrete), saves mask positions and switches mine to mask fill mode.
     * Run from the server main thread (scheduled automatically).
     */
    @Subcommand(value = "maskscan", permission = "lomines.admin.maskscan")
    public void maskScan(CommandSender sender, @Arg("mine") String name) {
        Scheduler.get(plugin).run(() -> {
            try {
                int n = mines.scanAndSaveMask(name);
                sender.sendMessage(Component.text(
                    "Mask mode: saved " + n + " cell(s) for mine '" + name
                        + "'. Reset will spawn ore only at those positions."));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Mine not found: " + name));
            } catch (IOException | ConfigParseException e) {
                sender.sendMessage(Component.text("Mask scan failed: " + e.getMessage()));
                plugin.loLogger().error("maskscan failed for " + name, e);
            }
        });
    }
}
