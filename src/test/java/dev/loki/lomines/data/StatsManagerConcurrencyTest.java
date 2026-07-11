package dev.loki.lomines.data;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.stats.model.PlayerStats;
import dev.loki.lomines.data.stats.service.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsManagerConcurrencyTest {

    @TempDir
    Path tempDir;
    private StatsManager statsManager;
    private LoMinesPlugin plugin;
    private BukkitScheduler scheduler;

    @BeforeEach
    void setUp() {
        plugin = mock(LoMinesPlugin.class);
        scheduler = mock(BukkitScheduler.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        statsManager = new StatsManager(plugin);
    }

    @Test
    void testStartAutoSave() {
        try (MockedStatic<Bukkit> schedulerStatic = mockStatic(Bukkit.class)) {
            BukkitTask task = mock(BukkitTask.class);

            schedulerStatic.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.runTaskTimerAsynchronously(any(), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);

            statsManager.startAutoSave();

            verify(scheduler).runTaskTimerAsynchronously(any(), any(Runnable.class), eq(6000L), eq(6000L));
        }
    }

    @Test
    void testStopAutoSave() {
        try (MockedStatic<Bukkit> schedulerStatic = mockStatic(Bukkit.class)) {
            BukkitTask task = mock(BukkitTask.class);

            schedulerStatic.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.runTaskTimerAsynchronously(any(), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);

            statsManager.startAutoSave();
            statsManager.stopAutoSave();

            verify(task).cancel();
        }
    }

    @Test
    void testStopAutoSaveWithoutStart() {
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
