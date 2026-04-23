package com.loki.lomines.block;

import dev.lolib.scheduler.Scheduler;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.util.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * BlockSetter implementation for vanilla Minecraft blocks.
 * Uses Bukkit API to set blocks with optimal performance settings.
 */
public final class BukkitBlockSetter extends BlockSetter {
    
    private final Map<String, Double> weights;
    private final LoMinesPlugin plugin;
    
    public BukkitBlockSetter(Map<String, Double> weights, LoMinesPlugin plugin) {
        this.weights = weights;
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
                    BlockData data = sampleBlockData();
                    world.getBlockAt(x, y, z).setBlockData(data, false);
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
            BlockData data = sampleBlockData();
            loc.getBlock().setBlockData(data, false);
            count++;
        }
        return count;
    }
    
    private BlockData sampleBlockData() {
        // Simple weighted random selection
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double random = Math.random() * totalWeight;
        
        double currentWeight = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (random <= currentWeight) {
                // Convert string key to BlockData
                Material material = Material.valueOf(entry.getKey().toUpperCase());
                return Bukkit.createBlockData(material);
            }
        }
        
        // Fallback to first entry
        String firstKey = weights.keySet().iterator().next();
        Material material = Material.valueOf(firstKey.toUpperCase());
        return Bukkit.createBlockData(material);
    }
}
