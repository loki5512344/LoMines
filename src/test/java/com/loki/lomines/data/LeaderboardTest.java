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
 * Unit tests for Leaderboard class.
 * Tests caching behavior, sorting, and filtering.
 */
class LeaderboardTest {
    
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
    void testGetTopTotal_EmptyStats() {
        List<LeaderboardEntry> top = leaderboard.getTopTotal(10);
        assertTrue(top.isEmpty(), "Top list should be empty when no stats exist");
    }
    
    @Test
    void testGetTopTotal_SinglePlayer() {
        UUID playerId = UUID.randomUUID();
        PlayerStats stats = statsManager.getOrCreate(playerId);
        stats.setTotalBlocks(100);
        
        List<LeaderboardEntry> top = leaderboard.getTopTotal(10);
        assertEquals(1, top.size());
        assertEquals(playerId, top.get(0).getPlayerId());
        assertEquals(100, top.get(0).getCount());
    }
    
    @Test
    void testGetTopTotal_SortedDescending() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setTotalBlocks(50);
        statsManager.getOrCreate(player2).setTotalBlocks(200);
        statsManager.getOrCreate(player3).setTotalBlocks(100);
        
        List<LeaderboardEntry> top = leaderboard.getTopTotal(10);
        assertEquals(3, top.size());
        assertEquals(player2, top.get(0).getPlayerId()); // 200 blocks
        assertEquals(player3, top.get(1).getPlayerId()); // 100 blocks
        assertEquals(player1, top.get(2).getPlayerId()); // 50 blocks
    }
    
    @Test
    void testGetTopTotal_LimitRespected() {
        for (int i = 0; i < 10; i++) {
            UUID playerId = UUID.randomUUID();
            statsManager.getOrCreate(playerId).setTotalBlocks(i + 1);
        }
        
        List<LeaderboardEntry> top = leaderboard.getTopTotal(5);
        assertEquals(5, top.size(), "Should respect the limit parameter");
    }
    
    @Test
    void testGetTopTotal_FiltersZeroBlocks() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setTotalBlocks(100);
        statsManager.getOrCreate(player2).setTotalBlocks(0);
        
        List<LeaderboardEntry> top = leaderboard.getTopTotal(10);
        assertEquals(1, top.size(), "Should filter out players with 0 blocks");
        assertEquals(player1, top.get(0).getPlayerId());
    }
    
    @Test
    void testGetTopTotal_CachingWorks() {
        UUID playerId = UUID.randomUUID();
        statsManager.getOrCreate(playerId).setTotalBlocks(100);
        
        // First call - builds cache
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        
        // Second call - should use cache (same reference)
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        
        assertEquals(top1.size(), top2.size());
        assertEquals(top1.get(0).getPlayerId(), top2.get(0).getPlayerId());
    }
    
    @Test
    void testInvalidateCache_ClearsCache() {
        UUID playerId = UUID.randomUUID();
        statsManager.getOrCreate(playerId).setTotalBlocks(100);
        
        // Build cache
        leaderboard.getTopTotal(10);
        
        // Invalidate cache
        leaderboard.invalidateCache();
        
        // Add new player
        UUID player2 = UUID.randomUUID();
        statsManager.getOrCreate(player2).setTotalBlocks(200);
        
        // Should rebuild cache with new player
        List<LeaderboardEntry> top = leaderboard.getTopTotal(10);
        assertEquals(2, top.size(), "Cache should be rebuilt after invalidation");
        assertEquals(player2, top.get(0).getPlayerId(), "New player should be first");
    }
    
    @Test
    void testGetTopByMine_SortedDescending() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setMineBlocks("mine1", 50);
        statsManager.getOrCreate(player2).setMineBlocks("mine1", 200);
        statsManager.getOrCreate(player3).setMineBlocks("mine1", 100);
        
        List<LeaderboardEntry> top = leaderboard.getTopByMine("mine1", 10);
        assertEquals(3, top.size());
        assertEquals(player2, top.get(0).getPlayerId()); // 200 blocks
        assertEquals(player3, top.get(1).getPlayerId()); // 100 blocks
        assertEquals(player1, top.get(2).getPlayerId()); // 50 blocks
    }
    
    @Test
    void testGetTopByMine_FiltersZeroBlocks() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setMineBlocks("mine1", 100);
        statsManager.getOrCreate(player2).setMineBlocks("mine1", 0);
        
        List<LeaderboardEntry> top = leaderboard.getTopByMine("mine1", 10);
        assertEquals(1, top.size(), "Should filter out players with 0 blocks");
        assertEquals(player1, top.get(0).getPlayerId());
    }
    
    @Test
    void testGetTopByMine_OnlyIncludesSpecificMine() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setMineBlocks("mine1", 100);
        statsManager.getOrCreate(player2).setMineBlocks("mine2", 200);
        
        List<LeaderboardEntry> top = leaderboard.getTopByMine("mine1", 10);
        assertEquals(1, top.size(), "Should only include players with blocks in specified mine");
        assertEquals(player1, top.get(0).getPlayerId());
    }
    
    @Test
    void testGetPosition_ReturnsCorrectPosition() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();
        
        statsManager.getOrCreate(player1).setTotalBlocks(50);
        statsManager.getOrCreate(player2).setTotalBlocks(200);
        statsManager.getOrCreate(player3).setTotalBlocks(100);
        
        assertEquals(1, leaderboard.getPosition(player2), "Player with most blocks should be position 1");
        assertEquals(2, leaderboard.getPosition(player3), "Player with second most blocks should be position 2");
        assertEquals(3, leaderboard.getPosition(player1), "Player with least blocks should be position 3");
    }
    
    @Test
    void testGetPosition_ReturnsMinusOneForNonExistentPlayer() {
        UUID playerId = UUID.randomUUID();
        assertEquals(-1, leaderboard.getPosition(playerId), "Should return -1 for non-existent player");
    }
    
    @Test
    void testGetPosition_ReturnsMinusOneForZeroBlocks() {
        UUID playerId = UUID.randomUUID();
        statsManager.getOrCreate(playerId).setTotalBlocks(0);
        
        assertEquals(-1, leaderboard.getPosition(playerId), "Should return -1 for player with 0 blocks");
    }
}
