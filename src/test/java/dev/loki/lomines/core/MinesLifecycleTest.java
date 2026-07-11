package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("WorldGuard dependency not available in unit tests")
class MinesLifecycleTest {

    @TempDir
    Path tempDir;

    private Mines mines;
    private LoMinesPlugin plugin;
    private BukkitScheduler scheduler;
    private MockedStatic<Bukkit> globalBukkit;

    @BeforeEach
    void setUp() {
        globalBukkit = mockStatic(Bukkit.class);

        org.bukkit.plugin.PluginManager pluginManager = mock(org.bukkit.plugin.PluginManager.class);
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
    void testCreateNewMine() throws IOException {
        mines.create("newmine");

        assertEquals(1, mines.getAll().size());
        assertTrue(mines.find("newmine").isPresent());
        Mine mine = mines.get("newmine");
        assertNotNull(mine);
        assertEquals("newmine", mine.getName());
        assertTrue(Files.exists(tempDir.resolve("mines/newmine.yml")));
        verify(plugin.getLogger()).info("Created mine: newmine");
    }

    @Test
    void testCreateMineCreatesFolder() throws IOException {
        assertFalse(Files.exists(tempDir.resolve("mines")));
        mines.create("testmine");
        assertTrue(Files.isDirectory(tempDir.resolve("mines")));
    }

    @Test
    void testCreateDuplicateMineThrowsException() throws IOException {
        mines.create("duplicate");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mines.create("duplicate"));
        assertTrue(ex.getMessage().contains("Mine already exists"));
    }

    @Test
    void testCreateMineIsCaseInsensitive() throws IOException {
        mines.create("TestMine");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mines.create("testmine"));
        assertTrue(ex.getMessage().contains("Mine already exists"));
    }
}
