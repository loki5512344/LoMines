package dev.loki.lomines.util;

import dev.loki.lomines.util.format.ChunkUtils;
import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ChunkUtils.
 * Tests Paper detection and chunk update functionality.
 */
class ChunkUtilsTest {

    @Mock
    private World world;

    @Mock
    private Chunk chunk;

    @Mock
    private Player player;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateChunks_withNullRegion_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkUtils.updateChunks(null);
        });
    }

    @Test
    void testUpdateChunks_withSingleChunkRegion() {
        // Create a small region within a single chunk
        Location loc1 = new Location(world, 0, 64, 0);
        Location loc2 = new Location(world, 5, 70, 5);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
        when(world.getPlayers()).thenReturn(Collections.singletonList(player));
        when(chunk.getWorld()).thenReturn(world);
        when(chunk.getX()).thenReturn(0);
        when(chunk.getZ()).thenReturn(0);

        // Should not throw exception
        Cuboid region = new Cuboid(loc1, loc2);
        assertDoesNotThrow(() -> ChunkUtils.updateChunks(region));

        // Verify chunk was retrieved
        verify(world, atLeastOnce()).getChunkAt(0, 0);
    }

    @Test
    void testUpdateChunks_withMultipleChunks() {
        // Create a region spanning multiple chunks (16 blocks per chunk)
        Location loc1 = new Location(world, 0, 64, 0);
        Location loc2 = new Location(world, 32, 70, 32);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
        when(world.getPlayers()).thenReturn(Collections.singletonList(player));
        when(chunk.getWorld()).thenReturn(world);
        when(chunk.getX()).thenReturn(0);
        when(chunk.getZ()).thenReturn(0);

        Cuboid region = new Cuboid(loc1, loc2);
        assertDoesNotThrow(() -> ChunkUtils.updateChunks(region));

        // Should retrieve multiple chunks (0,0), (0,1), (0,2), (1,0), (1,1), (1,2), (2,0), (2,1), (2,2)
        // That's 9 chunks total for a 3x3 chunk area
        verify(world, atLeast(9)).getChunkAt(anyInt(), anyInt());
    }

    @Test
    void testUpdateChunks_withNegativeCoordinates() {
        // Test with negative coordinates
        Location loc1 = new Location(world, -16, 64, -16);
        Location loc2 = new Location(world, -1, 70, -1);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
        when(world.getPlayers()).thenReturn(Collections.singletonList(player));
        when(chunk.getWorld()).thenReturn(world);
        when(chunk.getX()).thenReturn(-1);
        when(chunk.getZ()).thenReturn(-1);

        Cuboid region = new Cuboid(loc1, loc2);
        assertDoesNotThrow(() -> ChunkUtils.updateChunks(region));

        // Verify chunk was retrieved for negative coordinates
        verify(world, atLeastOnce()).getChunkAt(-1, -1);
    }
}
