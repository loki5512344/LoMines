package dev.loki.lomines.util.block;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class BlockUpdateUtil {

    private BlockUpdateUtil() {
    }

    public static void sendBlockUpdate(Block block) {
        if (block == null) {
            return;
        }

        Chunk chunk = block.getChunk();
        World world = block.getWorld();

        for (Player player : world.getPlayers()) {
            if (isChunkVisibleToPlayer(player, chunk)) {
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }
        }
    }

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
        if (!player.getWorld().equals(chunk.getWorld())) {
            return false;
        }

        int renderDistance = player.getClientViewDistance();
        int playerChunkX = player.getLocation().getBlockX() >> 4;
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;

        int dx = Math.abs(playerChunkX - chunk.getX());
        int dz = Math.abs(playerChunkZ - chunk.getZ());

        return dx <= renderDistance && dz <= renderDistance;
    }

    private static boolean isLocationVisibleToPlayer(Player player, Location loc) {
        if (!player.getWorld().equals(loc.getWorld())) {
            return false;
        }

        double renderDistance = player.getClientViewDistance() * 16;
        return player.getLocation().distanceSquared(loc) <= renderDistance * renderDistance;
    }
}
