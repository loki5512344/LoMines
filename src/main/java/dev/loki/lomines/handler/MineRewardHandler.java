package dev.loki.lomines.handler;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.reward.Reward;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Handles reward distribution for block breaks in mines.
 * Checks configured rewards and gives items/executes commands based on chance.
 */
public final class MineRewardHandler {

    private final List<Reward> rewards;
    private final LoMinesPlugin plugin;
    private final Random random = new Random();

    public MineRewardHandler(MineConfig config, LoMinesPlugin plugin) {
        this.rewards = config.getRewards();
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

        for (Reward reward : rewards) {
            if (reward.matches(material) && rollChance(reward.chance())) {
                giveReward(player, reward);
            }
        }
    }

    /**
     * Rolls a random chance check.
     *
     * @param chance The chance percentage (0-100)
     * @return true if the roll succeeds
     */
    private boolean rollChance(double chance) {
        return random.nextDouble() * 100.0 < chance;
    }

    /**
     * Gives a reward to the player.
     *
     * @param player The player to give the reward to
     * @param reward The reward to give
     */
    private void giveReward(Player player, Reward reward) {
        for (ItemStack item : reward.items()) {
            player.getInventory().addItem(item.clone());
        }

        for (String command : reward.commands()) {
            String parsed = command.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
        }
    }
}
