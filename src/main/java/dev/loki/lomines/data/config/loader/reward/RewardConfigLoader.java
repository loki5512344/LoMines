package dev.loki.lomines.data.config.loader.reward;

import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.reward.RewardConfig.RewardEntry;
import dev.loki.lomines.data.config.reward.RewardConfig.RewardEntry.ItemReward;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader for rewards configuration section.
 */
public final class RewardConfigLoader {

    /**
     * Parses rewards from YAML configuration.
     */
    public RewardConfig parse(YamlConfiguration yaml) {
        List<RewardEntry> entries = new ArrayList<>();
        List<Map<?, ?>> rewardList = yaml.getMapList("rewards");

        if (rewardList.isEmpty()) {
            rewardList = yaml.getMapList("random-rewards"); // Legacy fallback
        }

        for (Map<?, ?> map : rewardList) {
            entries.add(parseRewardEntry(map));
        }

        return new RewardConfig(entries);
    }

    private RewardEntry parseRewardEntry(Map<?, ?> map) {
        double chance = ((Number) map.getOrDefault("chance", 0)).doubleValue();

        List<BlockKey> blocks = new ArrayList<>();
        List<?> blockList = (List<?>) map.get("blocks");
        if (blockList != null) {
            for (Object b : blockList) {
                blocks.add(BlockKey.deserialize(b.toString()));
            }
        }

        List<ItemReward> items = new ArrayList<>();
        List<?> itemList = (List<?>) map.get("items");
        if (itemList != null) {
            for (Object i : itemList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = (Map<String, Object>) i;
                items.add(parseItemReward(itemMap));
            }
        }

        @SuppressWarnings("unchecked")
        List<String> commands = (List<String>) map.getOrDefault("commands", List.of());
        boolean preventDrops = (Boolean) map.getOrDefault("prevent-drops", false);

        return new RewardEntry(blocks, chance, items, commands, preventDrops);
    }

    private ItemReward parseItemReward(Map<String, Object> map) {
        String typeStr = (String) map.get("type");
        Material material = Material.matchMaterial(typeStr);
        if (material == null) {
            material = Material.STONE;
        }

        int amount = ((Number) map.getOrDefault("amount", 1)).intValue();
        String name = (String) map.get("name");
        @SuppressWarnings("unchecked")
        List<String> lore = (List<String>) map.getOrDefault("lore", List.of());

        return new ItemReward(material, amount, name, lore);
    }

    /**
     * Saves rewards to YAML configuration.
     */
    public void save(YamlConfiguration yaml, RewardConfig rewards) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RewardEntry entry : rewards.entries()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("chance", entry.chance());
            map.put("blocks", entry.blocks().stream().map(BlockKey::serialize).toList());

            List<Map<String, Object>> items = new ArrayList<>();
            for (ItemReward item : entry.items()) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("type", item.material().name().toLowerCase());
                itemMap.put("amount", item.amount());
                if (item.displayName() != null) {
                    itemMap.put("name", item.displayName());
                }
                if (!item.lore().isEmpty()) {
                    itemMap.put("lore", item.lore());
                }
                items.add(itemMap);
            }
            map.put("items", items);
            map.put("commands", entry.commands());
            map.put("prevent-drops", entry.preventVanillaDrops());
            list.add(map);
        }
        yaml.set("rewards", list);
    }
}
