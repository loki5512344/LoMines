package dev.loki.lomines.core.mine.model;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.block.BlockSetter;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.handler.ui.ActionBarHandler;
import dev.loki.lomines.handler.block.MineBlockHandler;
import dev.loki.lomines.handler.reset.MineResetHandler;
import dev.loki.lomines.util.location.BlockKeys;
import dev.loki.lomines.util.location.geo.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class Mine {

    private final String name;
    private final List<Cuboid> regions;
    private final MineConfig config;
    private final BlockSetter blockSetter;
    private final LoMinesPlugin plugin;
    private final MineState state;
    private final MineResetHandler resetHandler;
    private final MineBlockHandler blockHandler;
    private final ActionBarHandler actionBarHandler;
    private BukkitTask actionBarTask;
    private final Set<String> maskBlockKeys;

    public Mine(String name, MineConfig config, LoMinesPlugin plugin) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        this.regions = config.region().regions();
        this.blockSetter = createBlockSetter(config, plugin);
        this.maskBlockKeys = initMaskBlockKeys(config);
        this.state = new MineState(config);
        this.resetHandler = new MineResetHandler(this, plugin);
        this.blockHandler = new MineBlockHandler(this, plugin);
        this.actionBarHandler = new ActionBarHandler(this);
    }

    private Set<String> initMaskBlockKeys(MineConfig config) {
        if (config.blocks().fillMode() != FillMode.MASK || config.blocks().mask() == null) {
            return Set.of();
        }
        return config.blocks().mask().positions().keySet().stream()
                .map(LocationParser::parseSimple)
                .filter(Objects::nonNull)
                .map(BlockKeys::key)
                .collect(Collectors.toSet());
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
        return contains(location) && (config.blocks().fillMode() != FillMode.MASK || maskBlockKeys.contains(BlockKeys.key(location)));
    }

    public int getTotalVolume() {
        return state.getTotalVolume();
    }

    public double getPercentFilled() {
        return state.getPercentFilled();
    }

    private BlockSetter createBlockSetter(MineConfig config, LoMinesPlugin plugin) {
        var weights = config.blocks().weights();
        if (weights.isEmpty()) {
            throw new IllegalArgumentException("Mine must have at least one block type");
        }
        BlockKey firstKey = weights.keySet().iterator().next();
        return switch (firstKey) {
            case BlockKey.Oraxen oraxen -> throw new IllegalArgumentException("External plugins disabled");
            case BlockKey.ItemsAdder itemsAdder -> throw new IllegalArgumentException("External plugins disabled");
            case BlockKey.Vanilla vanilla -> new dev.loki.lomines.block.BukkitBlockSetter(weights, plugin);
        };
    }

    private void startActionBarTask() {
        if (config.ui().actionBarEnabled()) {
            actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> actionBarHandler.sendToNearbyPlayers(), 10L, 10L);
        }
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

    public int getBlocks() {
        return state.getBlocks();
    }

    public int getTicks() {
        return state.getTicks();
    }

    public AtomicInteger getBlocksAtomic() {
        return state.getBlocksAtomic();
    }

    public AtomicInteger getTicksAtomic() {
        return state.getTicksAtomic();
    }
}
