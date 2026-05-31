package dev.loki.lomines.data.reward;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses reward configurations from YAML.
 * Handles reward chance, materials, items, commands, and prevent-drops settings.
 */
public final class RewardParser {

    private final RewardItemParser itemParser;
    private final RewardMaterialParser materialParser;
    private final RewardCommandParser commandParser;

    public RewardParser() {
        this.itemParser = new RewardItemParser();
        this.materialParser = new RewardMaterialParser();
        this.commandParser = new RewardCommandParser();
    }

    public List<Reward> parseRewards(YamlConfiguration yaml) throws ConfigParseException {
        List<Reward> rewards = new ArrayList<>();

        if (!yaml.contains("random-rewards")) {
            return rewards;
        }

        List<?> rewardsList = yaml.getList("random-rewards");
        if (rewardsList == null) {
            return rewards;
        }

        for (int i = 0; i < rewardsList.size(); i++) {
            Object rewardObj = rewardsList.get(i);

            if (!(rewardObj instanceof Map)) {
                throw new ConfigParseException(
                        "Invalid reward at index " + i + ": expected map, got " +
                                (rewardObj != null ? rewardObj.getClass().getSimpleName() : "null")
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> rewardMap = (Map<String, Object>) rewardObj;

            try {
                Reward reward = parseReward(rewardMap);
                rewards.add(reward);
            } catch (ConfigParseException e) {
                throw new ConfigParseException(
                        "Error parsing reward at index " + i + ": " + e.getMessage(),
                        e
                );
            }
        }

        return rewards;
    }

    private Reward parseReward(Map<String, Object> rewardMap) throws ConfigParseException {
        double chance = parseRewardField(rewardMap, "chance", Double.class);
        boolean preventDrops = parseRewardField(rewardMap, "prevent-drops", Boolean.class, false);

        var materials = materialParser.parseMaterials(rewardMap);
        List<ItemStack> items = parseRewardItems(rewardMap);
        List<String> commands = commandParser.parseCommands(rewardMap);

        return new Reward(materials, chance, items, commands, preventDrops);
    }

    @SuppressWarnings("unchecked")
    private <T> T parseRewardField(Map<String, Object> map, String key, Class<T> type)
            throws ConfigParseException {
        if (!map.containsKey(key)) {
            throw new ConfigParseException("Missing required '" + key + "' field in reward");
        }
        Object value = map.get(key);
        if (type == Double.class) {
            return (T) parseDoubleValue(key, value);
        } else if (type == Boolean.class) {
            if (!(value instanceof Boolean)) {
                throw new ConfigParseException(
                        "Invalid '" + key + "' value: expected boolean, got " +
                                (value != null ? value.getClass().getSimpleName() : "null")
                );
            }
            return (T) value;
        }
        throw new ConfigParseException("Unsupported type: " + type);
    }

    @SuppressWarnings("unchecked")
    private <T> T parseRewardField(Map<String, Object> map, String key, Class<T> type, T defaultValue) {
        if (!map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    private Double parseDoubleValue(String key, Object value) throws ConfigParseException {
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                throw new ConfigParseException(
                        "Invalid '" + key + "' type: expected number, got " +
                                (value != null ? value.getClass().getSimpleName() : "null")
                );
            }
        } catch (NumberFormatException e) {
            throw new ConfigParseException("Invalid '" + key + "' value: " + value, e);
        }
    }

    private List<ItemStack> parseRewardItems(Map<String, Object> rewardMap)
            throws ConfigParseException {
        List<ItemStack> items = new ArrayList<>();
        if (!rewardMap.containsKey("items")) {
            return items;
        }
        Object itemsObj = rewardMap.get("items");
        if (!(itemsObj instanceof List<?> itemsList)) {
            throw new ConfigParseException(
                    "Invalid 'items' type: expected list, got " +
                            (itemsObj != null ? itemsObj.getClass().getSimpleName() : "null")
            );
        }
        for (int i = 0; i < itemsList.size(); i++) {
            Object itemObj = itemsList.get(i);
            if (!(itemObj instanceof Map)) {
                throw new ConfigParseException(
                        "Invalid item at index " + i + ": expected map, got " +
                                (itemObj != null ? itemObj.getClass().getSimpleName() : "null")
                );
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) itemObj;
            ItemStack item = itemParser.parseItem(itemMap, i);
            items.add(item);
        }
        return items;
    }
}
