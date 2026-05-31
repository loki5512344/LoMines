package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.lolib.core.LoLogger;
import dev.lolib.scheduler.ScheduledTask;
import dev.lolib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
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
 * Tests loading and creating mines.
 */
class MinesTest {

    @TempDir
    Path tempDir;
    private Mines mines;
    private LoMinesPlugin plugin;
    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        plugin = mock(LoMinesPlugin.class);
        Logger logger = mock(Logger.class);
        LoLogger loLogger = mock(LoLogger.class);
        scheduler = mock(Scheduler.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.loLogger()).thenReturn(loLogger);

        mines = new Mines(plugin);
    }

    /**
     * Helper method to set up Bukkit and Scheduler mocking for tests that create mines.
     */
    private void setupBukkitAndScheduler(MockedStatic<Bukkit> bukkit, MockedStatic<Scheduler> schedulerStatic) {
        Server server = mock(Server.class);
        World world = mock(World.class);
        BlockData blockData = mock(BlockData.class);

        bukkit.when(Bukkit::getServer).thenReturn(server);
        bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
        bukkit.when(() -> Bukkit.createBlockData(any(Material.class))).thenReturn(blockData);

        when(world.getName()).thenReturn("world");

        schedulerStatic.when(() -> Scheduler.get(plugin)).thenReturn(scheduler);

        // Mock scheduler methods to execute callbacks immediately for testing
        ScheduledTask task = mock(ScheduledTask.class);
        when(scheduler.runTimer(any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
    }

    @Test
    void testLoadAllWithNoMinesFolder() throws IOException {
        // When mines folder doesn't exist, loadAll should not throw
        assertDoesNotThrow(() -> mines.loadAll());

        // No mines should be loaded
        assertEquals(0, mines.getAll().size());
    }

    @Test
    void testLoadAllWithEmptyMinesFolder() throws IOException {
        // Create empty mines folder
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);

        // Load all mines
        mines.loadAll();

        // No mines should be loaded
        assertEquals(0, mines.getAll().size());
    }

    @Test
    void testLoadAllWithValidMine() throws IOException {
        // Create mines folder
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);

        // Create a valid mine config file
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

        Path configFile = minesFolder.resolve("testmine.yml");
        Files.writeString(configFile, yamlContent);

        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Load all mines
            mines.loadAll();

            // Verify mine was loaded
            assertEquals(1, mines.getAll().size());
            assertTrue(mines.find("testmine").isPresent());

            Mine mine = mines.get("testmine");
            assertNotNull(mine);
            assertEquals("testmine", mine.getName());
        }
    }

    @Test
    void testLoadAllWithMultipleMines() throws IOException {
        // Create mines folder
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);

        // Create multiple mine config files
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

        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Load all mines
            mines.loadAll();

            // Verify all mines were loaded
            assertEquals(3, mines.getAll().size());
            assertTrue(mines.find("mine1").isPresent());
            assertTrue(mines.find("mine2").isPresent());
            assertTrue(mines.find("mine3").isPresent());
        }
    }

    @Test
    void testLoadAllIgnoresNonYamlFiles() throws IOException {
        // Create mines folder
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);

        // Create a valid mine config file
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
        Files.writeString(minesFolder.resolve("readme.txt"), "This is not a mine config");
        Files.writeString(minesFolder.resolve("backup.bak"), "Backup file");

        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Load all mines
            mines.loadAll();

            // Only the .yml file should be loaded
            assertEquals(1, mines.getAll().size());
            assertTrue(mines.find("validmine").isPresent());
        }
    }

    @Test
    void testLoadAllWithInvalidMine() throws IOException {
        // Create mines folder
        Path minesFolder = tempDir.resolve("mines");
        Files.createDirectories(minesFolder);

        // Create an invalid mine config file (missing required fields)
        String invalidYaml = """
                selection.1: world;0;64;0;0.0;0.0
                """;

        Files.writeString(minesFolder.resolve("invalidmine.yml"), invalidYaml);

        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Load all mines - should not throw, but should log error
            assertDoesNotThrow(() -> mines.loadAll());

            // Invalid mine should not be loaded
            assertEquals(0, mines.getAll().size());

            // Verify error was logged
            verify(plugin.loLogger(), atLeastOnce()).error(anyString());
        }
    }

    @Test
    void testCreateNewMine() throws IOException {
        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Create a new mine
            mines.create("newmine");

            // Verify mine was created
            assertEquals(1, mines.getAll().size());
            assertTrue(mines.find("newmine").isPresent());

            Mine mine = mines.get("newmine");
            assertNotNull(mine);
            assertEquals("newmine", mine.getName());

            // Verify config file was created
            Path configFile = tempDir.resolve("mines/newmine.yml");
            assertTrue(Files.exists(configFile));

            // Verify logger was called
            verify(plugin.loLogger()).info("Created mine: newmine");
        }
    }

    @Test
    void testCreateMineCreatesFolder() throws IOException {
        // Ensure mines folder doesn't exist
        Path minesFolder = tempDir.resolve("mines");
        assertFalse(Files.exists(minesFolder));

        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Create a new mine
            mines.create("testmine");

            // Verify mines folder was created
            assertTrue(Files.exists(minesFolder));
            assertTrue(Files.isDirectory(minesFolder));
        }
    }

    @Test
    void testCreateDuplicateMineThrowsException() throws IOException {
        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Create a mine
            mines.create("duplicate");

            // Try to create the same mine again
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                mines.create("duplicate");
            });

            assertTrue(exception.getMessage().contains("Mine already exists"));
        }
    }

    @Test
    void testCreateMineIsCaseInsensitive() throws IOException {
        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Create a mine with mixed case
            mines.create("TestMine");

            // Try to create the same mine with different case
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                mines.create("testmine");
            });

            assertTrue(exception.getMessage().contains("Mine already exists"));
        }
    }

    @Test
    void testGetMineByName() throws IOException {
        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Create a mine
            mines.create("testmine");

            // Get mine by exact name
            Mine mine = mines.get("testmine");
            assertNotNull(mine);
            assertEquals("testmine", mine.getName());

            // Get mine by different case
            Mine mine2 = mines.get("TestMine");
            assertNotNull(mine2);
            assertEquals("testmine", mine2.getName());

            // Same mine instance
            assertSame(mine, mine2);
        }
    }

    @Test
    void testGetNonExistentMineThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mines.get("nonexistent");
        });

        assertTrue(exception.getMessage().contains("Mine not found"));
    }

    @Test
    void testFindMineReturnsOptional() throws IOException {
        // Mock Bukkit and Scheduler
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Scheduler> schedulerStatic = mockStatic(Scheduler.class)) {

            setupBukkitAndScheduler(bukkit, schedulerStatic);

            // Create a mine
            mines.create("testmine");

            // Find existing mine
            assertTrue(mines.find("testmine").isPresent());

            // Find non-existent mine
            assertFalse(mines.find("nonexistent").isPresent());
        }
    }
}
