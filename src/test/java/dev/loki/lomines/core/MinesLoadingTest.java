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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("WorldGuard dependency not available in unit tests")
class MinesLoadingTest {

    private static final String FULL_YAML = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              stone: 100
            reset:
              ticks: 6000
              percent: 10.0
            reset-on-percent: false
            actionbar:
              enabled: true
              message: "Test mine"
              range: 50.0
            timer-format: "mm:ss"
            teleport-on-reset: false
            reset-commands: []
            broadcast-reset: ""
            random-rewards: []
            """;

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
    void testLoadAllWithValidMine() throws IOException {
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);
        Files.writeString(minesFolder.resolve("testmine.yml"), FULL_YAML);

        mines.loadAll();

        assertEquals(1, mines.getAll().size());
        assertTrue(mines.find("testmine").isPresent());
        Mine mine = mines.get("testmine");
        assertNotNull(mine);
        assertEquals("testmine", mine.getName());
    }

    @Test
    void testLoadAllWithMultipleMines() throws IOException {
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);
        Files.writeString(minesFolder.resolve("mine1.yml"), FULL_YAML);
        Files.writeString(minesFolder.resolve("mine2.yml"), FULL_YAML);
        Files.writeString(minesFolder.resolve("mine3.yml"), FULL_YAML);

        mines.loadAll();

        assertEquals(3, mines.getAll().size());
        assertTrue(mines.find("mine1").isPresent());
        assertTrue(mines.find("mine2").isPresent());
        assertTrue(mines.find("mine3").isPresent());
    }

    @Test
    void testLoadAllIgnoresNonYamlFiles() throws IOException {
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);
        Files.writeString(minesFolder.resolve("validmine.yml"), FULL_YAML);
        Files.writeString(minesFolder.resolve("readme.txt"), "not a mine config");
        Files.writeString(minesFolder.resolve("backup.bak"), "backup file");

        mines.loadAll();

        assertEquals(1, mines.getAll().size());
        assertTrue(mines.find("validmine").isPresent());
    }
}
