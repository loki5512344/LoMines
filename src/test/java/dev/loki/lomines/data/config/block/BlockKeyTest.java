package dev.loki.lomines.data.config.block;

import org.bukkit.Material;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("Paper API RegistryAccess not available in unit tests")
class BlockKeyTest {

    @Test
    void testVanillaSerialization() {
        BlockKey.Vanilla stone = new BlockKey.Vanilla(Material.STONE);
        assertEquals("stone", stone.serialize());

        BlockKey.Vanilla diamondOre = new BlockKey.Vanilla(Material.DIAMOND_ORE);
        assertEquals("diamond_ore", diamondOre.serialize());
    }

    @Test
    void testOraxenSerialization() {
        BlockKey.Oraxen custom = new BlockKey.Oraxen("ruby_ore");
        assertEquals("oraxen:ruby_ore", custom.serialize());
    }

    @Test
    void testItemsAdderSerialization() {
        BlockKey.ItemsAdder custom = new BlockKey.ItemsAdder("mythic_block");
        assertEquals("itemsadder:mythic_block", custom.serialize());
    }

    @Test
    void testDeserializeVanilla() {
        BlockKey key = BlockKey.deserialize("stone");
        assertInstanceOf(BlockKey.Vanilla.class, key);
        assertEquals(Material.STONE, ((BlockKey.Vanilla) key).material());
    }

    @Test
    void testDeserializeOraxen() {
        BlockKey key = BlockKey.deserialize("oraxen:custom_block");
        assertInstanceOf(BlockKey.Oraxen.class, key);
        assertEquals("custom_block", ((BlockKey.Oraxen) key).id());
    }

    @Test
    void testDeserializeItemsAdder() {
        BlockKey key = BlockKey.deserialize("itemsadder:mythic_ore");
        assertInstanceOf(BlockKey.ItemsAdder.class, key);
        assertEquals("mythic_ore", ((BlockKey.ItemsAdder) key).id());
    }

    @Test
    void testDeserializeNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> BlockKey.deserialize(null));
    }

    @Test
    void testDeserializeEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> BlockKey.deserialize(""));
    }

    @Test
    void testDeserializeBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> BlockKey.deserialize("   "));
    }

    @Test
    void testDeserializeUnknownVanillaThrows() {
        assertThrows(IllegalArgumentException.class, () -> BlockKey.deserialize("not_a_real_block"));
    }

    @Test
    void testVanillaNotBlockThrows() {
        assertThrows(IllegalArgumentException.class, () -> new BlockKey.Vanilla(Material.DIAMOND));
    }

    @Test
    void testOraxenNullIdThrows() {
        assertThrows(NullPointerException.class, () -> new BlockKey.Oraxen(null));
    }

    @Test
    void testOraxenBlankIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> new BlockKey.Oraxen("   "));
    }

    @Test
    void testItemsAdderEmptyIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> new BlockKey.ItemsAdder(""));
    }

    @Test
    void testCaseInsensitiveDeserialization() {
        BlockKey key1 = BlockKey.deserialize("STONE");
        BlockKey key2 = BlockKey.deserialize("stone");
        BlockKey key3 = BlockKey.deserialize("StOnE");

        assertEquals(key1.serialize(), key2.serialize());
        assertEquals(key2.serialize(), key3.serialize());
    }

    @Test
    void testEquality() {
        BlockKey a = new BlockKey.Vanilla(Material.STONE);
        BlockKey b = new BlockKey.Vanilla(Material.STONE);
        BlockKey c = new BlockKey.Vanilla(Material.DIRT);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
