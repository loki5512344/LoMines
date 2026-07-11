package dev.loki.lomines.block;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.util.block.BlockUpdateUtil;
import dev.loki.lomines.util.location.geo.Cuboid;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * BlockSetter implementation for vanilla Minecraft blocks.
 * Uses Bukkit API to set blocks with optimal performance settings.
 * Updated for BlockKey type-safe configuration.
 *
 * <p>Prevents ghost blocks by sending block update packets to clients
 * after bulk block placement.</p>
 */
public final class BukkitBlockSetter extends BlockSetter {

    private final Map<BlockKey, Double> weights;
    private final LoMinesPlugin plugin;

    public BukkitBlockSetter(Map<BlockKey, Double> weights, LoMinesPlugin plugin) {
        this.weights = weights;
        this.plugin = plugin;
    }

    @Override
    public void fill(Cuboid region, IntConsumer callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int count = fillSync(region);

            // Send block updates on main thread to prevent ghost blocks
            Bukkit.getScheduler().runTask(plugin, () -> {
                BlockUpdateUtil.sendRegionUpdate(
                        region.getWorld(),
                        region.getMinX(), region.getMinY(), region.getMinZ(),
                        region.getMaxX(), region.getMaxY(), region.getMaxZ()
                );
                callback.accept(count);
            });
        });
    }

    @Override
    public void fillAtLocations(List<Location> locations, IntConsumer callback) {
        if (locations == null || locations.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(0));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int count = fillAtLocationsSync(locations);

            // Send block updates on main thread to prevent ghost blocks
            Bukkit.getScheduler().runTask(plugin, () -> {
                BlockUpdateUtil.sendLocationsUpdate(locations);
                callback.accept(count);
            });
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
        double random = Math.random();

        double currentWeight = 0;
        for (Map.Entry<BlockKey, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (random <= currentWeight) {
                // BlockKey is already validated to be a block
                if (entry.getKey() instanceof BlockKey.Vanilla(Material material)) {
                    return Bukkit.createBlockData(material);
                }
                // Fallback for non-vanilla keys in vanilla setter (shouldn't happen)
                return Bukkit.createBlockData(Material.STONE);
            }
        }

        // Fallback to first entry
        BlockKey firstKey = weights.keySet().iterator().next();
        if (firstKey instanceof BlockKey.Vanilla(Material material)) {
            return Bukkit.createBlockData(material);
        }
        return Bukkit.createBlockData(Material.STONE);
    }
}
