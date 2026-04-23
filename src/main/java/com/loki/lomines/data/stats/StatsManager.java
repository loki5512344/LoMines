package com.loki.lomines.data.stats;

import com.loki.lomines.LoMinesPlugin;
import dev.lolib.scheduler.Scheduler;
import dev.lolib.scheduler.ScheduledTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player statistics for all mines.
 * Handles loading, saving, and auto-saving of player statistics.
 */
public final class StatsManager {
    
    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();
    private final LoMinesPlugin plugin;
    private final Path statsFile;
    private final Leaderboard leaderboard;
    private ScheduledTask saveTask;
    
    public StatsManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = plugin.getDataFolder().toPath().resolve("stats.yml");
        this.leaderboard = new Leaderboard(this);
    }
    
    /**
     * Loads statistics from file.
     */
    public void load() throws IOException {
        if (!Files.exists(statsFile)) {
            plugin.loLogger().info("Stats file does not exist, starting with empty statistics");
            return;
        }
        
        File file = statsFile.toFile();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        
        ConfigurationSection playersSection = yaml.getConfigurationSection("players");
        if (playersSection == null) {
            plugin.loLogger().info("No players section found in stats.yml");
            return;
        }
        
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidString);
                
                if (playerSection == null) {
                    continue;
                }
                
                PlayerStats playerStats = getOrCreate(playerId);
                
                // Load total blocks
                long totalBlocks = playerSection.getLong("total", 0);
                playerStats.setTotalBlocks(totalBlocks);
                
                // Load mine-specific stats
                ConfigurationSection minesSection = playerSection.getConfigurationSection("mines");
                if (minesSection != null) {
                    for (String mineName : minesSection.getKeys(false)) {
                        long mineBlocks = minesSection.getLong(mineName, 0);
                        playerStats.setMineBlocks(mineName, mineBlocks);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.loLogger().warn("Invalid UUID in stats.yml: " + uuidString);
            }
        }
        
        plugin.loLogger().info("Loaded statistics for " + stats.size() + " player(s)");
    }
    
    /**
     * Saves statistics to file.
     */
    public void save() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerStats playerStats = entry.getValue();
            
            String path = "players." + playerId.toString();
            
            // Save total blocks
            yaml.set(path + ".total", playerStats.getTotalBlocks());
            
            // Save mine-specific stats
            Map<String, Long> mineStats = playerStats.getMineStatsSnapshot();
            if (!mineStats.isEmpty()) {
                for (Map.Entry<String, Long> mineEntry : mineStats.entrySet()) {
                    yaml.set(path + ".mines." + mineEntry.getKey(), mineEntry.getValue());
                }
            }
        }
        
        // Ensure parent directory exists
        if (!Files.exists(statsFile.getParent())) {
            Files.createDirectories(statsFile.getParent());
        }
        
        yaml.save(statsFile.toFile());
    }
    
    /**
     * Starts auto-save task.
     */
    public void startAutoSave() {
        // Auto-save every 5 minutes (20 ticks/second * 60 seconds * 5 minutes)
        long interval = 20L * 60L * 5L;
        
        saveTask = Scheduler.get(plugin).runTimerAsync(
            () -> {
                try {
                    save();
                    plugin.loLogger().info("Statistics auto-saved");
                } catch (IOException e) {
                    plugin.loLogger().error("Failed to auto-save statistics: " + e.getMessage());
                }
            },
            interval,
            interval
        );
    }
    
    /**
     * Stops auto-save task.
     */
    public void stopAutoSave() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
    }
    
    /**
     * Gets or creates player statistics.
     */
    public PlayerStats getOrCreate(UUID playerId) {
        return stats.computeIfAbsent(playerId, PlayerStats::new);
    }
    
    /**
     * Increments block count for a player in a specific mine.
     */
    public void incrementBlocks(UUID playerId, String mineName) {
        PlayerStats playerStats = getOrCreate(playerId);
        playerStats.incrementTotal();
        playerStats.incrementMine(mineName);
        
        // Invalidate leaderboard cache
        leaderboard.invalidateCache();
    }
    
    /**
     * Returns all player statistics.
     */
    public Collection<PlayerStats> getAllStats() {
        return Collections.unmodifiableCollection(stats.values());
    }
    
    /**
     * Returns the leaderboard instance.
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
