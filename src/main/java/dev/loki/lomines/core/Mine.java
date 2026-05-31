package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.block.BlockSetter;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.handler.ActionBarHandler;
import dev.loki.lomines.handler.MineBlockHandler;
import dev.loki.lomines.handler.MineResetHandler;
import dev.loki.lomines.util.location.BlockKeys;
import dev.loki.lomines.util.location.Cuboid;
import dev.lolib.scheduler.ScheduledTask;
import dev.lolib.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a single mine with its configuration and state.
 * Thread-safe implementation using AtomicInteger for mutable state.
 * Updated for new section-based configuration (v2).
 */
public final class Mine {

    // Immutable configuration
    private final String name;
    private final List<Cuboid> regions;
    private final MineConfig config;
    private final BlockSetter blockSetter;
    private final LoMinesPlugin plugin;
    private final int totalVolume;
    /**
     * Block keys for {@link FillMode#MASK}; empty when not mask mode.
     */
    private final Set<String> maskBlockKeys;

    // Mutable state (thread-safe)
    private final AtomicInteger blocks;
    private final AtomicInteger ticks;

    // Handlers
    private final MineResetHandler resetHandler;
    private final MineBlockHandler blockHandler;
    private final ActionBarHandler actionBarHandler;

    // Tasks
    private ScheduledTask actionBarTask;

    /**
     * Creates a new mine instance with new configuration system.
     */
    public Mine(String name, MineConfig config, LoMinesPlugin plugin) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        this.regions = config.region().regions();
        this.blockSetter = createBlockSetter(config, plugin);

        if (config.blocks().fillMode() == FillMode.MASK) {
            this.maskBlockKeys = new HashSet<>();
            if (config.blocks().mask() != null) {
                for (String pos : config.blocks().mask().positions().keySet()) {
                    Location loc = parseLocation(pos);
                    if (loc != null) {
                        maskBlockKeys.add(BlockKeys.key(loc));
                    }
                }
            }
            this.totalVolume = maskBlockKeys.size();
        } else {
            this.maskBlockKeys = Set.of();
            this.totalVolume = config.region().totalVolume();
        }
        this.blocks = new AtomicInteger(Math.max(0, totalVolume));
        this.ticks = new AtomicInteger(0);

        this.resetHandler = new MineResetHandler(this, plugin);
        this.blockHandler = new MineBlockHandler(this, plugin);
        this.actionBarHandler = new ActionBarHandler(this);
    }

    /**
     * Starts periodic tasks for this mine.
     */
    public void start() {
        startActionBarTask();
    }

    /**
     * Stops all tasks for this mine.
     */
    public void stop() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }

    /**
     * Resets the mine by filling all regions with blocks.
     */
    public void reset(boolean silent) {
        resetHandler.reset(silent);
    }

    /**
     * Handles a block break event in this mine.
     */
    public void onBlockBreak(Player player, Block block) {
        blockHandler.handle(player, block);
    }

    /**
     * Checks if the given location is within any region of this mine.
     */
    public boolean contains(Location location) {
        return config.region().contains(location);
    }

    /**
     * Whether breaking this block counts toward mine progress ({@link FillMode#MASK} ignores blocks outside painted cells).
     */
    public boolean appliesToBlock(Location location) {
        if (!contains(location)) {
            return false;
        }
        if (config.blocks().fillMode() != FillMode.MASK) {
            return true;
        }
        return maskBlockKeys.contains(BlockKeys.key(location));
    }

    /**
     * Calculates the total volume of all regions in this mine.
     */
    public int getTotalVolume() {
        return totalVolume;
    }

    /**
     * Calculates the percentage of blocks remaining in the mine.
     */
    public double getPercentFilled() {
        if (totalVolume == 0) {
            return 0.0;
        }
        return (double) blocks.get() / totalVolume * 100.0;
    }

    /**
     * Creates the appropriate BlockSetter based on block configuration.
     */
    private BlockSetter createBlockSetter(MineConfig config, LoMinesPlugin plugin) {
        var weights = config.blocks().weights();

        if (weights.isEmpty()) {
            throw new IllegalArgumentException("Mine must have at least one block type");
        }

        // Get the first block key to determine the setter type
        BlockKey firstKey = weights.keySet().iterator().next();

        return switch (firstKey) {
            case BlockKey.Oraxen oraxen -> {
                // TODO: Enable when OraxenBlockSetter is ready
                throw new IllegalArgumentException("Oraxen integration is currently disabled");
            }
            case BlockKey.ItemsAdder itemsAdder -> {
                // TODO: Enable when ItemsAdderBlockSetter is ready
                throw new IllegalArgumentException("ItemsAdder integration is currently disabled");
            }
            case BlockKey.Vanilla vanilla ->
                new dev.loki.lomines.block.BukkitBlockSetter(weights, plugin);
        };
    }

    /**
     * Starts the action bar update task.
     */
    private void startActionBarTask() {
        if (!config.ui().actionBarEnabled()) {
            return;
        }

        actionBarTask = Scheduler.get(plugin).runTimer(() -> {
            actionBarHandler.sendToNearbyPlayers();
        }, 10L, 10L);
    }

    private Location parseLocation(String str) {
        String[] parts = str.split(";");
        if (parts.length < 4) return null;

        var world = org.bukkit.Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Getters

    public String getName() {
        return name;
    }

    public MineConfig getConfig() {
        return config;
    }

    public BlockSetter getBlockSetter() {
        return blockSetter;
    }

    public List<Cuboid> getRegions() {
        return regions;
    }

    public AtomicInteger getBlocksAtomic() {
        return blocks;
    }

    public int getBlocks() {
        return blocks.get();
    }

    public int getTicks() {
        return ticks.get();
    }

    public AtomicInteger getTicksAtomic() {
        return ticks;
    }
}
