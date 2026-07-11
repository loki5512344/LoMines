package dev.loki.lomines.data;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.PlayerStats;
import dev.loki.lomines.data.stats.service.StatsManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StatsManager.
 * Tests loading, saving, and auto-saving of player statistics.
 */
class StatsManagerTest {

    @TempDir
    Path tempDir;
    private StatsManager statsManager;
    private LoMinesPlugin plugin;


    @BeforeEach
    void setUp() {
        plugin = mock(LoMinesPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        statsManager = new StatsManager(plugin);
    }

    @Test
    void testGetOrCreate() {
        UUID playerId = UUID.randomUUID();

        PlayerStats stats1 = statsManager.getOrCreate(playerId);
        assertNotNull(stats1);
        assertEquals(playerId, stats1.getPlayerId());

        // Should return the same instance
        PlayerStats stats2 = statsManager.getOrCreate(playerId);
        assertSame(stats1, stats2);
    }

    @Test
    void testIncrementBlocks() {
        UUID playerId = UUID.randomUUID();
        String mineName = "TestMine";

        statsManager.incrementBlocks(playerId, mineName);

        PlayerStats stats = statsManager.getOrCreate(playerId);
        assertEquals(1, stats.getTotalBlocks());
        assertEquals(1, stats.getMineBlocks(mineName));

        // Increment again
        statsManager.incrementBlocks(playerId, mineName);
        assertEquals(2, stats.getTotalBlocks());
        assertEquals(2, stats.getMineBlocks(mineName));
    }

    @Test
    void testIncrementBlocksMultipleMines() {
        UUID playerId = UUID.randomUUID();

        statsManager.incrementBlocks(playerId, "Mine1");
        statsManager.incrementBlocks(playerId, "Mine2");
        statsManager.incrementBlocks(playerId, "Mine1");

        PlayerStats stats = statsManager.getOrCreate(playerId);
        assertEquals(3, stats.getTotalBlocks());
        assertEquals(2, stats.getMineBlocks("Mine1"));
        assertEquals(1, stats.getMineBlocks("Mine2"));
    }

    @Test
    void testSaveEmptyStats() throws IOException {
        statsManager.save();

        Path statsFile = tempDir.resolve("stats.yml");
        assertTrue(Files.exists(statsFile));

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statsFile.toFile());
        assertNull(yaml.getConfigurationSection("players"));
    }

    @Test
    void testSaveWithStats() throws IOException {
        UUID playerId = UUID.randomUUID();
        statsManager.incrementBlocks(playerId, "TestMine");
        statsManager.incrementBlocks(playerId, "TestMine");
        statsManager.incrementBlocks(playerId, "AnotherMine");

        statsManager.save();

        Path statsFile = tempDir.resolve("stats.yml");
        assertTrue(Files.exists(statsFile));

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statsFile.toFile());
        assertNotNull(yaml.getConfigurationSection("players"));

        String playerPath = "players." + playerId;
        assertEquals(3, yaml.getLong(playerPath + ".total"));
        assertEquals(2, yaml.getLong(playerPath + ".mines.testmine"));
        assertEquals(1, yaml.getLong(playerPath + ".mines.anothermine"));
    }
}
