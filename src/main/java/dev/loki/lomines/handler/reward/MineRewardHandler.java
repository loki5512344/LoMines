package dev.loki.lomines.handler.reward;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.reward.RewardConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Handles reward distribution for block breaks in mines.
 * Checks configured rewards and gives items/executes commands based on chance.
 * Updated for new configuration system (v2).
 */
public final class MineRewardHandler {

    private final RewardConfig rewards;
    private final LoMinesPlugin plugin;
    private final Random random = new Random();

    public MineRewardHandler(MineConfig config, LoMinesPlugin plugin) {
        this.rewards = config.rewards();
        this.plugin = plugin;
    }

    /**
     * Checks and applies rewards for a broken block.
     *
     * @param player The player who broke the block
     * @param block  The block that was broken
     */
    public void checkRewards(Player player, Block block) {
        Material material = block.getType();
        BlockKey key = new BlockKey.Vanilla(material);

        for (var entry : rewards.forBlock(key)) {
            if (entry.roll(random)) {
                giveReward(player, entry);
            }
        }
    }

    /**
     * Gives a reward to the player.
     *
     * @param player The player to give the reward to
     * @param reward The reward entry to give
     */
    private void giveReward(Player player, RewardConfig.RewardEntry reward) {
        // Give items
        for (var item : reward.items()) {
            player.getInventory().addItem(item.toItemStack());
        }

        // Execute commands
        for (String command : reward.commands()) {
            String parsed = command
                    .replace("%player%", player.getName())
                    .replace("%uuid%", player.getUniqueId().toString());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
        }

        // Handle vanilla drops prevention if configured
        if (reward.preventVanillaDrops()) {
            // Note: This would need to be handled in the block break event
            // by setting dropItems to false in the event
        }
    }
}
