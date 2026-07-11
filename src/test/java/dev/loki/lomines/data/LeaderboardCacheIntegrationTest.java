package dev.loki.lomines.data;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.Leaderboard;
import dev.loki.lomines.data.stats.model.LeaderboardEntry;
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
        File mockDataFolder = new File(System.getProperty("java.io.tmpdir"), "lomines-test");
        when(mockPlugin.getDataFolder()).thenReturn(mockDataFolder);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));

        statsManager = new StatsManager(mockPlugin);
        leaderboard = statsManager.getLeaderboard();
    }

    @Test
    void testCacheInvalidatedOnIncrementBlocks() {
        UUID player1 = UUID.randomUUID();

        // Set initial stats
        statsManager.getOrCreate(player1).setTotalBlocks(100);

        // Build cache
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        assertEquals(1, top1.size(), "Should have 1 player initially");
        assertEquals(player1, top1.get(0).playerId());

        // Increment blocks for player2 - this should invalidate cache
        UUID player2 = UUID.randomUUID();
        statsManager.incrementBlocks(player2, "mine1");

        // Get top again - should rebuild cache with new player
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        assertEquals(2, top2.size(), "Should have 2 players after increment");

        // Verify order (player1 has 100, player2 has 1)
        assertEquals(player1, top2.get(0).playerId(), "Player1 should still be first");
        assertEquals(player2, top2.get(1).playerId(), "Player2 should be second");
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
        assertEquals(player2, top1.get(0).playerId(), "Player2 should be first initially");

        // Increment player1 many times to overtake player2
        for (int i = 0; i < 60; i++) {
            statsManager.incrementBlocks(player1, "mine1");
        }

        // Get top again - cache should be rebuilt with new order
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        assertEquals(player1, top2.get(0).playerId(), "Player1 should be first after increments");
        assertEquals(110, top2.get(0).count(), "Player1 should have 110 blocks");
        assertEquals(player2, top2.get(1).playerId(), "Player2 should be second");
        assertEquals(100, top2.get(1).count(), "Player2 should still have 100 blocks");
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
        assertEquals(5, top2.get(0).count(), "Should have 5 blocks after 5 increments");
    }
}
