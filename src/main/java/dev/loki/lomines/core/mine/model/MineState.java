package dev.loki.lomines.core.mine.model;

import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.util.location.BlockKeys;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class MineState {

    private final AtomicInteger blocks;
    private final AtomicInteger ticks;
    private final int totalVolume;
    private final Set<String> maskBlockKeys;

    public MineState(MineConfig config) {
        this.maskBlockKeys = initMaskBlockKeys(config);
        this.totalVolume = config.blocks().fillMode() == FillMode.MASK
                ? maskBlockKeys.size()
                : config.region().totalVolume();
        this.blocks = new AtomicInteger(Math.max(0, totalVolume));
        this.ticks = new AtomicInteger(0);
    }

    private static Set<String> initMaskBlockKeys(MineConfig config) {
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

    public int getTotalVolume() {
        return totalVolume;
    }

    public double getPercentFilled() {
        if (totalVolume == 0) {
            return 0.0;
        }
        return (double) blocks.get() / totalVolume * 100.0;
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

    public Set<String> getMaskBlockKeys() {
        return maskBlockKeys;
    }

    public boolean appliesToBlock(Location location) {
        if (maskBlockKeys.isEmpty()) {
            return true;
        }
        return maskBlockKeys.contains(BlockKeys.key(location));
    }
}
