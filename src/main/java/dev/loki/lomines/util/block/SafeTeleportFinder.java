package dev.loki.lomines.util.block;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class SafeTeleportFinder {

    private SafeTeleportFinder() {
    }

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
