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
        if (block == null) return;

        Chunk chunk = block.getChunk();
        World world = block.getWorld();

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
        if (locations == null || locations.isEmpty()) return;

        for (Location loc : locations) {
            if (loc.getWorld() == null) continue;
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
                            player.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                        }
                }
            }
        }
    }

    private static boolean isChunkVisibleToPlayer(Player player, Chunk chunk) {
        if (!player.getWorld().equals(chunk.getWorld())) return false;

        int renderDistance = player.getClientViewDistance();
        int playerChunkX = player.getLocation().getBlockX() >> 4;
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;

        int dx = Math.abs(playerChunkX - chunk.getX());
        int dz = Math.abs(playerChunkZ - chunk.getZ());

        return dx <= renderDistance && dz <= renderDistance;
    }

    private static boolean isLocationVisibleToPlayer(Player player, Location loc) {
        if (!player.getWorld().equals(loc.getWorld())) return false;

        double renderDistance = player.getClientViewDistance() * 16;
        return player.getLocation().distanceSquared(loc) <= renderDistance * renderDistance;
    }

    /**
     * Finds a safe teleport location around the target by searching upward.
     * A location is safe when feet and head blocks are non-solid.
     */
    public static Location findSafeTeleportLocation(Location target, int maxBlocksUp) {
        if (target == null || target.getWorld() == null) {
            return target;
        }

        Location base = target.clone();
        for (int i = 0; i <= Math.max(0, maxBlocksUp); i++) {
            Location candidate = base.clone().add(0, i, 0);
            Block feet = candidate.getBlock();
            Block head = candidate.clone().add(0, 1, 0).getBlock();
            if (!feet.getType().isSolid() && !head.getType().isSolid()) {
                return candidate;
            }
        }
        return base;
    }
}
