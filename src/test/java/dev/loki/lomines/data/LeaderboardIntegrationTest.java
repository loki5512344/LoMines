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

class LeaderboardIntegrationTest {

    private StatsManager statsManager;
    private Leaderboard leaderboard;

    @BeforeEach
    void setUp() {
        LoMinesPlugin mockPlugin = mock(LoMinesPlugin.class);
        File mockDataFolder = new File(System.getProperty("java.io.tmpdir"), "lomines-test");
        when(mockPlugin.getDataFolder()).thenReturn(mockDataFolder);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));

        statsManager = new StatsManager(mockPlugin);
        leaderboard = statsManager.getLeaderboard();
    }

    @Test
    void testCachePerformance() {
        for (int i = 0; i < 100; i++) {
            UUID playerId = UUID.randomUUID();
            statsManager.getOrCreate(playerId).setTotalBlocks(i + 1);
        }

        long start1 = System.nanoTime();
        List<LeaderboardEntry> top1 = leaderboard.getTopTotal(10);
        long time1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(10);
        long time2 = System.nanoTime() - start2;

        assertEquals(10, top1.size());
        assertEquals(10, top2.size());

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

        List<LeaderboardEntry> top10 = leaderboard.getTopTotal(10);
        assertEquals(3, top10.size());

        List<LeaderboardEntry> top2 = leaderboard.getTopTotal(2);
        assertEquals(2, top2.size());
        assertEquals(player3, top2.get(0).playerId(), "Should have highest player");
        assertEquals(player2, top2.get(1).playerId(), "Should have second highest player");
    }
}
