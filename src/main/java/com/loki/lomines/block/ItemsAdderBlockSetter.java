package com.loki.lomines.block;

import dev.lolib.scheduler.Scheduler;
import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.util.Cuboid;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * BlockSetter implementation for ItemsAdder custom blocks.
 * Uses ItemsAdder API to place custom blocks in mine regions.
 */
public final class ItemsAdderBlockSetter extends BlockSetter {
    
    private final Map<String, Double> itemsAdderIds;
    private final LoMinesPlugin plugin;
    
    public ItemsAdderBlockSetter(Map<String, Double> itemsAdderIds, LoMinesPlugin plugin) {
        this.itemsAdderIds = itemsAdderIds;
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
                    String itemsAdderId = sampleItemsAdderId();
                    Block block = world.getBlockAt(x, y, z);
                    CustomBlock.place(itemsAdderId, block.getLocation());
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
            String itemsAdderId = sampleItemsAdderId();
            Block block = loc.getBlock();
            CustomBlock.place(itemsAdderId, block.getLocation());
            count++;
        }
        return count;
    }
    
    private String sampleItemsAdderId() {
        // Simple weighted random selection
        double totalWeight = itemsAdderIds.values().stream().mapToDouble(Double::doubleValue).sum();
        double random = Math.random() * totalWeight;
        
        double currentWeight = 0;
        for (Map.Entry<String, Double> entry : itemsAdderIds.entrySet()) {
            currentWeight += entry.getValue();
            if (random <= currentWeight) {
                return entry.getKey();
            }
        }
        
        // Fallback to first entry
        return itemsAdderIds.keySet().iterator().next();
    }
}
