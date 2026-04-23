package com.loki.lomines.core;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.block.BukkitBlockSetter;
import com.loki.lomines.block.ItemsAdderBlockSetter;
import com.loki.lomines.block.OraxenBlockSetter;
import com.loki.lomines.data.MineConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
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
    }
    
    @Test
    void testCreateBlockSetter_WithOraxenPrefix_CreatesOraxenBlockSetter() {
        // Arrange
        Map<String, Double> blocks = new HashMap<>();
        blocks.put("oraxen:custom_ore", 50.0);
        blocks.put("oraxen:rare_ore", 30.0);
        
        MineConfig config = createTestConfig(blocks);
        
        // Act
        Mine mine = new Mine("test_mine", config, plugin);
        
        // Assert
        assertNotNull(mine.getBlockSetter());
        assertTrue(mine.getBlockSetter() instanceof OraxenBlockSetter,
                "Expected OraxenBlockSetter for oraxen: prefix");
    }
    
    @Test
    void testCreateBlockSetter_WithItemsAdderPrefix_CreatesItemsAdderBlockSetter() {
        // Arrange
        Map<String, Double> blocks = new HashMap<>();
        blocks.put("itemsadder:custom_block", 60.0);
        blocks.put("itemsadder:special_block", 40.0);
        
        MineConfig config = createTestConfig(blocks);
        
        // Act
        Mine mine = new Mine("test_mine", config, plugin);
        
        // Assert
        assertNotNull(mine.getBlockSetter());
        assertTrue(mine.getBlockSetter() instanceof ItemsAdderBlockSetter,
                "Expected ItemsAdderBlockSetter for itemsadder: prefix");
    }
    
    @Test
    void testCreateBlockSetter_WithoutPrefix_CreatesBukkitBlockSetter() {
        // Arrange
        Map<String, Double> blocks = new HashMap<>();
        blocks.put("STONE", 50.0);
        blocks.put("COAL_ORE", 30.0);
        blocks.put("IRON_ORE", 20.0);
        
        MineConfig config = createTestConfig(blocks);
        
        // Act
        Mine mine = new Mine("test_mine", config, plugin);
        
        // Assert
        assertNotNull(mine.getBlockSetter());
        assertTrue(mine.getBlockSetter() instanceof BukkitBlockSetter,
                "Expected BukkitBlockSetter for vanilla blocks without prefix");
    }
    
    @Test
    void testCreateBlockSetter_WithEmptyBlocks_ThrowsException() {
        // Arrange
        Map<String, Double> blocks = new HashMap<>();
        MineConfig config = createTestConfig(blocks);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Mine("test_mine", config, plugin)
        );
        
        assertEquals("Mine must have at least one block type", exception.getMessage());
    }
    
    private MineConfig createTestConfig(Map<String, Double> blocks) {
        when(world.getName()).thenReturn("world");
        
        Location loc1 = new Location(world, 0, 0, 0);
        Location loc2 = new Location(world, 10, 10, 10);
        
        return MineConfig.builder()
                .selections(List.of(loc1, loc2))
                .blocks(blocks)
                .resetTicks(6000)
                .build();
    }
}
