package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.model.MineConfigDefaults;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("Paper API RegistryAccess not available in unit tests")
class MineConfigTest {

    @Test
    void testBuilder() {
        try (MockedStatic<org.bukkit.Bukkit> bukkit = Mockito.mockStatic(org.bukkit.Bukkit.class)) {
            World world = mock(World.class);
            when(world.getName()).thenReturn("world");
            bukkit.when(() -> org.bukkit.Bukkit.getWorld("world")).thenReturn(world);

            Location loc1 = new Location(world, 0, 64, 0);
            Location loc2 = new Location(world, 10, 74, 10);
            RegionConfig region = RegionConfig.fromSelections(List.of(loc1, loc2));

            Map<BlockKey, Double> weights = new HashMap<>();
            weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);
            BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

            MineConfig config = MineConfig.builder("testmine")
                    .region(region)
                    .blocks(blocks)
                    .reset(ResetConfig.defaults())
                    .rewards(RewardConfig.empty())
                    .teleport(TeleportConfig.disabled())
                    .ui(UIConfig.defaults())
                    .build();

            assertEquals("testmine", config.name());
            assertEquals(1, config.region().regionCount());
            assertEquals(1, config.blocks().blockCount());
        }
    }

    @Test
    void testDefaultsFactory() {
        try (MockedStatic<org.bukkit.Bukkit> bukkit = Mockito.mockStatic(org.bukkit.Bukkit.class)) {
            World world = mock(World.class);
            when(world.getName()).thenReturn("world");
            bukkit.when(() -> org.bukkit.Bukkit.getWorld("world")).thenReturn(world);

            Location loc1 = new Location(world, 0, 64, 0);
            Location loc2 = new Location(world, 10, 74, 10);
            RegionConfig region = RegionConfig.fromSelections(List.of(loc1, loc2));

            Map<BlockKey, Double> weights = new HashMap<>();
            weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);
            BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

            MineConfig config = MineConfigDefaults.defaults("mymine", region, blocks);

            assertEquals("mymine", config.name());
            assertNotNull(config.reset());
            assertNotNull(config.rewards());
            assertNotNull(config.teleport());
            assertNotNull(config.ui());
        }
    }

    @Test
    void testNameNormalized() {
        try (MockedStatic<org.bukkit.Bukkit> bukkit = Mockito.mockStatic(org.bukkit.Bukkit.class)) {
            World world = mock(World.class);
            when(world.getName()).thenReturn("world");
            bukkit.when(() -> org.bukkit.Bukkit.getWorld("world")).thenReturn(world);

            Location loc1 = new Location(world, 0, 64, 0);
            Location loc2 = new Location(world, 10, 74, 10);
            RegionConfig region = RegionConfig.fromSelections(List.of(loc1, loc2));

            Map<BlockKey, Double> weights = new HashMap<>();
            weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);
            BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

            MineConfig config = MineConfig.builder("TestMine_123 ")
                    .region(region)
                    .blocks(blocks)
                    .build();

            assertEquals("testmine_123", config.name());
        }
    }

    @Test
    void testWorldNameDelegation() {
        try (MockedStatic<org.bukkit.Bukkit> bukkit = Mockito.mockStatic(org.bukkit.Bukkit.class)) {
            World world = mock(World.class);
            when(world.getName()).thenReturn("nether");
            bukkit.when(() -> org.bukkit.Bukkit.getWorld("nether")).thenReturn(world);

            Location loc1 = new Location(world, 0, 64, 0);
            Location loc2 = new Location(world, 10, 74, 10);
            RegionConfig region = RegionConfig.fromSelections(List.of(loc1, loc2));

            Map<BlockKey, Double> weights = new HashMap<>();
            weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);
            BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

            MineConfig config = MineConfig.builder("test")
                    .region(region)
                    .blocks(blocks)
                    .build();

            assertEquals("nether", config.worldName());
        }
    }
}
