package dev.loki.lomines.data.config.block;

import org.bukkit.Material;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Paper API RegistryAccess not available in unit tests")
class BlockConfigTest {

    @Test
    void testValidConfig() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 50.0);
        weights.put(new BlockKey.Vanilla(Material.DIRT), 30.0);
        weights.put(new BlockKey.Vanilla(Material.COAL_ORE), 20.0);

        BlockConfig config = new BlockConfig(weights, FillMode.CUBOID, null);

        assertEquals(3, config.blockCount());
        assertFalse(config.hasCustomBlocks());
        assertEquals(FillMode.CUBOID, config.fillMode());
    }

    @Test
    void testWeightsAreNormalized() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);
        weights.put(new BlockKey.Vanilla(Material.DIRT), 100.0);

        BlockConfig config = new BlockConfig(weights, FillMode.CUBOID, null);

        // Should be normalized to sum to 1.0
        double stoneWeight = config.weightFor(new BlockKey.Vanilla(Material.STONE));
        double dirtWeight = config.weightFor(new BlockKey.Vanilla(Material.DIRT));

        assertEquals(0.5, stoneWeight, 0.001);
        assertEquals(0.5, dirtWeight, 0.001);
    }

    @Test
    void testEmptyConfigThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new BlockConfig(Map.of(), FillMode.CUBOID, null));
    }

    @Test
    void testNegativeWeightThrows() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), -10.0);

        assertThrows(IllegalArgumentException.class, () ->
                new BlockConfig(weights, FillMode.CUBOID, null));
    }

    @Test
    void testZeroWeightThrows() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 0.0);

        assertThrows(IllegalArgumentException.class, () ->
                new BlockConfig(weights, FillMode.CUBOID, null));
    }

    @Test
    void testNullWeightThrows() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), null);

        assertThrows(IllegalArgumentException.class, () ->
                new BlockConfig(weights, FillMode.CUBOID, null));
    }

    @Test
    void testNullBlockKeyThrows() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(null, 50.0);

        assertThrows(NullPointerException.class, () ->
                new BlockConfig(weights, FillMode.CUBOID, null));
    }

    @Test
    void testCustomBlocksDetected() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 50.0);
        weights.put(new BlockKey.Oraxen("ruby_ore"), 50.0);

        BlockConfig config = new BlockConfig(weights, FillMode.CUBOID, null);
        assertTrue(config.hasCustomBlocks());
    }

    @Test
    void testVanillaOnlyFactory() {
        Map<Material, Double> materials = new HashMap<>();
        materials.put(Material.STONE, 60.0);
        materials.put(Material.DIRT, 40.0);

        BlockConfig config = BlockConfig.vanilla(materials);

        assertEquals(2, config.blockCount());
        assertFalse(config.hasCustomBlocks());
    }

    @Test
    void testImmutableWeights() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);

        BlockConfig config = new BlockConfig(weights, FillMode.CUBOID, null);

        // Try to modify the returned map
        Map<BlockKey, Double> returnedWeights = config.weights();
        assertThrows(UnsupportedOperationException.class, () ->
                returnedWeights.put(new BlockKey.Vanilla(Material.DIRT), 50.0));
    }

    @Test
    void testMaskConfig() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);

        BlockKey marker = new BlockKey.Vanilla(Material.PINK_CONCRETE);
        Map<String, Boolean> positions = new HashMap<>();
        positions.put("world;10;64;10;0;0", true);

        BlockConfig.MaskConfig mask = new BlockConfig.MaskConfig(marker, positions);
        BlockConfig config = new BlockConfig(weights, FillMode.MASK, mask);

        assertEquals(FillMode.MASK, config.fillMode());
        assertNotNull(config.mask());
    }

    @Test
    void testFillModeNullThrows() {
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 100.0);

        assertThrows(NullPointerException.class, () ->
                new BlockConfig(weights, null, null));
    }
}
