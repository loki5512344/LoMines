package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.core.mine.Mines;
import dev.lolib.core.LoLogger;
import dev.lolib.scheduler.ScheduledTask;
import dev.lolib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Mines registry.
 */
class MinesTest {

    @TempDir
    Path tempDir;

    private Mines mines;
    private LoMinesPlugin plugin;
    private Scheduler scheduler;
    // Keep Bukkit mock open for the whole test — WorldGuardRegionService calls
    // Bukkit.getPluginManager() in the Mines constructor.
    private MockedStatic<Bukkit> globalBukkit;
    private MockedStatic<Scheduler> globalScheduler;

    @BeforeEach
    void setUp() {
        globalBukkit = mockStatic(Bukkit.class);
        globalScheduler = mockStatic(Scheduler.class);

        PluginManager pluginManager = mock(org.bukkit.plugin.PluginManager.class);
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
        LoLogger loLogger = mock(LoLogger.class);
        scheduler = mock(Scheduler.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.loLogger()).thenReturn(loLogger);

        globalScheduler.when(() -> Scheduler.get(plugin)).thenReturn(scheduler);
        ScheduledTask task = mock(ScheduledTask.class);
        when(scheduler.runTimer(any(Runnable.class), anyLong(), anyLong())).thenReturn(task);

        mines = new Mines(plugin);
    }

    @AfterEach
    void tearDown() {
        if (globalScheduler != null) globalScheduler.close();
        if (globalBukkit != null) globalBukkit.close();
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
    void testLoadAllWithValidMine() throws IOException {
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);

        String yamlContent = """
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
        Files.writeString(minesFolder.resolve("testmine.yml"), yamlContent);

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

        String yamlContent = """
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
        Files.writeString(minesFolder.resolve("mine1.yml"), yamlContent);
        Files.writeString(minesFolder.resolve("mine2.yml"), yamlContent);
        Files.writeString(minesFolder.resolve("mine3.yml"), yamlContent);

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

        String yamlContent = """
                selection.1: world;0;64;0;0.0;0.0
                selection.2: world;10;74;10;0.0;0.0
                contents:
                  stone: 100
                reset:
                  ticks: 6000
                  percent: 10.0
                """;
        Files.writeString(minesFolder.resolve("validmine.yml"), yamlContent);
        Files.writeString(minesFolder.resolve("readme.txt"), "not a mine config");
        Files.writeString(minesFolder.resolve("backup.bak"), "backup file");

        mines.loadAll();

        assertEquals(1, mines.getAll().size());
        assertTrue(mines.find("validmine").isPresent());
    }

    @Test
    void testLoadAllWithInvalidMine() throws IOException {
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);
        Files.writeString(minesFolder.resolve("invalidmine.yml"), "selection.1: world;0;64;0;0.0;0.0\n");

        assertDoesNotThrow(() -> mines.loadAll());
        assertEquals(0, mines.getAll().size());
        verify(plugin.loLogger(), atLeastOnce()).error(anyString());
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
        verify(plugin.loLogger()).info("Created mine: newmine");
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

    @Test
    void testGetMineByName() throws IOException {
        mines.create("testmine");

        Mine mine = mines.get("testmine");
        assertNotNull(mine);
        assertEquals("testmine", mine.getName());

        Mine mine2 = mines.get("TestMine");
        assertNotNull(mine2);
        assertSame(mine, mine2);
    }

    @Test
    void testGetNonExistentMineThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mines.get("nonexistent"));
        assertTrue(ex.getMessage().contains("Mine not found"));
    }

    @Test
    void testFindMineReturnsOptional() throws IOException {
        mines.create("testmine");
        assertTrue(mines.find("testmine").isPresent());
        assertFalse(mines.find("nonexistent").isPresent());
    }
}
