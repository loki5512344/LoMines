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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChunkUtilsRefreshTest {

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
    void testUpdateChunks_withLargeRegion() {
        Location loc1 = new Location(world, 0, 0, 0);
        Location loc2 = new Location(world, 100, 100, 100);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
        when(world.getPlayers()).thenReturn(Collections.emptyList());
        when(chunk.getWorld()).thenReturn(world);

        Cuboid region = new Cuboid(loc1, loc2);
        assertDoesNotThrow(() -> ChunkUtils.updateChunks(region));

        verify(world, atLeast(40)).getChunkAt(anyInt(), anyInt());
    }

    @Test
    void testUpdateChunks_withNoPlayers() {
        Location loc1 = new Location(world, 0, 64, 0);
        Location loc2 = new Location(world, 5, 70, 5);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
        when(world.getPlayers()).thenReturn(Collections.emptyList());
        when(chunk.getWorld()).thenReturn(world);

        Cuboid region = new Cuboid(loc1, loc2);
        assertDoesNotThrow(() -> ChunkUtils.updateChunks(region));
    }

    @Test
    void testChunkCoordinateCalculation() {
        Location loc1 = new Location(world, 0, 64, 0);
        Location loc2 = new Location(world, 15, 70, 15);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
        when(world.getPlayers()).thenReturn(Collections.emptyList());
        when(chunk.getWorld()).thenReturn(world);

        Cuboid singleChunk = new Cuboid(loc1, loc2);
        ChunkUtils.updateChunks(singleChunk);

        verify(world, times(1)).getChunkAt(0, 0);
    }
}
