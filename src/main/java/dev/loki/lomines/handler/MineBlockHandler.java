package dev.loki.lomines.handler;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.MineConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Handles block break events within a mine.
 * Coordinates reward checking, statistics updates, and reset condition checks.
 * Updated for new configuration system (v2).
 */
public final class MineBlockHandler {

    private final Mine mine;
    private final LoMinesPlugin plugin;
    private final MineRewardHandler rewardHandler;

    public MineBlockHandler(Mine mine, LoMinesPlugin plugin) {
        this.mine = mine;
        this.plugin = plugin;
        this.rewardHandler = new MineRewardHandler(mine.getConfig(), plugin);
    }

    /**
     * Handles a block break event in the mine.
     *
     * @param player The player who broke the block
     * @param block  The block that was broken
     */
    public void handle(Player player, Block block) {
        int remaining = mine.getBlocksAtomic().decrementAndGet();

        rewardHandler.checkRewards(player, block);

        if (plugin.getConfig().getBoolean("statistics-enabled", true)) {
            plugin.getStatsManager().incrementBlocks(player.getUniqueId(), mine.getName());
        }

        checkResetConditions(remaining);
    }

    /**
     * Checks if reset conditions are met and triggers reset if needed.
     */
    private void checkResetConditions(int remaining) {
        MineConfig config = mine.getConfig();
        int total = mine.getTotalVolume();
        if (total <= 0) {
            return;
        }
        double percent = (double) remaining / total * 100.0;

        if (config.reset().isPercentTriggerEnabled() && percent <= config.reset().percentTrigger()) {
            mine.reset(false);
        }
    }
}
