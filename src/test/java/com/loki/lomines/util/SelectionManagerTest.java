package com.loki.lomines.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SelectionManagerTest {
    
    private SelectionManager manager;
    private UUID playerId;
    
    @BeforeEach
    void setUp() {
        manager = new SelectionManager();
        playerId = UUID.randomUUID();
    }
    
    @Test
    void getSelection_createsNewSelectionIfNotExists() {
        Selection selection = manager.getSelection(playerId);
        
        assertNotNull(selection);
        assertTrue(manager.hasSelection(playerId));
    }
    
    @Test
    void getSelection_returnsSameInstanceOnMultipleCalls() {
        Selection first = manager.getSelection(playerId);
        Selection second = manager.getSelection(playerId);
        
        assertSame(first, second);
    }
    
    @Test
    void getSelection_throwsExceptionForNullPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.getSelection(null);
        });
    }
    
    @Test
    void findSelection_returnsEmptyWhenNotExists() {
        Optional<Selection> result = manager.findSelection(playerId);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findSelection_returnsSelectionWhenExists() {
        Selection created = manager.getSelection(playerId);
        Optional<Selection> found = manager.findSelection(playerId);
        
        assertTrue(found.isPresent());
        assertSame(created, found.get());
    }
    
    @Test
    void findSelection_throwsExceptionForNullPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.findSelection(null);
        });
    }
    
    @Test
    void hasSelection_returnsFalseWhenNotExists() {
        assertFalse(manager.hasSelection(playerId));
    }
    
    @Test
    void hasSelection_returnsTrueWhenExists() {
        manager.getSelection(playerId);
        
        assertTrue(manager.hasSelection(playerId));
    }
    
    @Test
    void hasSelection_throwsExceptionForNullPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.hasSelection(null);
        });
    }
    
    @Test
    void clearSelection_removesSelection() {
        manager.getSelection(playerId);
        assertTrue(manager.hasSelection(playerId));
        
        manager.clearSelection(playerId);
        
        assertFalse(manager.hasSelection(playerId));
    }
    
    @Test
    void clearSelection_doesNothingWhenSelectionNotExists() {
        assertDoesNotThrow(() -> {
            manager.clearSelection(playerId);
        });
    }
    
    @Test
    void clearSelection_throwsExceptionForNullPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.clearSelection(null);
        });
    }
    
    @Test
    void multiplePlayersHaveIndependentSelections() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        Selection selection1 = manager.getSelection(player1);
        Selection selection2 = manager.getSelection(player2);
        
        assertNotSame(selection1, selection2);
        assertTrue(manager.hasSelection(player1));
        assertTrue(manager.hasSelection(player2));
        
        manager.clearSelection(player1);
        
        assertFalse(manager.hasSelection(player1));
        assertTrue(manager.hasSelection(player2));
    }
}
