package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Mines registry.
 */
@Disabled("WorldGuard dependency not available in unit tests")
class MinesTest {

    @TempDir
    Path tempDir;

    private Mines mines;
    private LoMinesPlugin plugin;
    private BukkitScheduler scheduler;
    private MockedStatic<Bukkit> globalBukkit;

    @BeforeEach
    void setUp() {
        globalBukkit = mockStatic(Bukkit.class);

        PluginManager pluginManager = mock(PluginManager.class);
        Server server = mock(Server.class);
        World world = mock(World.class);
        BlockData blockData = mock(BlockData.class);

        globalBukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);
        globalBukkit.when(Bukkit::getServer).thenReturn(server);
        globalBukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
        globalBukkit.when(() -> Bukkit.createBlockData(any(Material.class))).thenReturn(blockData);
        when(world.getName()).thenReturn("world");

        plugin = mock(LoMinesPlugin.class);
        Logger logger = mock(Logger.class);
        scheduler = mock(BukkitScheduler.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(logger);

        globalBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        BukkitTask task = mock(BukkitTask.class);
        when(scheduler.runTaskTimer(any(), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);

        mines = new Mines(plugin);
    }

    @AfterEach
    void tearDown() {
        if (globalBukkit != null) {
            globalBukkit.close();
        }
    }

    @Test
    void testLoadAllWithNoMinesFolder() throws IOException {
        assertDoesNotThrow(() -> mines.loadAll());
        assertEquals(0, mines.getAll().size());
    }

    @Test
    void testLoadAllWithEmptyMinesFolder() throws IOException {
        Files.createDirectories(tempDir.resolve("mines"));
        mines.loadAll();
        assertEquals(0, mines.getAll().size());
    }

    @Test
    void testLoadAllWithInvalidMine() throws IOException {
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);
        Files.writeString(minesFolder.resolve("invalidmine.yml"), "selection.1: world;0;64;0;0.0;0.0\n");

        assertDoesNotThrow(() -> mines.loadAll());
        assertEquals(0, mines.getAll().size());
        verify(plugin.getLogger(), atLeastOnce()).severe(anyString());
    }
}
