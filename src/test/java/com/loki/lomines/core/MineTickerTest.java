package com.loki.lomines.core;

import com.loki.lomines.LoMinesPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MineTicker class.
 * Note: Full integration testing requires a running Bukkit server.
 * These tests verify basic construction and null safety.
 */
class MineTickerTest {
    
    @Mock
    private LoMinesPlugin plugin;
    
    @Mock
    private Mines mines;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testConstruction_WithValidParameters_DoesNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> new MineTicker(mines, plugin));
    }
    
    @Test
    void testStop_WhenNotRunning_DoesNotThrow() {
        // Arrange
        MineTicker ticker = new MineTicker(mines, plugin);
        
        // Act & Assert
        assertDoesNotThrow(() -> ticker.stop());
    }
    
    @Test
    void testMultipleStops_DoNotThrow() {
        // Arrange
        MineTicker ticker = new MineTicker(mines, plugin);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ticker.stop();
            ticker.stop();
            ticker.stop();
        });
    }
}
