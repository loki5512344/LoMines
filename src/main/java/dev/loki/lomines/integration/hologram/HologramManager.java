package dev.loki.lomines.integration.hologram;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.data.config.ui.HologramConfig;
import dev.loki.lomines.integration.hologram.provider.DecentHologramsProvider;
import dev.loki.lomines.integration.hologram.provider.HolographicDisplaysProvider;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramManager {

    private final LoMinesPlugin plugin;
    private final HologramRenderer renderer;
    private final Map<String, Integer> activeTasks = new ConcurrentHashMap<>();
    private HologramProvider provider;

    public HologramManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.renderer = new HologramRenderer();
        initializeProvider();
    }

    private void initializeProvider() {
        // Try HolographicDisplays first, then DecentHolograms
        HolographicDisplaysProvider hdProvider = new HolographicDisplaysProvider();
        if (hdProvider.isAvailable()) {
            this.provider = hdProvider;
            plugin.getLogger().info("Using HolographicDisplays for holograms");
            return;
        }

        DecentHologramsProvider dhProvider = new DecentHologramsProvider();
        if (dhProvider.isAvailable()) {
            this.provider = dhProvider;
            plugin.getLogger().info("Using DecentHolograms for holograms");
            return;
        }

        plugin.getLogger().warning("No hologram provider found. Install HolographicDisplays or DecentHolograms.");
    }

    /**
     * Creates a hologram for a mine if enabled in config.
     */
    public void createMineHologram(Mine mine) {
        if (provider == null || !provider.isAvailable()) {
            return;
        }
        HologramConfig config = mine.getConfig().ui().hologram();
        if (!config.enabled()) {
            return;
        }
        String id = "lomines_" + mine.getName();
        Location loc = renderer.calculateLocation(mine, config.height());
        List<String> lines = renderer.formatLines(config.format(), mine);

        provider.createHologram(id, loc, lines);
        startUpdateTask(mine);
    }

    public void updateMineHologram(Mine mine) {
        if (provider == null || !provider.isAvailable()) {
            return;
        }
        String id = "lomines_" + mine.getName();
        if (!provider.exists(id)) {
            return;
        }
        HologramConfig config = mine.getConfig().ui().hologram();
        List<String> lines = renderer.formatLines(config.format(), mine);
        provider.updateHologram(id, lines);
    }

    /**
     * Deletes a mine's hologram.
     */
    public void deleteMineHologram(String mineName) {
        if (provider == null) {
            return;
        }
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
        if (provider == null || !provider.isAvailable()) {
            return false;
        }

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
