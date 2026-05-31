package com.loki.lomines.block;

import dev.lolib.scheduler.Scheduler;
import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.util.Cuboid;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * BlockSetter implementation for Oraxen custom blocks.
 * Uses Oraxen API to place custom blocks in mine regions.
 */
public final class OraxenBlockSetter extends BlockSetter {
    
    private final Map<String, Double> oraxenIds;
    private final LoMinesPlugin plugin;
    
    public OraxenBlockSetter(Map<String, Double> oraxenIds, LoMinesPlugin plugin) {
        this.oraxenIds = oraxenIds;
        this.plugin = plugin;
    }
    
    @Override
    public void fill(Cuboid region, IntConsumer callback) {
        Scheduler.get(plugin).runAsync(() -> {
            int count = fillSync(region);
            
            Scheduler.get(plugin).run(() -> {
                callback.accept(count);
            });
        });
    }
    
    @Override
    public void fillAtLocations(List<Location> locations, IntConsumer callback) {
        if (locations == null || locations.isEmpty()) {
            Scheduler.get(plugin).run(() -> callback.accept(0));
            return;
        }
        Scheduler.get(plugin).runAsync(() -> {
            int count = fillAtLocationsSync(locations);
            Scheduler.get(plugin).run(() -> callback.accept(count));
        });
    }
    
    private int fillSync(Cuboid region) {
        int count = 0;
        World world = region.getWorld();
        
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    String oraxenId = sampleOraxenId();
                    Block block = world.getBlockAt(x, y, z);
                    OraxenBlocks.place(oraxenId, block.getLocation());
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private int fillAtLocationsSync(List<Location> locations) {
        int count = 0;
        for (Location loc : locations) {
            if (loc.getWorld() == null) {
                continue;
            }
            String oraxenId = sampleOraxenId();
            Block block = loc.getBlock();
            OraxenBlocks.place(oraxenId, block.getLocation());
            count++;
        }
        return count;
    }
    
    private String sampleOraxenId() {
        // Simple weighted random selection
        double totalWeight = oraxenIds.values().stream().mapToDouble(Double::doubleValue).sum();
        double random = Math.random() * totalWeight;
        
        double currentWeight = 0;
        for (Map.Entry<String, Double> entry : oraxenIds.entrySet()) {
            currentWeight += entry.getValue();
            if (random <= currentWeight) {
                return entry.getKey();
            }
        }
        
        // Fallback to first entry
        return oraxenIds.keySet().iterator().next();
    }
}
