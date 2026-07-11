package dev.loki.lomines.block;

import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Location;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * Abstract base class for block setting strategies.
 * Provides a template for filling mine regions with blocks.
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>BukkitBlockSetter - For vanilla Minecraft blocks</li>
 *   <li>OraxenBlockSetter - For Oraxen custom blocks</li>
 *   <li>ItemsAdderBlockSetter - For ItemsAdder custom blocks</li>
 * </ul>
 *
 * <p>This is a placeholder that will be fully implemented in task 2.7.</p>
 */
public abstract class BlockSetter {

    /**
     * Fills a cuboid region with blocks asynchronously.
     * The callback is invoked in the main thread after completion.
     *
     * @param region   The region to fill
     * @param callback Called with the number of blocks set when complete
     */
    public abstract void fill(Cuboid region, IntConsumer callback);

    /**
     * Places random weighted blocks only at the given locations (mask mode).
     */
    public abstract void fillAtLocations(List<Location> locations, IntConsumer callback);
}
