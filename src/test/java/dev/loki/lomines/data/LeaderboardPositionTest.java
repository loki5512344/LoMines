package dev.loki.lomines.data;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.Leaderboard;
import dev.loki.lomines.data.stats.service.StatsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderboardPositionTest {

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
