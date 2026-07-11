package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.data.config.region.RegionConfig;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MineConfigValidationTest {

    @Test
    void testNullNameThrows() {
        assertThrows(NullPointerException.class, () ->
                MineConfig.builder(null).build());
    }

    @Test
    @Disabled("Paper API RegistryAccess not available in unit tests")
    void testBlankNameThrows() {
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

            assertThrows(IllegalArgumentException.class, () ->
                    MineConfig.builder("  ")
                            .region(region)
                            .blocks(blocks)
                            .build());
        }
    }

    @Test
    @Disabled("Paper API RegistryAccess not available in unit tests")
    void testNullRegionThrows() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);
        BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

        assertThrows(NullPointerException.class, () ->
                MineConfig.builder("test")
                        .region(null)
                        .blocks(blocks)
                        .build());
    }

    @Test
    void testNullBlocksThrows() {
        try (MockedStatic<org.bukkit.Bukkit> bukkit = Mockito.mockStatic(org.bukkit.Bukkit.class)) {
            World world = mock(World.class);
            when(world.getName()).thenReturn("world");
            bukkit.when(() -> org.bukkit.Bukkit.getWorld("world")).thenReturn(world);

            Location loc1 = new Location(world, 0, 64, 0);
            Location loc2 = new Location(world, 10, 74, 10);
            RegionConfig region = RegionConfig.fromSelections(List.of(loc1, loc2));

            assertThrows(NullPointerException.class, () ->
                    MineConfig.builder("test")
                            .region(region)
                            .blocks(null)
                            .build());
        }
    }
}
