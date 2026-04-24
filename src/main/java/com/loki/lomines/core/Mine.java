package com.loki.lomines.core;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.block.BlockSetter;
import com.loki.lomines.data.FillMode;
import com.loki.lomines.data.config.MineConfig;
import com.loki.lomines.util.BlockKeys;
import com.loki.lomines.handler.ActionBarHandler;
import com.loki.lomines.handler.MineBlockHandler;
import com.loki.lomines.handler.MineResetHandler;
import com.loki.lomines.util.Cuboid;
import dev.lolib.scheduler.Scheduler;
import dev.lolib.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a single mine with its configuration and state.
 * Thread-safe implementation using AtomicInteger for mutable state.
 */
public final class Mine {
    
    // Immutable configuration
    private final String name;
    private final List<Cuboid> regions;
    private final MineConfig config;
    private final BlockSetter blockSetter;
    private final LoMinesPlugin plugin;
    private final int totalVolume;
    /** Block keys for {@link FillMode#MASK}; empty when not mask mode. */
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
     * Creates a new mine instance.
     */
    public Mine(String name, MineConfig config, LoMinesPlugin plugin) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        this.regions = parseRegions(config);
        this.blockSetter = createBlockSetter(config, plugin);
        
        if (config.getFillMode() == FillMode.MASK) {
            this.maskBlockKeys = new HashSet<>();
            for (Location loc : config.getMaskPositions()) {
                maskBlockKeys.add(BlockKeys.key(loc));
            }
            this.totalVolume = config.getMaskPositions().size();
        } else {
            this.maskBlockKeys = Set.of();
            this.totalVolume = regions.stream().mapToInt(Cuboid::getVolume).sum();
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
        return regions.stream().anyMatch(region -> region.contains(location));
    }
    
    /**
     * Whether breaking this block counts toward mine progress ({@link FillMode#MASK} ignores blocks outside painted cells).
     */
    public boolean appliesToBlock(Location location) {
        if (!contains(location)) {
            return false;
        }
        if (config.getFillMode() != FillMode.MASK) {
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
     * Parses cuboid regions from selection points in the config.
     * Selections are paired: 1-2, 3-4, 5-6, 7-8, 9-10.
     */
    private List<Cuboid> parseRegions(MineConfig config) {
        List<Location> selections = config.getSelections();
        
        if (selections.isEmpty()) {
            throw new IllegalArgumentException("Mine must have at least one region (2 selection points)");
        }
        
        if (selections.size() % 2 != 0) {
            throw new IllegalArgumentException("Selections must be in pairs (even number of points)");
        }
        
        List<Cuboid> cuboids = new ArrayList<>();
        for (int i = 0; i < selections.size(); i += 2) {
            Location loc1 = selections.get(i);
            Location loc2 = selections.get(i + 1);
            cuboids.add(new Cuboid(loc1, loc2));
        }
        
        return Collections.unmodifiableList(cuboids);
    }
    
    /**
     * Creates the appropriate BlockSetter based on block configuration.
     */
    private BlockSetter createBlockSetter(MineConfig config, LoMinesPlugin plugin) {
        Map<String, Double> blocks = config.getBlocks();
        
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("Mine must have at least one block type");
        }
        
        // Get the first block key to determine the setter type
        String firstKey = blocks.keySet().iterator().next();
        
        if (firstKey.startsWith("oraxen:")) {
            return new com.loki.lomines.block.OraxenBlockSetter(blocks, plugin);
        } else if (firstKey.startsWith("itemsadder:")) {
            return new com.loki.lomines.block.ItemsAdderBlockSetter(blocks, plugin);
        } else {
            return new com.loki.lomines.block.BukkitBlockSetter(blocks, plugin);
        }
    }
    
    /**
     * Starts the action bar update task.
     */
    private void startActionBarTask() {
        if (!config.isActionBarEnabled()) {
            return;
        }
        
        actionBarTask = Scheduler.get(plugin).runTimer(() -> {
            actionBarHandler.sendToNearbyPlayers();
        }, 10L, 10L);
    }
    
    public String getName() { return name; }
    public MineConfig getConfig() { return config; }
    public BlockSetter getBlockSetter() { return blockSetter; }
    public List<Cuboid> getRegions() { return regions; }
    public AtomicInteger getBlocksAtomic() { return blocks; }
    public int getBlocks() { return blocks.get(); }
    public int getTicks() { return ticks.get(); }
    public AtomicInteger getTicksAtomic() { return ticks; }
}
