package com.loki.lomines.util.location;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Stable string keys for block-aligned locations (world + block integer coords).
 */
public final class BlockKeys {
    
    private BlockKeys() {
    }
    
    public static String key(Block block) {
        return key(block.getLocation());
    }
    
    public static String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
}
