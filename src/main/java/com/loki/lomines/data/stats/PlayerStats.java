package com.loki.lomines.data.stats;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores statistics for a single player.
 * This is a placeholder implementation that will be completed in task 5.1.
 */
public final class PlayerStats {
    
    private final UUID playerId;
    private final AtomicLong totalBlocks = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> mineStats = new ConcurrentHashMap<>();
    
    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public long getTotalBlocks() {
        return totalBlocks.get();
    }
    
    public long getMineBlocks(String mineName) {
        AtomicLong counter = mineStats.get(mineName.toLowerCase());
        return counter != null ? counter.get() : 0;
    }
    
    public void incrementTotal() {
        totalBlocks.incrementAndGet();
    }
    
    public void incrementMine(String mineName) {
        mineStats.computeIfAbsent(mineName.toLowerCase(), k -> new AtomicLong(0))
                 .incrementAndGet();
    }
    
    public void setTotalBlocks(long value) {
        totalBlocks.set(value);
    }
    
    public void setMineBlocks(String mineName, long value) {
        mineStats.computeIfAbsent(mineName.toLowerCase(), k -> new AtomicLong(0))
                 .set(value);
    }
    
    public java.util.Map<String, Long> getMineStatsSnapshot() {
        java.util.Map<String, Long> snapshot = new java.util.HashMap<>();
        mineStats.forEach((mine, counter) -> snapshot.put(mine, counter.get()));
        return snapshot;
    }
}
