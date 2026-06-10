package dev.loki.lomines.integration.placeholder;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for LoMines.
 * Provides placeholders for mine information and player statistics.
 */
public final class LoMinesPlaceholderExpansion extends PlaceholderExpansion {

    private final LoMinesPlugin plugin;

    public LoMinesPlaceholderExpansion(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "lomines";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty()
                ? "loki"
                : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @Nullable
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] parts = params.toLowerCase().split("_");
        if (parts.length == 0) {
            return null;
        }

        return switch (parts[0]) {
            case "mine" -> handleMinePlaceholder(parts);
            case "player" -> handlePlayerPlaceholder(player, parts);
            case "count" -> String.valueOf(plugin.getMines().getAll().size());
            default -> null;
        };
    }

    /**
     * Handles mine-related placeholders.
     * Format: %lomines_mine_<name>_<attribute>%
     */
    private @Nullable String handleMinePlaceholder(String[] parts) {
        if (parts.length < 3) {
            return null;
        }

        // parts[1] is mine name, parts[2] is attribute
        String mineName = parts[1];
        String attribute = parts[2];

        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            return "Unknown";
        }

        return switch (attribute) {
            case "name" -> mine.getName();
            case "blocks" -> String.valueOf(mine.getBlocks());
            case "total" -> String.valueOf(mine.getTotalVolume());
            case "percent" -> String.format("%.1f", mine.getPercentFilled());
            case "percentint" -> String.valueOf((int) mine.getPercentFilled());
            case "world" -> mine.getConfig().worldName();
            case "remaining" -> String.valueOf(mine.getTotalVolume() - mine.getBlocks());
            case "resettime" -> formatResetTime(mine);
            case "resetseconds" -> String.valueOf(getResetSeconds(mine));
            default -> null;
        };
    }

    /**
     * Handles player-related placeholders.
     * Format: %lomines_player_<attribute>%
     */
    private @Nullable String handlePlayerPlaceholder(Player player, String[] parts) {
        if (player == null || parts.length < 2) {
            return null;
        }

        String attribute = parts[1];
        var statsManager = plugin.getStatsManager();
        var stats = statsManager.getOrCreate(player.getUniqueId());

        return switch (attribute) {
            case "blocksmined" -> String.valueOf(stats.getTotalBlocks());
            case "minesreset" -> "0";
            case "playtime" -> formatPlayTime(0);
            case "rank" -> String.valueOf(statsManager.getLeaderboard().getPosition(player.getUniqueId()));
            default -> null;
        };
    }

    private String formatResetTime(Mine mine) {
        int ticks = mine.getTicks();
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    private int getResetSeconds(Mine mine) {
        return mine.getTicks() / 20;
    }

    private String formatPlayTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, mins);
        }
        return String.format("%dm", mins);
    }
}
