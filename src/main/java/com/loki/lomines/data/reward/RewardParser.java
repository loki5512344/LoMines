package com.loki.lomines.data.reward;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses reward configurations from YAML.
 * Handles reward chance, materials, items, commands, and prevent-drops settings.
 */
final class RewardParser {

    private final RewardItemParser itemParser;

    RewardParser() {
        this.itemParser = new RewardItemParser();
    }

    /**
     * Parses all rewards from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return list of parsed Reward objects
     * @throws ConfigParseException if reward configuration is invalid
     */
    List<Reward> parseRewards(YamlConfiguration yaml) throws ConfigParseException {
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

    /**
     * Parses a single reward from a map configuration.
     *
     * @param rewardMap the reward configuration map
     * @return the parsed Reward object
     * @throws ConfigParseException if the reward configuration is invalid
     */
    private Reward parseReward(Map<String, Object> rewardMap) throws ConfigParseException {
        double chance = parseRewardField(rewardMap, "chance", Double.class);
        boolean preventDrops = parseRewardField(rewardMap, "prevent-drops", Boolean.class, false);

        List<Material> materials = parseRewardMaterials(rewardMap);
        List<ItemStack> items = parseRewardItems(rewardMap);
        List<String> commands = parseRewardCommands(rewardMap);

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

    private List<Material> parseRewardMaterials(Map<String, Object> rewardMap)
            throws ConfigParseException {
        if (!rewardMap.containsKey("blocks")) {
            throw new ConfigParseException("Missing required 'blocks' field in reward");
        }
        Object blocksObj = rewardMap.get("blocks");
        if (!(blocksObj instanceof List)) {
            throw new ConfigParseException(
                "Invalid 'blocks' type: expected list, got " +
                (blocksObj != null ? blocksObj.getClass().getSimpleName() : "null")
            );
        }
        @SuppressWarnings("unchecked")
        List<String> blocksList = (List<String>) blocksObj;
        List<Material> materials = new ArrayList<>();
        for (String blockName : blocksList) {
            if (blockName == null || blockName.trim().isEmpty()) {
                throw new ConfigParseException("Block name cannot be null or empty");
            }
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                materials.add(material);
            } catch (IllegalArgumentException e) {
                throw new ConfigParseException(
                    "Unknown material in reward blocks: '" + blockName + "'",
                    e
                );
            }
        }
        return materials;
    }

    private List<ItemStack> parseRewardItems(Map<String, Object> rewardMap)
            throws ConfigParseException {
        List<ItemStack> items = new ArrayList<>();
        if (!rewardMap.containsKey("items")) {
            return items;
        }
        Object itemsObj = rewardMap.get("items");
        if (!(itemsObj instanceof List)) {
            throw new ConfigParseException(
                "Invalid 'items' type: expected list, got " +
                (itemsObj != null ? itemsObj.getClass().getSimpleName() : "null")
            );
        }
        List<?> itemsList = (List<?>) itemsObj;
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

    private List<String> parseRewardCommands(Map<String, Object> rewardMap)
            throws ConfigParseException {
        List<String> commands = new ArrayList<>();
        if (!rewardMap.containsKey("commands")) {
            return commands;
        }
        Object commandsObj = rewardMap.get("commands");
        if (!(commandsObj instanceof List)) {
            throw new ConfigParseException(
                "Invalid 'commands' type: expected list, got " +
                (commandsObj != null ? commandsObj.getClass().getSimpleName() : "null")
            );
        }
        @SuppressWarnings("unchecked")
        List<String> commandsList = (List<String>) commandsObj;
        for (String command : commandsList) {
            if (command == null || command.trim().isEmpty()) {
                throw new ConfigParseException("Command cannot be null or empty");
            }
            commands.add(command);
        }
        return commands;
    }
}
