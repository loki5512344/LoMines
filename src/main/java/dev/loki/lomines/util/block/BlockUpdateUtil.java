package dev.loki.lomines.util.block;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Utility for sending block updates to clients to prevent ghost blocks.
 *
 * <p>Ghost blocks occur when the server sets blocks without notifying clients,
 * causing a desync where blocks appear invisible or behave strangely on the client.
 * This utility forces block updates to be sent to all relevant players.</p>
 */
public final class BlockUpdateUtil {

    private BlockUpdateUtil() {
    }

    /**
     * Sends a block update to all players who can see the specified chunk.
     * This prevents ghost blocks by forcing the server to send the block state to clients.
     *
     * @param block the block to update
     */
    public static void sendBlockUpdate(Block block) {
        if (block == null) {
            return;
        }

        // Get chunk and players in it
        Chunk chunk = block.getChunk();
        World world = block.getWorld();

        // Send block change to all players in the world who can see this chunk
        for (Player player : world.getPlayers()) {
            if (isChunkVisibleToPlayer(player, chunk)) {
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }
        }
    }

    /**
     * Sends block updates for all blocks in a cuboid region.
     * Uses batching to minimize packet overhead.
     *
     * @param world the world containing the blocks
     * @param minX  minimum X coordinate
     * @param minY  minimum Y coordinate
     * @param minZ  minimum Z coordinate
     * @param maxX  maximum X coordinate
     * @param maxY  maximum Y coordinate
     * @param maxZ  maximum Z coordinate
     */
    public static void sendRegionUpdate(World world, int minX, int minY, int minZ,
                                        int maxX, int maxY, int maxZ) {
        Collection<Player> players = world.getPlayers();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    for (Player player : players) {
                        if (isLocationVisibleToPlayer(player, block.getLocation())) {
                            player.sendBlockChange(block.getLocation(), block.getBlockData());
                        }
                    }
                }
            }
        }
    }

    /**
     * Sends updates for a list of locations. More efficient than region update
     * when only specific positions need updating.
     *
     * @param locations the locations to update
     */
    public static void sendLocationsUpdate(java.util.List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }

        for (Location loc : locations) {
            if (loc.getWorld() == null) {
                continue;
            }
            Block block = loc.getBlock();

            for (Player player : loc.getWorld().getPlayers()) {
                if (isLocationVisibleToPlayer(player, loc)) {
                    player.sendBlockChange(loc, block.getBlockData());
                }
            }
        }
    }

    /**
     * Refreshes chunks for all players in a region.
     * This is a heavier operation but ensures complete chunk resync.
     *
     * @param world the world
     * @param minX  minimum block X
     * @param minZ  minimum block Z
     * @param maxX  maximum block X
     * @param maxZ  maximum block Z
     */
    public static void refreshChunks(World world, int minX, int minZ, int maxX, int maxZ) {
        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                if (chunk.isLoaded()) {
                    for (Player player : world.getPlayers()) {
                        player.sendChunkUpdate(chunk);
                    }
                }
            }
        }
    }

    /**
     * Finds a safe teleport location near the given destination.
     * Checks for suffocation hazards (blocks at head/body level).
     *
     * @param destination the desired destination
     * @return a safe location (may be the same as destination if safe)
     */
    public static Location findSafeTeleportLocation(Location destination) {
        if (destination == null || destination.getWorld() == null) {
            return destination;
        }

        World world = destination.getWorld();
        int x = destination.getBlockX();
        int y = destination.getBlockY();
        int z = destination.getBlockZ();
        float yaw = destination.getYaw();
        float pitch = destination.getPitch();

        // Check if original location is safe
        if (isSafeLocation(world, x, y, z)) {
            return destination;
        }

        // Search upward for a safe spot (priority: don't drop player down)
        for (int offset = 1; offset <= 5; offset++) {
            if (isSafeLocation(world, x, y + offset, z)) {
                return new Location(world, x + 0.5, y + offset, z + 0.5, yaw, pitch);
            }
        }

        // Search downward if no safe spot above
        for (int offset = 1; offset <= 5 && y - offset >= world.getMinHeight(); offset++) {
            if (isSafeLocation(world, x, y - offset, z)) {
                return new Location(world, x + 0.5, y - offset, z + 0.5, yaw, pitch);
            }
        }

        // Search nearby blocks
        int[][] nearby = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] offset : nearby) {
            for (int yOffset = 0; yOffset <= 3; yOffset++) {
                int newY = y + yOffset;
                if (newY < world.getMinHeight() || newY >= world.getMaxHeight()) {
                    continue;
                }
                if (isSafeLocation(world, x + offset[0], newY, z + offset[1])) {
                    return new Location(world, x + offset[0] + 0.5, newY, z + offset[1] + 0.5, yaw, pitch);
                }
            }
        }

        // If nothing else works, return original but at least center it
        return new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
    }

    /**
     * Checks if a location is safe for teleport (no suffocation).
     */
    private static boolean isSafeLocation(World world, int x, int y, int z) {
        // Check feet position (can be passable)
        Block feetBlock = world.getBlockAt(x, y, z);
        // Check head position (must be passable)
        Block headBlock = world.getBlockAt(x, y + 1, z);

        // Safe if feet is passable and head is passable
        return isPassable(feetBlock) && isPassable(headBlock);
    }

    /**
     * Checks if a block is passable (not solid).
     */
    private static boolean isPassable(Block block) {
        return !block.getType().isSolid() || block.getType().isAir();
    }

    /**
     * Checks if a player can see a chunk (is within render distance).
     */
    private static boolean isChunkVisibleToPlayer(Player player, Chunk chunk) {
        if (!player.getWorld().equals(chunk.getWorld())) {
            return false;
        }

        int renderDistance = player.getClientViewDistance();
        int playerChunkX = player.getLocation().getBlockX() >> 4;
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;

        int dx = Math.abs(playerChunkX - chunk.getX());
        int dz = Math.abs(playerChunkZ - chunk.getZ());

        // Check if chunk is within player's view distance
        return dx <= renderDistance && dz <= renderDistance;
    }

    /**
     * Checks if a location is visible to a player (within render distance).
     */
    private static boolean isLocationVisibleToPlayer(Player player, Location loc) {
        if (!player.getWorld().equals(loc.getWorld())) {
            return false;
        }

        double renderDistance = player.getClientViewDistance() * 16; // blocks
        return player.getLocation().distanceSquared(loc) <= renderDistance * renderDistance;
    }
}
