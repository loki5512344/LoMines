package dev.loki.lomines.util.format;

import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public final class ChunkRefresher {

    private ChunkRefresher() {
    }

    public static void refreshChunks(Cuboid region, boolean isPaper, Method sendChunkChangeMethod) {
        Set<ChunkCoordinate> chunks = getAffectedChunks(region);
        World world = region.getWorld();

        for (ChunkCoordinate coord : chunks) {
            Chunk chunk = world.getChunkAt(coord.x, coord.z);
            updateChunk(chunk, isPaper, sendChunkChangeMethod);
        }
    }

    private static void updateChunk(Chunk chunk, boolean isPaper, Method sendChunkChangeMethod) {
        if (isPaper && sendChunkChangeMethod != null) {
            updateChunkPaper(chunk, sendChunkChangeMethod);
        } else {
            updateChunkBukkit(chunk);
        }
    }

    private static void updateChunkPaper(Chunk chunk, Method sendChunkChangeMethod) {
        try {
            sendChunkChangeMethod.invoke(chunk);
        } catch (Exception e) {
            updateChunkBukkit(chunk);
        }
    }

    private static void updateChunkBukkit(Chunk chunk) {
        World world = chunk.getWorld();
        try {
            Method refreshChunk = World.class.getMethod("refreshChunk", int.class, int.class);
            refreshChunk.invoke(world, chunk.getX(), chunk.getZ());
        } catch (Exception e) {
            // chunks will update naturally
        }
    }

    private static Set<ChunkCoordinate> getAffectedChunks(Cuboid region) {
        Set<ChunkCoordinate> chunks = new HashSet<>();
        int minChunkX = region.getMinX() >> 4;
        int maxChunkX = region.getMaxX() >> 4;
        int minChunkZ = region.getMinZ() >> 4;
        int maxChunkZ = region.getMaxZ() >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(new ChunkCoordinate(chunkX, chunkZ));
            }
        }
        return chunks;
    }

    private record ChunkCoordinate(int x, int z) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ChunkCoordinate(int x1, int z1))) {
                return false;
            }
            return x == x1 && z == z1;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, z);
        }
    }
}
