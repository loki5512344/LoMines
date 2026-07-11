package dev.loki.lomines.data;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.PlayerStats;
import dev.loki.lomines.data.stats.service.StatsManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatsManagerIOTest {

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
    void testLoadNonExistentFile() throws IOException {
        statsManager.load();

        assertTrue(statsManager.getAllStats().isEmpty());
    }

    @Test
    void testLoadEmptyFile() throws IOException {
        Path statsFile = tempDir.resolve("stats.yml");
        Files.writeString(statsFile, "");

        statsManager.load();

        assertTrue(statsManager.getAllStats().isEmpty());
    }

    @Test
    void testLoadValidStats() throws IOException {
        UUID playerId = UUID.randomUUID();
        String yamlContent = String.format("""
                players:
                  %s:
                    total: 100
                    mines:
                      mine1: 60
                      mine2: 40
                """, playerId);

        Path statsFile = tempDir.resolve("stats.yml");
        Files.writeString(statsFile, yamlContent);

        statsManager.load();

        PlayerStats stats = statsManager.getOrCreate(playerId);
        assertEquals(100, stats.getTotalBlocks());
        assertEquals(60, stats.getMineBlocks("mine1"));
        assertEquals(40, stats.getMineBlocks("mine2"));
    }

    @Test
    void testLoadInvalidUUID() throws IOException {
        String yamlContent = """
                players:
                  invalid-uuid:
                    total: 100
                """;

        Path statsFile = tempDir.resolve("stats.yml");
        Files.writeString(statsFile, yamlContent);

        statsManager.load();

        assertTrue(statsManager.getAllStats().isEmpty());
    }

    @Test
    void testSaveAndLoadRoundTrip() throws IOException {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        statsManager.incrementBlocks(player1, "Mine1");
        statsManager.incrementBlocks(player1, "Mine1");
        statsManager.incrementBlocks(player1, "Mine2");
        statsManager.incrementBlocks(player2, "Mine1");

        statsManager.save();

        StatsManager newManager = new StatsManager(plugin);
        newManager.load();

        PlayerStats stats1 = newManager.getOrCreate(player1);
        assertEquals(3, stats1.getTotalBlocks());
        assertEquals(2, stats1.getMineBlocks("Mine1"));
        assertEquals(1, stats1.getMineBlocks("Mine2"));

        PlayerStats stats2 = newManager.getOrCreate(player2);
        assertEquals(1, stats2.getTotalBlocks());
        assertEquals(1, stats2.getMineBlocks("Mine1"));
    }
}
