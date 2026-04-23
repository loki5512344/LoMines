package com.loki.lomines.util.format;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for updating chunks after block modifications.
 * Automatically detects Paper API availability and uses the most efficient method.
 * 
 * <p>Paper detection is done through method reflection rather than Class.forName
 * to provide a more reliable mechanism as per requirement 21.4.</p>
 */
public final class ChunkUtils {
    
    private static final boolean IS_PAPER = detectPaper();
    private static final Method SEND_CHUNK_CHANGE_METHOD = findSendChunkChangeMethod();
    
    private ChunkUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Updates all chunks within the given cuboid region.
     * Uses Paper API if available for better performance, otherwise falls back to Bukkit API.
     * 
     * @param region The cuboid region whose chunks should be updated
     * @throws IllegalArgumentException if region is null
     */
    public static void updateChunks(Cuboid region) {
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }
        
        Set<ChunkCoordinate> chunks = getAffectedChunks(region);
        World world = region.getWorld();
        
        for (ChunkCoordinate coord : chunks) {
            Chunk chunk = world.getChunkAt(coord.x, coord.z);
            updateChunk(chunk);
        }
    }
    
    /**
     * Detects if Paper API is available by checking for Paper-specific methods.
     * This is more reliable than Class.forName as it checks for actual functionality.
     * 
     * @return true if Paper API is available, false otherwise
     */
    private static boolean detectPaper() {
        try {
            // Check for Paper-specific method in Chunk class
            // Paper adds sendChunkChange methods that Bukkit doesn't have
            Class<?> chunkClass = Chunk.class;
            chunkClass.getMethod("getPluginChunkTickets");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    /**
     * Finds the sendChunkChange method if running on Paper.
     * 
     * @return The sendChunkChange method, or null if not available
     */
    private static Method findSendChunkChangeMethod() {
        if (!IS_PAPER) {
            return null;
        }
        
        try {
            Class<?> chunkClass = Chunk.class;
            // Paper 1.21 has sendChunkChange() method
            return chunkClass.getMethod("sendChunkChange");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    
    /**
     * Updates a single chunk using the appropriate API.
     * 
     * @param chunk The chunk to update
     */
    private static void updateChunk(Chunk chunk) {
        if (IS_PAPER && SEND_CHUNK_CHANGE_METHOD != null) {
            updateChunkPaper(chunk);
        } else {
            updateChunkBukkit(chunk);
        }
    }
    
    /**
     * Updates a chunk using Paper API.
     * 
     * @param chunk The chunk to update
     */
    private static void updateChunkPaper(Chunk chunk) {
        try {
            SEND_CHUNK_CHANGE_METHOD.invoke(chunk);
        } catch (Exception e) {
            // Fall back to Bukkit if Paper method fails
            updateChunkBukkit(chunk);
        }
    }
    
    /**
     * Updates a chunk using Bukkit API.
     * Forces chunk to be resent to all players by marking it as modified.
     * 
     * @param chunk The chunk to update
     */
    private static void updateChunkBukkit(Chunk chunk) {
        // Bukkit doesn't have a direct chunk update method
        // The chunk will be automatically sent to players when they are nearby
        // We can force a refresh by unloading and reloading, but that's expensive
        // Instead, we rely on the natural chunk update mechanism
        // Players will see the changes when the chunk is next sent to them
        
        // For Bukkit, we can use the World's refreshChunk method if available
        World world = chunk.getWorld();
        try {
            // Try to use refreshChunk if it exists (some Bukkit versions have it)
            Method refreshChunk = World.class.getMethod("refreshChunk", int.class, int.class);
            refreshChunk.invoke(world, chunk.getX(), chunk.getZ());
        } catch (Exception e) {
            // If refreshChunk doesn't exist, chunks will update naturally
            // This is acceptable as blocks are already set
        }
    }
    
    /**
     * Calculates all chunk coordinates that are affected by the given region.
     * 
     * @param region The cuboid region
     * @return Set of chunk coordinates
     */
    private static Set<ChunkCoordinate> getAffectedChunks(Cuboid region) {
        Set<ChunkCoordinate> chunks = new HashSet<>();
        
        // Convert block coordinates to chunk coordinates
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
    
    /**
     * Simple immutable holder for chunk coordinates.
     */
    private static final class ChunkCoordinate {
        final int x;
        final int z;
        
        ChunkCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ChunkCoordinate)) return false;
            ChunkCoordinate other = (ChunkCoordinate) obj;
            return x == other.x && z == other.z;
        }
        
        @Override
        public int hashCode() {
            return 31 * x + z;
        }
    }
}
