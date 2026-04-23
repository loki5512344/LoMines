package com.loki.lomines.data;

import com.loki.lomines.LoMinesPlugin;
import dev.lolib.core.LoLogger;
import dev.lolib.scheduler.Scheduler;
import dev.lolib.scheduler.ScheduledTask;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StatsManager.
 * Tests loading, saving, and auto-saving of player statistics.
 */
class StatsManagerTest {
    
    private StatsManager statsManager;
    private LoMinesPlugin plugin;
    private Scheduler scheduler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        plugin = mock(LoMinesPlugin.class);
        LoLogger loLogger = mock(LoLogger.class);
        scheduler = mock(Scheduler.class);
        
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));
        when(plugin.loLogger()).thenReturn(loLogger);
        
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
        
        String playerPath = "players." + playerId.toString();
        assertEquals(3, yaml.getLong(playerPath + ".total"));
        assertEquals(2, yaml.getLong(playerPath + ".mines.testmine"));
        assertEquals(1, yaml.getLong(playerPath + ".mines.anothermine"));
    }
    
    @Test
    void testLoadNonExistentFile() throws IOException {
        // Should not throw exception, just log info
        statsManager.load();
        
        // Stats should be empty
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
            """, playerId.toString());
        
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
        
        // Should not throw exception, just log warning
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
        
        // Create new manager and load
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
    
    @Test
    void testStartAutoSave() {
        try (MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {
            ScheduledTask task = mock(ScheduledTask.class);
            
            schedulerStatic.when(() -> Scheduler.get(plugin)).thenReturn(scheduler);
            when(scheduler.runTimerAsync(any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
            
            statsManager.startAutoSave();
            
            // Verify that runTimerAsync was called with correct interval (5 minutes = 6000 ticks)
            verify(scheduler).runTimerAsync(any(Runnable.class), eq(6000L), eq(6000L));
        }
    }
    
    @Test
    void testStopAutoSave() {
        try (MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {
            ScheduledTask task = mock(ScheduledTask.class);
            
            schedulerStatic.when(() -> Scheduler.get(plugin)).thenReturn(scheduler);
            when(scheduler.runTimerAsync(any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
            
            statsManager.startAutoSave();
            statsManager.stopAutoSave();
            
            verify(task).cancel();
        }
    }
    
    @Test
    void testStopAutoSaveWithoutStart() {
        // Should not throw exception
        assertDoesNotThrow(() -> statsManager.stopAutoSave());
    }
    
    @Test
    void testGetAllStats() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        statsManager.incrementBlocks(player1, "Mine1");
        statsManager.incrementBlocks(player2, "Mine2");
        
        assertEquals(2, statsManager.getAllStats().size());
    }
    
    @Test
    void testGetLeaderboard() {
        assertNotNull(statsManager.getLeaderboard());
    }
}
