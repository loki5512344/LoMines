package com.loki.lomines.data;

import com.loki.lomines.util.LocationParser;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses YAML configuration into intermediate data structures.
 * Responsible only for reading YAML and converting to Java objects.
 * Does not perform validation - that's handled by ConfigValidator.
 */
final class ConfigParser {

    /**
     * Parses selection coordinates from the YAML configuration.
     * Reads selection.1 through selection.10 fields.
     *
     * @param yaml the YAML configuration
     * @return list of parsed Location objects
     * @throws ConfigParseException if location parsing fails
     */
    List<Location> parseSelections(YamlConfiguration yaml) throws ConfigParseException {
        List<Location> selections = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String key = "selection." + i;
            if (yaml.contains(key)) {
                String locationString = yaml.getString(key);
                if (locationString == null || locationString.trim().isEmpty()) {
                    throw new ConfigParseException(
                        "Selection coordinate at '" + key + "' is empty"
                    );
                }

                try {
                    Location location = LocationParser.parse(locationString);
                    selections.add(location);
                } catch (ConfigParseException e) {
                    throw new ConfigParseException(
                        "Invalid selection coordinate at '" + key + "': " + e.getMessage(),
                        e
                    );
                }
            }
        }

        return selections;
    }

    /**
     * Parses block contents from the YAML configuration.
     * Supports vanilla blocks, Oraxen blocks (oraxen: prefix), and ItemsAdder blocks (itemsadder: prefix).
     *
     * @param yaml the YAML configuration
     * @return map of block string key to weight
     * @throws ConfigParseException if block configuration is invalid
     */
    Map<String, Double> parseBlocks(YamlConfiguration yaml) throws ConfigParseException {
        ConfigurationSection contentsSection = yaml.getConfigurationSection("contents");

        if (contentsSection == null) {
            throw new ConfigParseException(
                "Missing required 'contents' section in configuration"
            );
        }

        Map<String, Double> blocks = new HashMap<>();

        for (String key : contentsSection.getKeys(false)) {
            Object value = contentsSection.get(key);

            double weight = parseWeight(key, value);

            // Store block key (validation happens in ConfigValidator)
            String blockKey = normalizeBlockKey(key);
            blocks.put(blockKey, weight);
        }

        return blocks;
    }

    /**
     * Parses fill mode from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return the parsed FillMode
     */
    FillMode parseFillMode(YamlConfiguration yaml) {
        String raw = yaml.getString("fill-mode", "cuboid");
        if (raw == null || raw.isBlank()) {
            return FillMode.CUBOID;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "mask" -> FillMode.MASK;
            default -> FillMode.CUBOID;
        };
    }

    /**
     * Parses mask marker material from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return the mask marker material name
     */
    String parseMaskMarker(YamlConfiguration yaml) {
        String marker = yaml.getString("mask.marker", "pink_concrete");
        if (marker == null || marker.isBlank()) {
            return "pink_concrete";
        }
        return marker.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses mask positions from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return list of mask positions
     * @throws ConfigParseException if position parsing fails
     */
    List<Location> parseMaskPositions(YamlConfiguration yaml) throws ConfigParseException {
        List<String> rawList = yaml.getStringList("mask.positions");
        if (rawList == null || rawList.isEmpty()) {
            return List.of();
        }

        List<Location> out = new ArrayList<>();
        for (int i = 0; i < rawList.size(); i++) {
            String line = rawList.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            try {
                out.add(LocationParser.parse(line.trim()));
            } catch (ConfigParseException e) {
                throw new ConfigParseException(
                    "Invalid mask.positions entry at index " + i + ": " + e.getMessage(),
                    e
                );
            }
        }
        return out;
    }

    /**
     * Parses rewards from the YAML configuration.
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
     * Parses the teleport location from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return the parsed Location, or null if not configured
     * @throws ConfigParseException if the teleport location is invalid
     */
    Location parseTeleportLocation(YamlConfiguration yaml) throws ConfigParseException {
        if (!yaml.contains("teleport-location")) {
            return null;
        }

        String locationString = yaml.getString("teleport-location");
        if (locationString == null || locationString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocationParser.parse(locationString);
        } catch (ConfigParseException e) {
            throw new ConfigParseException(
                "Invalid teleport location: " + e.getMessage(),
                e
            );
        }
    }

    private double parseWeight(String key, Object value) throws ConfigParseException {
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                throw new ConfigParseException(
                    "Invalid weight type for block '" + key + "': expected number, got " +
                    (value != null ? value.getClass().getSimpleName() : "null")
                );
            }
        } catch (NumberFormatException e) {
            throw new ConfigParseException(
                "Invalid weight value for block '" + key + "': " + value,
                e
            );
        }
    }

    private String normalizeBlockKey(String key) {
        if (key.startsWith("oraxen:") || key.startsWith("itemsadder:")) {
            return key;
        }
        return key.toLowerCase();
    }

    private Reward parseReward(Map<String, Object> rewardMap) throws ConfigParseException {
        double chance = parseRewardField(rewardMap, "chance", Double.class);
        boolean preventDrops = parseRewardField(rewardMap, "prevent-drops", Boolean.class, false);

        List<org.bukkit.Material> materials = parseRewardMaterials(rewardMap);
        List<org.bukkit.inventory.ItemStack> items = parseRewardItems(rewardMap);
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

    private List<org.bukkit.Material> parseRewardMaterials(Map<String, Object> rewardMap)
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

        List<org.bukkit.Material> materials = new ArrayList<>();
        for (String blockName : blocksList) {
            if (blockName == null || blockName.trim().isEmpty()) {
                throw new ConfigParseException("Block name cannot be null or empty");
            }

            try {
                org.bukkit.Material material = org.bukkit.Material.valueOf(blockName.toUpperCase());
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

    private List<org.bukkit.inventory.ItemStack> parseRewardItems(Map<String, Object> rewardMap)
            throws ConfigParseException {
        List<org.bukkit.inventory.ItemStack> items = new ArrayList<>();

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

            org.bukkit.inventory.ItemStack item = parseRewardItem(itemMap, i);
            items.add(item);
        }

        return items;
    }

    private org.bukkit.inventory.ItemStack parseRewardItem(Map<String, Object> itemMap, int index)
            throws ConfigParseException {
        if (!itemMap.containsKey("type")) {
            throw new ConfigParseException("Missing required 'type' field in item at index " + index);
        }

        String typeName = String.valueOf(itemMap.get("type"));
        org.bukkit.Material material;
        try {
            material = org.bukkit.Material.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException(
                "Unknown material in item type: '" + typeName + "'",
                e
            );
        }

        int amount = parseItemAmount(itemMap);

        return new org.bukkit.inventory.ItemStack(material, amount);
    }

    private int parseItemAmount(Map<String, Object> itemMap) throws ConfigParseException {
        if (!itemMap.containsKey("amount")) {
            return 1;
        }

        Object amountObj = itemMap.get("amount");
        try {
            if (amountObj instanceof Number) {
                return ((Number) amountObj).intValue();
            } else if (amountObj instanceof String) {
                return Integer.parseInt((String) amountObj);
            } else {
                throw new ConfigParseException(
                    "Invalid 'amount' type: expected number, got " +
                    (amountObj != null ? amountObj.getClass().getSimpleName() : "null")
                );
            }
        } catch (NumberFormatException e) {
            throw new ConfigParseException("Invalid 'amount' value: " + amountObj, e);
        }
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
