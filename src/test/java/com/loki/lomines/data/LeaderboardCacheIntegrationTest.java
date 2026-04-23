package com.loki.lomines.data;

import com.loki.lomines.LoMinesPlugin;
import dev.lolib.core.LoLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for Leaderboard caching with StatsManager.
 * Verifies that cache invalidation works correctly when statistics change.
 */
class LeaderboardCacheIntegrationTest {
    
    private StatsManager statsManager;
    private Leaderboard leaderboard;
    
    @BeforeEach
    void setUp() {
        // Create a mock plugin
        LoMinesPlugin mockPlugin = mock(LoMinesPlugin.class);
        LoLogger loLogger = mock(LoLogger.class);
        File mockDataFolder = new File(System.getProperty("java.io.tmpdir"), "lomines-test");
        when(mockPlugin.getDataFolder()).thenReturn(mockDataFolder);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
        when(mockPlugin.loLogger()).thenReturn(loLogger);
        
        statsManager = new StatsManager(mockPlugin);
        leaderboard = statsManager.getLeaderboard();
    }
    
    @Test
    void testCacheInvalidatedOnIncrementBlocks() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        // Set initial stats
        statsManager.getOrCreate(player1).setTotalBlocks(100);
        
        // Build cache
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        assertEquals(1, top1.size(), "Should have 1 player initially");
        assertEquals(player1, top1.get(0).getPlayerId());
        
        // Increment blocks for player2 - this should invalidate cache
        statsManager.incrementBlocks(player2, "mine1");
        
        // Get top again - should rebuild cache with new player
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        assertEquals(2, top2.size(), "Should have 2 players after increment");
        
        // Verify order (player1 has 100, player2 has 1)
        assertEquals(player1, top2.get(0).getPlayerId(), "Player1 should still be first");
        assertEquals(player2, top2.get(1).getPlayerId(), "Player2 should be second");
    }
    
    @Test
    void testCacheRebuildsWithCorrectOrder() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        // Set initial stats
        statsManager.getOrCreate(player1).setTotalBlocks(50);
        statsManager.getOrCreate(player2).setTotalBlocks(100);
        
        // Build cache
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        assertEquals(player2, top1.get(0).getPlayerId(), "Player2 should be first initially");
        
        // Increment player1 many times to overtake player2
        for (int i = 0; i < 60; i++) {
            statsManager.incrementBlocks(player1, "mine1");
        }
        
        // Get top again - cache should be rebuilt with new order
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        assertEquals(player1, top2.get(0).getPlayerId(), "Player1 should be first after increments");
        assertEquals(110, top2.get(0).getCount(), "Player1 should have 110 blocks");
        assertEquals(player2, top2.get(1).getPlayerId(), "Player2 should be second");
        assertEquals(100, top2.get(1).getCount(), "Player2 should still have 100 blocks");
    }
    
    @Test
    void testMultipleIncrementsInvalidateCache() {
        UUID player1 = UUID.randomUUID();
        
        // Build empty cache
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        assertTrue(top1.isEmpty(), "Should be empty initially");
        
        // Increment blocks multiple times
        for (int i = 0; i < 5; i++) {
            statsManager.incrementBlocks(player1, "mine1");
        }
        
        // Get top - should show updated count
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        assertEquals(1, top2.size());
        assertEquals(5, top2.get(0).getCount(), "Should have 5 blocks after 5 increments");
    }
    
    @Test
    void testCachePerformance() {
        // Add many players
        for (int i = 0; i < 100; i++) {
            UUID playerId = UUID.randomUUID();
            statsManager.getOrCreate(playerId).setTotalBlocks(i + 1);
        }
        
        // First call - builds cache (slower)
        long start1 = System.nanoTime();
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        long time1 = System.nanoTime() - start1;
        
        // Second call - uses cache (should be much faster)
        long start2 = System.nanoTime();
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        long time2 = System.nanoTime() - start2;
        
        assertEquals(10, top1.size());
        assertEquals(10, top2.size());
        
        // Cache hit should be significantly faster (at least 2x)
        assertTrue(time2 < time1, 
            String.format("Cache hit (%d ns) should be faster than cache miss (%d ns)", time2, time1));
    }
    
    @Test
    void testDifferentLimitsUseSameCache() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setTotalBlocks(100);
        statsManager.getOrCreate(player2).setTotalBlocks(200);
        statsManager.getOrCreate(player3).setTotalBlocks(300);
        
        // Build cache with limit 10
        List<LeaderboardEntry> top10 = leaderboard.getTopTotal(10);
        assertEquals(3, top10.size());
        
        // Get with limit 2 - should use same cache
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(2);
        assertEquals(2, top2.size());
        assertEquals(player3, top2.get(0).getPlayerId(), "Should have highest player");
        assertEquals(player2, top2.get(1).getPlayerId(), "Should have second highest player");
    }
}
