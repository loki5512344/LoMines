package com.loki.lomines.util.selection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds all blocks of a given vanilla material inside cuboid regions (marker paint for mask mode).
 */
public final class MaskScanner {
    
    private MaskScanner() {
    }
    
    /**
     * Collects block centers for every block whose type equals {@code marker} inside {@code regions}.
     */
    public static List<Location> scan(List<Cuboid> regions, Material marker) {
        List<Location> out = new ArrayList<>();
        for (Cuboid region : regions) {
            World world = region.getWorld();
            int minX = region.getMinX();
            int maxX = region.getMaxX();
            int minY = region.getMinY();
            int maxY = region.getMaxY();
            int minZ = region.getMinZ();
            int maxZ = region.getMaxZ();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == marker) {
                            out.add(block.getLocation());
                        }
                    }
                }
            }
        }
        return out;
    }
}
