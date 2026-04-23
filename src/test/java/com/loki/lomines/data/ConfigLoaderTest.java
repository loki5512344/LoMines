package com.loki.lomines.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigLoader.
 * Tests parsing of YAML configuration files into MineConfig objects.
 */
class ConfigLoaderTest {
    
    private ConfigLoader configLoader;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        configLoader = new ConfigLoader();
    }
    
    @Test
    void testLoadValidConfig() throws Exception {
        // Create a valid YAML config file
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              stone: 50
              coal_ore: 30
              iron_ore: 20
            reset:
              ticks: 6000
              percent: 10.0
            reset-on-percent: false
            actionbar:
              enabled: true
              message: "Mine resetting soon"
              range: 50.0
            timer-format: "mm:ss"
            teleport-on-reset: false
            reset-commands:
              - "say Mine reset!"
            broadcast-reset: "Mine has been reset"
            random-rewards:
              - chance: 1.0
                prevent-drops: false
                blocks:
                  - diamond_ore
                items:
                  - type: diamond
                    amount: 1
                commands:
                  - "eco give %player% 100"
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        // Mock Bukkit
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            // Load the config
            MineConfig config = configLoader.load(configFile);
            
            // Verify selections
            assertNotNull(config.getSelections());
            assertEquals(2, config.getSelections().size());
            
            // Verify blocks
            assertNotNull(config.getBlocks());
            assertEquals(3, config.getBlocks().size());
            assertTrue(config.getBlocks().containsKey("stone"));
            assertTrue(config.getBlocks().containsKey("coal_ore"));
            assertTrue(config.getBlocks().containsKey("iron_ore"));
            assertEquals(50.0, config.getBlocks().get("stone"));
            assertEquals(30.0, config.getBlocks().get("coal_ore"));
            assertEquals(20.0, config.getBlocks().get("iron_ore"));
            
            // Verify rewards
            assertNotNull(config.getRewards());
            assertEquals(1, config.getRewards().size());
            Reward reward = config.getRewards().get(0);
            assertEquals(1.0, reward.getChance());
            assertFalse(reward.isPreventDrops());
            assertEquals(1, reward.getMaterials().size());
            assertTrue(reward.getMaterials().contains(Material.DIAMOND_ORE));
            
            // Verify reset settings
            assertEquals(6000, config.getResetTicks());
            assertEquals(10.0, config.getResetPercent());
            assertFalse(config.isResetOnPercentEnabled());
            
            // Verify action bar settings
            assertTrue(config.isActionBarEnabled());
            assertEquals("Mine resetting soon", config.getActionBarMessage());
            assertEquals(50.0, config.getActionBarRange());
            
            // Verify other settings
            assertEquals("mm:ss", config.getTimerFormat());
            assertFalse(config.isTeleportOnReset());
            assertNull(config.getTeleportLocation());
            assertEquals(1, config.getResetCommands().size());
            assertEquals("say Mine reset!", config.getResetCommands().get(0));
            assertEquals("Mine has been reset", config.getBroadcastReset());
        }
    }
    
    @Test
    void testLoadMissingFile() {
        Path nonExistentFile = tempDir.resolve("nonexistent.yml");
        
        assertThrows(IOException.class, () -> {
            configLoader.load(nonExistentFile);
        });
    }
    
    @Test
    void testLoadNullPath() {
        ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
            configLoader.load(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }
    
    @Test
    void testLoadMissingSelections() throws Exception {
        String yamlContent = """
            contents:
              stone: 100
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
            configLoader.load(configFile);
        });
        
        assertTrue(exception.getMessage().contains("No selection coordinates found"));
    }
    
    @Test
    void testLoadOddNumberOfSelections() throws Exception {
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            selection.3: world;20;64;20;0.0;0.0
            contents:
              stone: 100
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
                configLoader.load(configFile);
            });
            
            assertTrue(exception.getMessage().contains("must be in pairs"));
        }
    }

    @Test
    void testLoadMissingContents() throws Exception {
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
                configLoader.load(configFile);
            });
            
            assertTrue(exception.getMessage().contains("Missing required 'contents' section"));
        }
    }
    
    @Test
    void testLoadInvalidMaterial() throws Exception {
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              invalid_material: 100
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
                configLoader.load(configFile);
            });
            
            assertTrue(exception.getMessage().contains("Unknown material"));
        }
    }
    
    @Test
    void testLoadInvalidBlockWeight() throws Exception {
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              stone: -10
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
                configLoader.load(configFile);
            });
            
            assertTrue(exception.getMessage().contains("must be positive"));
        }
    }
    
    @Test
    void testLoadInvalidRewardChance() throws Exception {
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              stone: 100
            random-rewards:
              - chance: 150
                blocks:
                  - diamond_ore
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            ConfigParseException exception = assertThrows(ConfigParseException.class, () -> {
                configLoader.load(configFile);
            });
            
            assertTrue(exception.getMessage().contains("between 0 and 100"));
        }
    }
    
    @Test
    void testLoadWithTeleportLocation() throws Exception {
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              stone: 100
            teleport-on-reset: true
            teleport-location: world;5;70;5;90.0;0.0
            """;
        
        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);
        
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);
            
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
            
            when(world.getName()).thenReturn("world");
            
            MineConfig config = configLoader.load(configFile);
            
            assertTrue(config.isTeleportOnReset());
            assertNotNull(config.getTeleportLocation());
            assertEquals(5.0, config.getTeleportLocation().getX());
            assertEquals(70.0, config.getTeleportLocation().getY());
            assertEquals(5.0, config.getTeleportLocation().getZ());
        }
    }


    @Test
    void testSaveAndLoadRoundTrip() throws Exception {
        // Create a config file to load (without items to avoid ItemStack initialization issues)
        String yamlContent = """
            selection.1: world;0;64;0;0.0;0.0
            selection.2: world;10;74;10;0.0;0.0
            contents:
              stone: 50
              coal_ore: 30
            reset:
              ticks: 6000
              percent: 10.0
            reset-on-percent: false
            actionbar:
              enabled: true
              message: "Mine resetting soon"
              range: 50.0
            timer-format: "mm:ss"
            teleport-on-reset: false
            reset-commands:
              - "say Mine reset!"
            broadcast-reset: "Mine has been reset"
            random-rewards:
              - chance: 1.0
                prevent-drops: false
                blocks:
                  - diamond_ore
                commands:
                  - "eco give %player% 100"
            """;

        Path configFile = tempDir.resolve("test-mine.yml");
        Files.writeString(configFile, yamlContent);

        // Mock Bukkit
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);

            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);

            when(world.getName()).thenReturn("world");

            // Load the config
            MineConfig config = configLoader.load(configFile);

            // Save to a new file
            Path savedFile = tempDir.resolve("saved-mine.yml");
            configLoader.save(config, savedFile);

            // Verify the file was created
            assertTrue(Files.exists(savedFile));

            // Verify the content is valid YAML
            String savedContent = Files.readString(savedFile);
            assertFalse(savedContent.isEmpty());

            // Load the saved config
            MineConfig loadedConfig = configLoader.load(savedFile);

            // Verify key properties match
            assertEquals(config.getResetTicks(), loadedConfig.getResetTicks());
            assertEquals(config.getResetPercent(), loadedConfig.getResetPercent());
            assertEquals(config.isResetOnPercentEnabled(), loadedConfig.isResetOnPercentEnabled());
            assertEquals(config.isActionBarEnabled(), loadedConfig.isActionBarEnabled());
            assertEquals(config.getActionBarMessage(), loadedConfig.getActionBarMessage());
            assertEquals(config.getActionBarRange(), loadedConfig.getActionBarRange());
            assertEquals(config.getTimerFormat(), loadedConfig.getTimerFormat());
            assertEquals(config.isTeleportOnReset(), loadedConfig.isTeleportOnReset());
            assertEquals(config.getBroadcastReset(), loadedConfig.getBroadcastReset());
            assertEquals(config.getResetCommands().size(), loadedConfig.getResetCommands().size());
            assertEquals(config.getRewards().size(), loadedConfig.getRewards().size());
        }
    }

    @Test
    void testSaveNullConfig() {
        Path configFile = tempDir.resolve("test.yml");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            configLoader.save(null, configFile);
        });

        assertTrue(exception.getMessage().contains("Config cannot be null"));
    }

    @Test
    void testSaveNullPath() throws Exception {
        // Create a minimal config
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);

            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);

            when(world.getName()).thenReturn("world");

            Location loc1 = new Location(world, 0, 64, 0, 0, 0);
            Location loc2 = new Location(world, 10, 74, 10, 0, 0);

            MineConfig config = MineConfig.builder()
                .selections(List.of(loc1, loc2))
                .blocks(Map.of("stone", 100.0))
                .rewards(List.of())
                .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                configLoader.save(config, null);
            });

            assertTrue(exception.getMessage().contains("Config file path cannot be null"));
        }
    }

    @Test
    void testSaveWithTeleportLocation() throws Exception {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);

            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);

            when(world.getName()).thenReturn("world");

            Location loc1 = new Location(world, 0, 64, 0, 0, 0);
            Location loc2 = new Location(world, 10, 74, 10, 0, 0);
            Location teleportLoc = new Location(world, 5, 70, 5, 90, 0);

            MineConfig config = MineConfig.builder()
                .selections(List.of(loc1, loc2))
                .blocks(Map.of("stone", 100.0))
                .rewards(List.of())
                .teleportOnReset(true)
                .teleportLocation(teleportLoc)
                .build();

            Path savedFile = tempDir.resolve("with-teleport.yml");
            configLoader.save(config, savedFile);

            // Verify the file was created
            assertTrue(Files.exists(savedFile));

            // Load and verify
            MineConfig loadedConfig = configLoader.load(savedFile);
            assertTrue(loadedConfig.isTeleportOnReset());
            assertNotNull(loadedConfig.getTeleportLocation());
            assertEquals(5.0, loadedConfig.getTeleportLocation().getX());
            assertEquals(70.0, loadedConfig.getTeleportLocation().getY());
            assertEquals(5.0, loadedConfig.getTeleportLocation().getZ());
        }
    }

    @Test
    void testSaveEmptyRewards() throws Exception {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            Server server = mock(Server.class);
            World world = mock(World.class);

            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);

            when(world.getName()).thenReturn("world");

            Location loc1 = new Location(world, 0, 64, 0, 0, 0);
            Location loc2 = new Location(world, 10, 74, 10, 0, 0);

            MineConfig config = MineConfig.builder()
                .selections(List.of(loc1, loc2))
                .blocks(Map.of("stone", 100.0))
                .rewards(List.of())
                .build();

            Path savedFile = tempDir.resolve("no-rewards.yml");
            configLoader.save(config, savedFile);

            // Verify the file was created
            assertTrue(Files.exists(savedFile));

            // Load and verify
            MineConfig loadedConfig = configLoader.load(savedFile);
            assertTrue(loadedConfig.getRewards().isEmpty());
        }
    }

}
