package dev.loki.lomines.data.reward.parse;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.data.reward.entity.Reward;
import dev.loki.lomines.data.reward.parse.command.RewardCommandParser;
import dev.loki.lomines.data.reward.parse.item.RewardItemParser;
import dev.loki.lomines.data.reward.parse.item.RewardMaterialParser;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RewardEntryParser {

    private final RewardItemParser itemParser;
    private final RewardMaterialParser materialParser;
    private final RewardCommandParser commandParser;

    public RewardEntryParser() {
        this.itemParser = new RewardItemParser();
        this.materialParser = new RewardMaterialParser();
        this.commandParser = new RewardCommandParser();
    }

    public Reward parseReward(Map<String, Object> rewardMap) throws ConfigParseException {
        double chance = parseRewardField(rewardMap, "chance", Double.class);
        boolean preventDrops = parseRewardField(rewardMap, "prevent-drops", Boolean.class, false);

        var materials = materialParser.parseMaterials(rewardMap);
        List<ItemStack> items = parseRewardItems(rewardMap);
        List<String> commands = commandParser.parseCommands(rewardMap);

        return new Reward(materials, chance, items, commands, preventDrops);
    }

    @SuppressWarnings("unchecked")
    public <T> T parseRewardField(Map<String, Object> map, String key, Class<T> type)
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
    public <T> T parseRewardField(Map<String, Object> map, String key, Class<T> type, T defaultValue) {
        if (!map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    public Double parseDoubleValue(String key, Object value) throws ConfigParseException {
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

    public List<ItemStack> parseRewardItems(Map<String, Object> rewardMap)
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
