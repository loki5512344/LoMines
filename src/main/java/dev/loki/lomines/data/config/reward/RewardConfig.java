package dev.loki.lomines.data.config.reward;

import dev.loki.lomines.data.config.block.BlockKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for mine rewards - drops when breaking specific blocks.
 */
public record RewardConfig(List<RewardEntry> entries) {

    public RewardConfig {
        entries = entries != null ? List.copyOf(entries) : List.of();
    }

    /**
     * Returns rewards applicable for the given block.
     */
    public List<RewardEntry> forBlock(BlockKey block) {
        return entries.stream()
                .filter(e -> e.blocks().contains(block))
                .toList();
    }

    /**
     * Whether any rewards are configured.
     */
    public boolean hasRewards() {
        return !entries.isEmpty();
    }

    /**
     * Empty reward config.
     */
    public static RewardConfig empty() {
        return new RewardConfig(List.of());
    }

    /**
     * Single entry reward config builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    // --- Reward Entry ---

    public record RewardEntry(
            List<BlockKey> blocks,      // Which blocks trigger this reward
            double chance,              // 0.0-100.0 chance percentage
            List<ItemReward> items,     // Item drops
            List<String> commands,      // Commands to execute (%player% placeholder)
            boolean preventVanillaDrops  // Cancel vanilla drops
    ) {
        public RewardEntry {
            blocks = blocks != null ? List.copyOf(blocks) : List.of();
            items = items != null ? List.copyOf(items) : List.of();
            commands = commands != null ? List.copyOf(commands) : List.of();

            if (chance < 0 || chance > 100) {
                throw new IllegalArgumentException("Chance must be between 0 and 100: " + chance);
            }
        }

        /**
         * Check if this reward applies to the given block.
         */
        public boolean appliesTo(BlockKey block) {
            return blocks.contains(block);
        }

        /**
         * Roll for this reward (0-100 random check).
         */
        public boolean roll(java.util.Random random) {
            return random.nextDouble() * 100.0 < chance;
        }

        public record ItemReward(
                Material material,
                int amount,
                String displayName,      // MiniMessage format
                List<String> lore        // MiniMessage format
        ) {
            public ItemReward {
                Objects.requireNonNull(material, "Material cannot be null");
                amount = Math.max(1, Math.min(amount, 64));
                lore = lore != null ? List.copyOf(lore) : List.of();
            }

            /**
             * Build ItemStack from this reward.
             */
            public ItemStack toItemStack() {
                ItemStack item = new ItemStack(material, amount);
                if (displayName != null || !lore.isEmpty()) {
                    item.editMeta(meta -> {
                        if (displayName != null) {
                            meta.displayName(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(displayName));
                        }
                        if (!lore.isEmpty()) {
                            var mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
                            meta.lore(lore.stream().map(mm::deserialize).toList());
                        }
                    });
                }
                return item;
            }
        }
    }

    // --- Builder ---

    public static class Builder {
        private final java.util.List<RewardEntry> entries = new java.util.ArrayList<>();

        public Builder add(RewardEntry entry) {
            entries.add(entry);
            return this;
        }

        public Builder add(double chance, List<BlockKey> blocks, List<RewardEntry.ItemReward> items) {
            entries.add(new RewardEntry(blocks, chance, items, List.of(), false));
            return this;
        }

        public Builder add(double chance, BlockKey block, RewardEntry.ItemReward item) {
            entries.add(new RewardEntry(List.of(block), chance, List.of(item), List.of(), false));
            return this;
        }

        public RewardConfig build() {
            return new RewardConfig(entries);
        }
    }
}
