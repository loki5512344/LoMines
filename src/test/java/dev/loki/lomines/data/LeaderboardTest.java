package dev.loki.lomines.data;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.Leaderboard;
import dev.loki.lomines.data.stats.model.LeaderboardEntry;
import dev.loki.lomines.data.stats.model.PlayerStats;
import dev.loki.lomines.data.stats.service.StatsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        File mockDataFolder = new File(System.getProperty("java.io.tmpdir"), "lomines-test");
        when(mockPlugin.getDataFolder()).thenReturn(mockDataFolder);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));

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
        assertEquals(playerId, top.get(0).playerId());
        assertEquals(100, top.get(0).count());
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
        assertEquals(player2, top.get(0).playerId()); // 200 blocks
        assertEquals(player3, top.get(1).playerId()); // 100 blocks
        assertEquals(player1, top.get(2).playerId()); // 50 blocks
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
        assertEquals(player1, top.get(0).playerId());
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
        assertEquals(top1.get(0).playerId(), top2.get(0).playerId());
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
        assertEquals(player2, top.get(0).playerId(), "New player should be first");
    }

}
