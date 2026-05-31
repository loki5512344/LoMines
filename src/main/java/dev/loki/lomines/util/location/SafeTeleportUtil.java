package dev.loki.lomines.util.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Utility for finding safe teleport locations.
 * Checks for suffocation hazards and finds alternative safe positions.
 */
public final class SafeTeleportUtil {

    private SafeTeleportUtil() {}

    /**
     * Finds a safe teleport location near the given destination.
     * Checks for suffocation hazards (blocks at head/body level).
     * <p>Will not teleport too high - limited to maxUpOffset blocks above original.</p>
     *
     * @param destination the desired destination
     * @param maxUpOffset maximum blocks to search upward (to avoid teleporting too high)
     * @return a safe location (may be the same as destination if safe)
     */
    public static Location findSafeTeleportLocation(Location destination, int maxUpOffset) {
        if (destination == null || destination.getWorld() == null) {
            return destination;
        }

        World world = destination.getWorld();
        int x = destination.getBlockX();
        int y = destination.getBlockY();
        int z = destination.getBlockZ();
        float yaw = destination.getYaw();
        float pitch = destination.getPitch();

        maxUpOffset = Math.max(2, Math.min(maxUpOffset, 10));

        if (isSafeLocation(world, x, y, z)) {
            return destination;
        }

        Location result = findNearbySafe(world, x, y, z, yaw, pitch);
        if (result != null) return result;

        result = searchUpward(world, x, y, z, yaw, pitch, maxUpOffset);
        if (result != null) return result;

        result = searchDownward(world, x, y, z, yaw, pitch);
        if (result != null) return result;

        result = searchDiagonal(world, x, y, z, yaw, pitch, maxUpOffset);
        if (result != null) return result;

        return new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
    }

    /**
     * Finds a safe teleport location with default max up offset of 3 blocks.
     */
    public static Location findSafeTeleportLocation(Location destination) {
        return findSafeTeleportLocation(destination, 3);
    }

    private static Location findNearbySafe(World world, int x, int y, int z, float yaw, float pitch) {
        int[][] nearby = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] offset : nearby) {
            if (isSafeLocation(world, x + offset[0], y, z + offset[1])) {
                return new Location(world, x + offset[0] + 0.5, y, z + offset[1] + 0.5, yaw, pitch);
            }
        }
        return null;
    }

    private static Location searchUpward(World world, int x, int y, int z, float yaw, float pitch, int maxUpOffset) {
        for (int offset = 1; offset <= maxUpOffset; offset++) {
            if (isSafeLocation(world, x, y + offset, z)) {
                return new Location(world, x + 0.5, y + offset, z + 0.5, yaw, pitch);
            }
        }
        return null;
    }

    private static Location searchDownward(World world, int x, int y, int z, float yaw, float pitch) {
        for (int offset = 1; offset <= 5 && y - offset >= world.getMinHeight(); offset++) {
            if (isSafeLocation(world, x, y - offset, z)) {
                return new Location(world, x + 0.5, y - offset, z + 0.5, yaw, pitch);
            }
        }
        return null;
    }

    private static Location searchDiagonal(World world, int x, int y, int z, float yaw, float pitch, int maxUpOffset) {
        int[][] nearbyDiagonal = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] offset : nearbyDiagonal) {
            for (int yOffset = -1; yOffset <= maxUpOffset; yOffset++) {
                int newY = y + yOffset;
                if (newY < world.getMinHeight() || newY >= world.getMaxHeight()) {
                    continue;
                }
                if (isSafeLocation(world, x + offset[0], newY, z + offset[1])) {
                    return new Location(world, x + offset[0] + 0.5, newY, z + offset[1] + 0.5, yaw, pitch);
                }
            }
        }
        return null;
    }

    private static boolean isSafeLocation(World world, int x, int y, int z) {
        Block feetBlock = world.getBlockAt(x, y, z);
        Block headBlock = world.getBlockAt(x, y + 1, z);
        return isPassable(feetBlock) && isPassable(headBlock);
    }

    private static boolean isPassable(Block block) {
        return !block.getType().isSolid() || block.getType().isAir();
    }
}
