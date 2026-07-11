package dev.loki.lomines;

import dev.loki.lomines.core.mine.service.MineTicker;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.data.stats.service.StatsManager;
import dev.loki.lomines.integration.IntegrationManager;
import dev.loki.lomines.wand.group.GroupWandManager;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initializes and manages plugin components.
 */
final class ComponentInitializer {

    private final LoMinesPlugin plugin;

    ComponentInitializer(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    void createDirectories() throws IOException {
        Path dataFolder = plugin.getDataFolder().toPath();
        Path minesFolder = dataFolder.resolve("mines");

        if (!Files.exists(minesFolder)) {
            Files.createDirectories(minesFolder);
            plugin.getLogger().info("Created mines directory");
        }
    }

    Components initialize() {
        Mines mines = new Mines(plugin);
        GroupWandManager groupWandManager = new GroupWandManager();
        StatsManager statsManager = new StatsManager(plugin);
        IntegrationManager integrationManager = new IntegrationManager(plugin);

        plugin.getLogger().info("Core components initialized");

        return new Components(mines, groupWandManager, statsManager, integrationManager);
    }

    void loadMines(Mines mines) {
        try {
            mines.loadAll();
            plugin.getLogger().info("Loaded " + mines.getAll().size() + " mine(s)");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load mines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    MineTicker startTicker(Mines mines) {
        MineTicker ticker = new MineTicker(mines, plugin);
        ticker.start();
        return ticker;
    }

    void startStatistics(StatsManager statsManager) {
        if (plugin.getConfig().getBoolean("statistics-enabled", true)) {
            try {
                statsManager.load();
                statsManager.startAutoSave();
                plugin.getLogger().info("Statistics system enabled");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load statistics: " + e.getMessage());
            }
        }
    }

    record Components(
            Mines mines,
            GroupWandManager groupWandManager,
            StatsManager statsManager,
            IntegrationManager integrationManager
    ) {
    }
}
