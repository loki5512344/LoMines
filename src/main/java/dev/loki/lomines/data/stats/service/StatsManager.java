package dev.loki.lomines.data.stats.service;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.Leaderboard;
import dev.loki.lomines.data.stats.model.PlayerStats;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StatsManager {

    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();
    private final LoMinesPlugin plugin;
    private final Leaderboard leaderboard;
    private final StatsPersistence persistence;

    public StatsManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.leaderboard = new Leaderboard(this);
        this.persistence = new StatsPersistence(plugin, this);
    }

    public StatsPersistence getPersistence() {
        return persistence;
    }

    public void load() throws IOException {
        persistence.load(stats);
    }

    public void save() throws IOException {
        persistence.save(stats);
    }

    public void startAutoSave() {
        persistence.startAutoSave(() -> {
            try {
                save();
                plugin.getLogger().info("Statistics auto-saved");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to auto-save statistics: " + e.getMessage());
            }
        });
    }

    public void stopAutoSave() {
        persistence.stopAutoSave();
    }

    public PlayerStats getOrCreate(UUID playerId) {
        return stats.computeIfAbsent(playerId, PlayerStats::new);
    }

    public void incrementBlocks(UUID playerId, String mineName) {
        PlayerStats playerStats = getOrCreate(playerId);
        playerStats.incrementTotal();
        playerStats.incrementMine(mineName);
        leaderboard.invalidateCache();
    }

    public Collection<PlayerStats> getAllStats() {
        return Collections.unmodifiableCollection(stats.values());
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
