package dev.loki.lomines.core.mine;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.service.MineFileManager;
import dev.loki.lomines.block.BlockSetter;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.handler.ActionBarHandler;
import dev.loki.lomines.handler.MineBlockHandler;
import dev.loki.lomines.handler.MineResetHandler;
import dev.loki.lomines.util.location.BlockKeys;
import dev.loki.lomines.util.location.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
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

    private final String name;
    private final List<Cuboid> regions;
    private final MineConfig config;
    private final BlockSetter blockSetter;
    private final LoMinesPlugin plugin;
    private final int totalVolume;
    private final Set<String> maskBlockKeys;
    private final AtomicInteger blocks;
    private final AtomicInteger ticks;
    private final MineResetHandler resetHandler;
    private final MineBlockHandler blockHandler;
    private final ActionBarHandler actionBarHandler;
    private ScheduledTask actionBarTask;

    public Mine(String name, MineConfig config, LoMinesPlugin plugin) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        this.regions = config.region().regions();
        this.blockSetter = createBlockSetter(config, plugin);
        this.maskBlockKeys = initMaskBlockKeys(config);
        this.totalVolume = config.blocks().fillMode() == FillMode.MASK
                ? maskBlockKeys.size()
                : config.region().totalVolume();
        this.blocks = new AtomicInteger(Math.max(0, totalVolume));
        this.ticks = new AtomicInteger(0);
        this.resetHandler = new MineResetHandler(this, plugin);
        this.blockHandler = new MineBlockHandler(this, plugin);
        this.actionBarHandler = new ActionBarHandler(this);
    }

    private Set<String> initMaskBlockKeys(MineConfig config) {
        if (config.blocks().fillMode() != FillMode.MASK) {
            return Set.of();
        }
        Set<String> keys = new HashSet<>();
        if (config.blocks().mask() != null) {
            for (String pos : config.blocks().mask().positions().keySet()) {
                Location loc = LocationParser.parseSimple(pos);
                if (loc != null) {
                    keys.add(BlockKeys.key(loc));
                }
            }
        }
        return keys;
    }

    public void start() {
        startActionBarTask();
    }

    public void stop() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }

    public void reset(boolean silent) {
        resetHandler.reset(silent);
    }

    public void onBlockBreak(Player player, Block block) {
        blockHandler.handle(player, block);
    }

    public boolean contains(Location location) {
        return config.region().contains(location);
    }

    public boolean appliesToBlock(Location location) {
        if (!contains(location)) return false;
        if (config.blocks().fillMode() != FillMode.MASK) return true;
        return maskBlockKeys.contains(BlockKeys.key(location));
    }

    public int getTotalVolume() {
        return totalVolume;
    }

    public double getPercentFilled() {
        if (totalVolume == 0) return 0.0;
        return (double) blocks.get() / totalVolume * 100.0;
    }

    private BlockSetter createBlockSetter(MineConfig config, LoMinesPlugin plugin) {
        var weights = config.blocks().weights();
        if (weights.isEmpty()) {
            throw new IllegalArgumentException("Mine must have at least one block type");
        }
        BlockKey firstKey = weights.keySet().iterator().next();
        return switch (firstKey) {
            case BlockKey.Oraxen oraxen -> throw new IllegalArgumentException("Oraxen integration is currently disabled");
            case BlockKey.ItemsAdder itemsAdder -> throw new IllegalArgumentException("ItemsAdder integration is currently disabled");
            case BlockKey.Vanilla vanilla -> new dev.loki.lomines.block.BukkitBlockSetter(weights, plugin);
        };
    }

    private void startActionBarTask() {
        if (!config.ui().actionBarEnabled()) return;
        actionBarTask = Scheduler.get(plugin).runTimer(() -> {
            actionBarHandler.sendToNearbyPlayers();
        }, 10L, 10L);
    }

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
