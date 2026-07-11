package dev.loki.lomines.data.stats.service;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class StatsPersistence {

    private final LoMinesPlugin plugin;
    private final Path statsFile;
    private final StatsManager statsManager;
    private BukkitTask saveTask;

    public StatsPersistence(LoMinesPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsFile = plugin.getDataFolder().toPath().resolve("stats.yml");
        this.statsManager = statsManager;
    }

    public void load(Map<UUID, PlayerStats> stats) throws IOException {
        if (!Files.exists(statsFile)) {
            plugin.getLogger().info("Stats file does not exist, starting with empty statistics");
            return;
        }

        File file = statsFile.toFile();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection playersSection = yaml.getConfigurationSection("players");
        if (playersSection == null) {
            plugin.getLogger().info("No players section found in stats.yml");
            return;
        }

        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidString);

                if (playerSection == null) {
                    continue;
                }

                PlayerStats playerStats = stats.computeIfAbsent(playerId, PlayerStats::new);

                long totalBlocks = playerSection.getLong("total", 0);
                playerStats.setTotalBlocks(totalBlocks);

                ConfigurationSection minesSection = playerSection.getConfigurationSection("mines");
                if (minesSection != null) {
                    for (String mineName : minesSection.getKeys(false)) {
                        long mineBlocks = minesSection.getLong(mineName, 0);
                        playerStats.setMineBlocks(mineName, mineBlocks);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in stats.yml: " + uuidString);
            }
        }

        plugin.getLogger().info("Loaded statistics for " + stats.size() + " player(s)");
    }

    public void save(Map<UUID, PlayerStats> stats) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerStats playerStats = entry.getValue();

            String path = "players." + playerId.toString();
            yaml.set(path + ".total", playerStats.getTotalBlocks());

            Map<String, Long> mineStats = playerStats.getMineStatsSnapshot();
            if (!mineStats.isEmpty()) {
                for (Map.Entry<String, Long> mineEntry : mineStats.entrySet()) {
                    yaml.set(path + ".mines." + mineEntry.getKey(), mineEntry.getValue());
                }
            }
        }

        if (!Files.exists(statsFile.getParent())) {
            Files.createDirectories(statsFile.getParent());
        }

        yaml.save(statsFile.toFile());
    }

    public void startAutoSave(Runnable saveAction) {
        long interval = 20L * 60L * 5L;
        saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, saveAction, interval, interval);
    }

    public void stopAutoSave() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
    }
}
