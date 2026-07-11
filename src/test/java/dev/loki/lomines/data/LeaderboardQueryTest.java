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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderboardQueryTest {

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
    void testGetTopByMine_SortedDescending() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();

        statsManager.getOrCreate(player1).setMineBlocks("mine1", 50);
        statsManager.getOrCreate(player2).setMineBlocks("mine1", 200);
        statsManager.getOrCreate(player3).setMineBlocks("mine1", 100);

        List<LeaderboardEntry> top = leaderboard.getTopByMine("mine1", 10);
        assertEquals(3, top.size());
        assertEquals(player2, top.get(0).playerId());
        assertEquals(player3, top.get(1).playerId());
        assertEquals(player1, top.get(2).playerId());
    }

    @Test
    void testGetTopByMine_FiltersZeroBlocks() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        statsManager.getOrCreate(player1).setMineBlocks("mine1", 100);
        statsManager.getOrCreate(player2).setMineBlocks("mine1", 0);

        List<LeaderboardEntry> top = leaderboard.getTopByMine("mine1", 10);
        assertEquals(1, top.size(), "Should filter out players with 0 blocks");
        assertEquals(player1, top.get(0).playerId());
    }

    @Test
    void testGetTopByMine_OnlyIncludesSpecificMine() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        statsManager.getOrCreate(player1).setMineBlocks("mine1", 100);
        statsManager.getOrCreate(player2).setMineBlocks("mine2", 200);

        List<LeaderboardEntry> top = leaderboard.getTopByMine("mine1", 10);
        assertEquals(1, top.size(), "Should only include players with blocks in specified mine");
        assertEquals(player1, top.get(0).playerId());
    }
}
