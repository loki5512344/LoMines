package dev.loki.lomines.util.location.geo;

import org.bukkit.Location;
import org.bukkit.World;

/** Immutable 3D rectangular region in a Minecraft world. Thread-safe. */
public final class Cuboid {

    private final World world;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public Cuboid(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            throw new IllegalArgumentException("Locations cannot be null");
        }

        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            throw new IllegalArgumentException("Location worlds cannot be null");
        }

        if (!loc1.getWorld().equals(loc2.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world");
        }

        this.world = loc1.getWorld();

        // Calculate min/max coordinates
        this.minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        this.minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        this.minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        this.maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        this.maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        this.maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        if (!location.getWorld().equals(world)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public int getVolume() {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        return width * height * depth;
    }

    public World getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    @Override
    public String toString() {
        return String.format("Cuboid[world=%s, min=(%d,%d,%d), max=(%d,%d,%d), volume=%d]",
                world.getName(), minX, minY, minZ, maxX, maxY, maxZ, getVolume());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cuboid other)) {
            return false;
        }

        return world.equals(other.world)
                && minX == other.minX && minY == other.minY && minZ == other.minZ
                && maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ;
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + minX;
        result = 31 * result + minY;
        result = 31 * result + minZ;
        result = 31 * result + maxX;
        result = 31 * result + maxY;
        result = 31 * result + maxZ;
        return result;
    }
}
