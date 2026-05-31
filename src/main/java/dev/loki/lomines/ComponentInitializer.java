package dev.loki.lomines;

import dev.loki.lomines.core.mine.MineTicker;
import dev.loki.lomines.core.mine.Mines;
import dev.loki.lomines.data.stats.StatsManager;
import dev.loki.lomines.integration.IntegrationManager;
import dev.loki.lomines.wand.GroupWandManager;
import dev.lolib.commands.CommandManager;

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
            plugin.loLogger().info("Created mines directory");
        }
    }

    Components initialize() {
        Mines mines = new Mines(plugin);
        GroupWandManager groupWandManager = new GroupWandManager();
        StatsManager statsManager = new StatsManager(plugin);
        CommandManager commandManager = new CommandManager(plugin);
        IntegrationManager integrationManager = new IntegrationManager(plugin);

        plugin.loLogger().info("Core components initialized");

        return new Components(mines, groupWandManager, statsManager, commandManager, integrationManager);
    }

    void loadMines(Mines mines) {
        try {
            mines.loadAll();
            plugin.loLogger().info("Loaded " + mines.getAll().size() + " mine(s)");
        } catch (IOException e) {
            plugin.loLogger().error("Failed to load mines: " + e.getMessage());
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
                plugin.loLogger().info("Statistics system enabled");
            } catch (IOException e) {
                plugin.loLogger().warn("Failed to load statistics: " + e.getMessage());
            }
        }
    }

    record Components(
            Mines mines,
            GroupWandManager groupWandManager,
            StatsManager statsManager,
            CommandManager commandManager,
            IntegrationManager integrationManager
    ) {
    }
}
