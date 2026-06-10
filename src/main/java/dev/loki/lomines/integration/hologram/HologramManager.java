package dev.loki.lomines.integration.hologram;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.ui.HologramConfig;
import dev.loki.lomines.integration.hologram.provider.DecentHologramsProvider;
import dev.loki.lomines.integration.hologram.provider.HolographicDisplaysProvider;
import dev.loki.lomines.util.format.ColorUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages holograms for all mines.
 * Displays mine information above each mine region.
 */
public final class HologramManager {

    private final LoMinesPlugin plugin;
    private final Map<String, Integer> activeTasks = new ConcurrentHashMap<>();
    private HologramProvider provider;

    public HologramManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
        initializeProvider();
    }

    private void initializeProvider() {
        // Try HolographicDisplays first, then DecentHolograms
        HolographicDisplaysProvider hdProvider = new HolographicDisplaysProvider();
        if (hdProvider.isAvailable()) {
            this.provider = hdProvider;
            plugin.loLogger().info("Using HolographicDisplays for holograms");
            return;
        }

        DecentHologramsProvider dhProvider = new DecentHologramsProvider();
        if (dhProvider.isAvailable()) {
            this.provider = dhProvider;
            plugin.loLogger().info("Using DecentHolograms for holograms");
            return;
        }

        plugin.loLogger().warn("No hologram provider found. Install HolographicDisplays or DecentHolograms.");
    }

    /**
     * Creates a hologram for a mine if enabled in config.
     */
    public void createMineHologram(Mine mine) {
        if (provider == null || !provider.isAvailable()) return;

        HologramConfig config = mine.getConfig().ui().hologram();
        if (!config.enabled()) return;

        String id = "lomines_" + mine.getName();
        Location loc = calculateHologramLocation(mine, config.height());
        List<String> lines = formatLines(config.format(), mine);

        provider.createHologram(id, loc, lines);
        startUpdateTask(mine);
    }

    /**
     * Updates a mine's hologram with current data.
     */
    public void updateMineHologram(Mine mine) {
        if (provider == null || !provider.isAvailable()) return;

        String id = "lomines_" + mine.getName();
        if (!provider.exists(id)) return;

        HologramConfig config = mine.getConfig().ui().hologram();
        List<String> lines = formatLines(config.format(), mine);
        provider.updateHologram(id, lines);
    }

    /**
     * Deletes a mine's hologram.
     */
    public void deleteMineHologram(String mineName) {
        if (provider == null) return;

        String id = "lomines_" + mineName;
        provider.deleteHologram(id);

        Integer taskId = activeTasks.remove(mineName);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Toggles hologram for a mine.
     */
    public boolean toggleHologram(Mine mine) {
        if (provider == null || !provider.isAvailable()) return false;

        String id = "lomines_" + mine.getName();
        boolean currentlyEnabled = provider.exists(id);

        if (currentlyEnabled) {
            deleteMineHologram(mine.getName());
        } else {
            createMineHologram(mine);
        }

        return !currentlyEnabled;
    }

    private void startUpdateTask(Mine mine) {
        String mineName = mine.getName();

        // Cancel existing task if any
        Integer existingTask = activeTasks.remove(mineName);
        if (existingTask != null) {
            plugin.getServer().getScheduler().cancelTask(existingTask);
        }

        // Start new update task (every second)
        int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                plugin,
                () -> updateMineHologram(mine),
                20L,  // Initial delay (1 second)
                20L   // Period (1 second)
        );

        activeTasks.put(mineName, taskId);
    }

    private Location calculateHologramLocation(Mine mine, double height) {
        if (mine.getRegions().isEmpty()) {
            return null;
        }

        var region = mine.getRegions().get(0);
        double centerX = (region.getMinX() + region.getMaxX()) / 2.0 + 0.5;
        double centerZ = (region.getMinZ() + region.getMaxZ()) / 2.0 + 0.5;
        double centerY = region.getMaxY() + height;

        return new Location(region.getWorld(), centerX, centerY, centerZ);
    }

    private List<String> formatLines(List<String> format, Mine mine) {
        List<String> result = new ArrayList<>();

        for (String line : format) {
            String formatted = line
                    .replace("{mine}", mine.getName())
                    .replace("{percent}", String.format("%.1f", mine.getPercentFilled()))
                    .replace("{bar}", createProgressBar(mine.getPercentFilled()))
                    .replace("{time}", formatResetTime(mine));

            // Convert &#RRGGBB and &codes to legacy for hologram plugins
            result.add(ColorUtils.toLegacy(formatted));
        }

        return result;
    }

    private String createProgressBar(double percent) {
        int filled = (int) Math.round(percent / 10.0);
        int empty = 10 - filled;

        // Use &#RRGGBB format which works with most hologram plugins
        String bar = "&#00FF00" + // Green for filled
                "█".repeat(Math.max(0, filled)) +
                "&#808080" + // Gray for empty
                "░".repeat(Math.max(0, empty));

        return bar;
    }

    private String formatResetTime(Mine mine) {
        int ticks = mine.getTicks();
        int seconds = (int) ((6000 - ticks) / 20.0); // Assuming 5min = 6000 ticks
        if (seconds < 0) seconds = 0;

        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    /**
     * Cleans up all holograms on shutdown.
     */
    public void shutdown() {
        for (String mineName : new ArrayList<>(activeTasks.keySet())) {
            deleteMineHologram(mineName);
        }
        activeTasks.clear();
    }

    /**
     * Gets the current provider name, or null if none.
     */
    public String getProviderName() {
        return provider != null ? provider.getName() : null;
    }
}
