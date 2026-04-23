package com.loki.lomines.data;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages leaderboard functionality with caching.
 * Provides methods to retrieve top players by total blocks or by specific mine.
 * 
 * Thread-safety: Uses volatile cache and synchronized blocks for thread-safe operations.
 * Cache is invalidated when player statistics change.
 */
public final class Leaderboard {
    
    private final StatsManager statsManager;
    
    // Cache for getTopTotal - volatile for visibility across threads
    private volatile List<LeaderboardEntry> cachedTopTotal = null;
    private final Object cacheLock = new Object();
    
    /**
     * Creates a new Leaderboard instance.
     * 
     * @param statsManager The stats manager to retrieve player statistics from
     */
    public Leaderboard(StatsManager statsManager) {
        this.statsManager = statsManager;
    }
    
    /**
     * Invalidates the cached top total leaderboard.
     * Should be called when player statistics change.
     */
    public void invalidateCache() {
        synchronized (cacheLock) {
            cachedTopTotal = null;
        }
    }
    
    /**
     * Returns the top players by total blocks mined across all mines.
     * Results are cached and reused until invalidateCache() is called.
     * 
     * @param limit Maximum number of entries to return
     * @return List of leaderboard entries sorted by descending block count
     */
    public List<LeaderboardEntry> getTopTotal(int limit) {
        // Fast path: check cache without locking
        if (cachedTopTotal != null) {
            return cachedTopTotal.subList(0, Math.min(limit, cachedTopTotal.size()));
        }
        
        // Slow path: build cache with double-checked locking
        synchronized (cacheLock) {
            // Double-check: another thread might have built the cache
            if (cachedTopTotal != null) {
                return cachedTopTotal.subList(0, Math.min(limit, cachedTopTotal.size()));
            }
            
            // Build the cache
            cachedTopTotal = statsManager.getAllStats().stream()
                .filter(stats -> stats.getTotalBlocks() > 0)
                .sorted(Comparator.comparingLong(PlayerStats::getTotalBlocks).reversed())
                .map(stats -> new LeaderboardEntry(stats.getPlayerId(), stats.getTotalBlocks()))
                .collect(Collectors.toList());
            
            return cachedTopTotal.subList(0, Math.min(limit, cachedTopTotal.size()));
        }
    }
    
    /**
     * Returns the top players for a specific mine.
     * This method does not use caching as mine-specific leaderboards are less frequently accessed.
     * 
     * @param mineName The name of the mine
     * @param limit Maximum number of entries to return
     * @return List of leaderboard entries sorted by descending block count for the specified mine
     */
    public List<LeaderboardEntry> getTopByMine(String mineName, int limit) {
        return statsManager.getAllStats().stream()
            .map(stats -> new LeaderboardEntry(stats.getPlayerId(), stats.getMineBlocks(mineName)))
            .filter(entry -> entry.getCount() > 0)
            .sorted(Comparator.comparingLong(LeaderboardEntry::getCount).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Returns the position of a player in the total leaderboard.
     * 
     * @param playerId The UUID of the player
     * @return The 1-based position of the player, or -1 if the player is not in the leaderboard
     */
    public int getPosition(UUID playerId) {
        List<LeaderboardEntry> top = getTopTotal(Integer.MAX_VALUE);
        for (int i = 0; i < top.size(); i++) {
            if (top.get(i).getPlayerId().equals(playerId)) {
                return i + 1; // 1-based position
            }
        }
        return -1;
    }
}
