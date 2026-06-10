package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.util.location.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Mine class, specifically testing BlockSetter creation logic.
 */
class MineTest {

    @Mock
    private LoMinesPlugin plugin;

    @Mock
    private World world;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(world.getName()).thenReturn("world");
    }

    @Test
    void testCreateBlockSetter_WithOraxenPrefix_CreatesBukkitBlockSetter() {
        // Oraxen integration is disabled — constructor throws IllegalArgumentException
        Map<BlockKey, Double> blocks = new HashMap<>();
        blocks.put(new BlockKey.Oraxen("custom_ore"), 50.0);
        blocks.put(new BlockKey.Oraxen("rare_ore"), 30.0);
        blocks.put(new BlockKey.Oraxen("other_ore"), 20.0);

        MineConfig config = createTestConfig(blocks);

        assertThrows(IllegalArgumentException.class,
                () -> new Mine("test_mine", config, plugin),
                "Expected IllegalArgumentException when Oraxen integration is disabled");
    }

    @Test
    void testCreateBlockSetter_WithItemsAdderPrefix_CreatesBukkitBlockSetter() {
        // ItemsAdder integration is disabled — constructor throws IllegalArgumentException
        Map<BlockKey, Double> blocks = new HashMap<>();
        blocks.put(new BlockKey.ItemsAdder("custom_block"), 60.0);
        blocks.put(new BlockKey.ItemsAdder("special_block"), 40.0);

        MineConfig config = createTestConfig(blocks);

        assertThrows(IllegalArgumentException.class,
                () -> new Mine("test_mine", config, plugin),
                "Expected IllegalArgumentException when ItemsAdder integration is disabled");
    }

    @Test
    void testCreateBlockSetter_WithoutPrefix_CreatesBukkitBlockSetter() {
        Map<BlockKey, Double> blocks = new HashMap<>();
        blocks.put(new BlockKey.Vanilla(Material.STONE), 50.0);
        blocks.put(new BlockKey.Vanilla(Material.COAL_ORE), 30.0);
        blocks.put(new BlockKey.Vanilla(Material.IRON_ORE), 20.0);

        MineConfig config = createTestConfig(blocks);

        Mine mine = new Mine("test_mine", config, plugin);

        assertNotNull(mine.getBlockSetter());
        assertInstanceOf(dev.loki.lomines.block.BukkitBlockSetter.class, mine.getBlockSetter(),
                "Expected BukkitBlockSetter for vanilla blocks");
    }

    @Test
    void testCreateBlockSetter_WithEmptyBlocks_ThrowsException() {
        Map<BlockKey, Double> blocks = new HashMap<>();

        assertThrows(
                IllegalArgumentException.class,
                () -> new BlockConfig(blocks, dev.loki.lomines.data.config.block.FillMode.CUBOID, null)
        );
    }

    private MineConfig createTestConfig(Map<BlockKey, Double> blocks) {
        Location loc1 = new Location(world, 0, 0, 0);
        Location loc2 = new Location(world, 10, 10, 10);

        RegionConfig region = RegionConfig.single(new Cuboid(loc1, loc2));
        BlockConfig blockConfig = new BlockConfig(blocks, dev.loki.lomines.data.config.block.FillMode.CUBOID, null);

        return MineConfig.builder("test_mine")
                .region(region)
                .blocks(blockConfig)
                .build();
    }
}
