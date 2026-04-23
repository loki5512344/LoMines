package com.loki.lomines.data;

import com.loki.lomines.util.LocationParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads MineConfig objects from YAML configuration files.
 * Validates all fields and throws ConfigParseException for invalid data.
 */
public final class ConfigLoader {
    
    public ConfigLoader() {
    }
    
    /**
     * Loads a MineConfig from a YAML file.
     *
     * @param configFile the path to the YAML configuration file
     * @return the loaded MineConfig object
     * @throws IOException if the file cannot be read
     * @throws ConfigParseException if the configuration is invalid
     */
    public MineConfig load(Path configFile) throws IOException, ConfigParseException {
        if (configFile == null) {
            throw new ConfigParseException("Config file path cannot be null");
        }
        
        if (!configFile.toFile().exists()) {
            throw new IOException("Config file does not exist: " + configFile);
        }
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile.toFile());
        
        FillMode fillMode = parseFillMode(yaml);
        return MineConfig.builder()
            .selections(parseSelections(yaml))
            .blocks(parseBlocks(yaml))
            .fillMode(fillMode)
            .maskMarkerMaterial(parseMaskMarker(yaml))
            .maskPositions(parseMaskPositions(yaml))
            .rewards(parseRewards(yaml))
            .resetTicks(yaml.getInt("reset.ticks", 6000))
            .resetPercent(yaml.getDouble("reset.percent", 10.0))
            .resetOnPercentEnabled(yaml.getBoolean("reset-on-percent", false))
            .actionBarEnabled(yaml.getBoolean("actionbar.enabled", true))
            .actionBarMessage(yaml.getString("actionbar.message", ""))
            .actionBarRange(yaml.getDouble("actionbar.range", 50.0))
            .timerFormat(yaml.getString("timer-format", "mm:ss"))
            .teleportOnReset(yaml.getBoolean("teleport-on-reset", false))
            .teleportLocation(parseTeleportLocation(yaml))
            .resetCommands(yaml.getStringList("reset-commands"))
            .broadcastReset(yaml.getString("broadcast-reset", ""))
            .build();
    }
    
    /**
     * Parses selection coordinates from the YAML configuration.
     * Reads selection.1 through selection.10 fields.
     *
     * @param yaml the YAML configuration
     * @return list of parsed Location objects
     * @throws ConfigParseException if any selection coordinate is invalid
     */
    private List<Location> parseSelections(YamlConfiguration yaml) throws ConfigParseException {
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
        
        if (selections.isEmpty()) {
            throw new ConfigParseException(
                "No selection coordinates found. At least one selection pair (2 points) is required"
            );
        }
        
        if (selections.size() % 2 != 0) {
            throw new ConfigParseException(
                "Invalid number of selection coordinates: " + selections.size() + 
                ". Selections must be in pairs (even number)"
            );
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
    private Map<String, Double> parseBlocks(YamlConfiguration yaml) throws ConfigParseException {
        ConfigurationSection contentsSection = yaml.getConfigurationSection("contents");
        
        if (contentsSection == null) {
            throw new ConfigParseException(
                "Missing required 'contents' section in configuration"
            );
        }
        
        Map<String, Double> blocks = new HashMap<>();
        
        for (String key : contentsSection.getKeys(false)) {
            Object value = contentsSection.get(key);
            
            double weight;
            try {
                if (value instanceof Number) {
                    weight = ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    weight = Double.parseDouble((String) value);
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
            
            if (weight <= 0) {
                throw new ConfigParseException(
                    "Block weight must be positive for '" + key + "', got: " + weight
                );
            }
            
            // Validate block key format
            if (key.startsWith("oraxen:") || key.startsWith("itemsadder:")) {
                // Custom blocks - just store the string key
                blocks.put(key, weight);
            } else {
                // Vanilla block - validate material exists
                try {
                    Material.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ConfigParseException(
                        "Unknown material: '" + key + "'. Must be a valid Minecraft material name",
                        e
                    );
                }
                
                // Store the lowercase material name as key
                blocks.put(key.toLowerCase(), weight);
            }
        }
        
        if (blocks.isEmpty()) {
            throw new ConfigParseException(
                "No blocks defined in 'contents' section. At least one block is required"
            );
        }
        
        return blocks;
    }
    
    private FillMode parseFillMode(YamlConfiguration yaml) {
        String raw = yaml.getString("fill-mode", "cuboid");
        if (raw == null || raw.isBlank()) {
            return FillMode.CUBOID;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "mask" -> FillMode.MASK;
            default -> FillMode.CUBOID;
        };
    }
    
    private String parseMaskMarker(YamlConfiguration yaml) throws ConfigParseException {
        String marker = yaml.getString("mask.marker", "pink_concrete");
        if (marker == null || marker.isBlank()) {
            return "pink_concrete";
        }
        try {
            Material.valueOf(marker.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException("Invalid mask.marker material: '" + marker + "'", e);
        }
        return marker.trim().toLowerCase(Locale.ROOT);
    }
    
    private List<Location> parseMaskPositions(YamlConfiguration yaml) throws ConfigParseException {
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
                throw new ConfigParseException("Invalid mask.positions entry at index " + i + ": " + e.getMessage(), e);
            }
        }
        return out;
    }

    /**
     * Parses rewards from the YAML configuration.
     * Reads the random-rewards list and creates Reward objects.
     *
     * @param yaml the YAML configuration
     * @return list of parsed Reward objects
     * @throws ConfigParseException if reward configuration is invalid
     */
    private List<Reward> parseRewards(YamlConfiguration yaml) throws ConfigParseException {
        List<Reward> rewards = new ArrayList<>();
        
        if (!yaml.contains("random-rewards")) {
            return rewards; // Rewards are optional
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
                Reward reward = parseReward(rewardMap, i);
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
     * Parses a single reward from a map.
     *
     * @param rewardMap the reward configuration map
     * @param index the index of the reward (for error messages)
     * @return the parsed Reward object
     * @throws ConfigParseException if the reward configuration is invalid
     */
    private Reward parseReward(Map<String, Object> rewardMap, int index) throws ConfigParseException {
        // Parse chance
        double chance = parseRewardChance(rewardMap, index);
        
        // Parse prevent-drops
        boolean preventDrops = false;
        if (rewardMap.containsKey("prevent-drops")) {
            Object preventDropsObj = rewardMap.get("prevent-drops");
            if (preventDropsObj instanceof Boolean) {
                preventDrops = (Boolean) preventDropsObj;
            } else {
                throw new ConfigParseException(
                    "Invalid 'prevent-drops' value: expected boolean, got " + 
                    (preventDropsObj != null ? preventDropsObj.getClass().getSimpleName() : "null")
                );
            }
        }
        
        // Parse blocks (materials)
        List<Material> materials = parseRewardMaterials(rewardMap, index);
        
        // Parse items
        List<ItemStack> items = parseRewardItems(rewardMap, index);
        
        // Parse commands
        List<String> commands = parseRewardCommands(rewardMap, index);
        
        return new Reward(materials, chance, items, commands, preventDrops);
    }
    
    /**
     * Parses the chance field from a reward map.
     */
    private double parseRewardChance(Map<String, Object> rewardMap, int index) throws ConfigParseException {
        if (!rewardMap.containsKey("chance")) {
            throw new ConfigParseException("Missing required 'chance' field in reward");
        }
        
        Object chanceObj = rewardMap.get("chance");
        double chance;
        
        try {
            if (chanceObj instanceof Number) {
                chance = ((Number) chanceObj).doubleValue();
            } else if (chanceObj instanceof String) {
                chance = Double.parseDouble((String) chanceObj);
            } else {
                throw new ConfigParseException(
                    "Invalid 'chance' type: expected number, got " + 
                    (chanceObj != null ? chanceObj.getClass().getSimpleName() : "null")
                );
            }
        } catch (NumberFormatException e) {
            throw new ConfigParseException(
                "Invalid 'chance' value: " + chanceObj,
                e
            );
        }
        
        if (chance < 0 || chance > 100) {
            throw new ConfigParseException(
                "Reward chance must be between 0 and 100, got: " + chance
            );
        }
        
        return chance;
    }
    
    /**
     * Parses the blocks (materials) field from a reward map.
     */
    private List<Material> parseRewardMaterials(Map<String, Object> rewardMap, int index) 
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
        
        if (blocksList.isEmpty()) {
            throw new ConfigParseException("Reward 'blocks' list cannot be empty");
        }
        
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
    
    /**
     * Parses the items field from a reward map.
     */
    private List<ItemStack> parseRewardItems(Map<String, Object> rewardMap, int index) 
            throws ConfigParseException {
        List<ItemStack> items = new ArrayList<>();
        
        if (!rewardMap.containsKey("items")) {
            return items; // Items are optional
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
            
            ItemStack item = parseRewardItem(itemMap, i);
            items.add(item);
        }
        
        return items;
    }

    /**
     * Parses a single item from a map.
     */
    private ItemStack parseRewardItem(Map<String, Object> itemMap, int index) 
            throws ConfigParseException {
        // Parse type (material)
        if (!itemMap.containsKey("type")) {
            throw new ConfigParseException("Missing required 'type' field in item at index " + index);
        }
        
        String typeName = String.valueOf(itemMap.get("type"));
        Material material;
        try {
            material = Material.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException(
                "Unknown material in item type: '" + typeName + "'",
                e
            );
        }
        
        // Parse amount
        int amount = 1;
        if (itemMap.containsKey("amount")) {
            Object amountObj = itemMap.get("amount");
            try {
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).intValue();
                } else if (amountObj instanceof String) {
                    amount = Integer.parseInt((String) amountObj);
                } else {
                    throw new ConfigParseException(
                        "Invalid 'amount' type: expected number, got " + 
                        (amountObj != null ? amountObj.getClass().getSimpleName() : "null")
                    );
                }
            } catch (NumberFormatException e) {
                throw new ConfigParseException(
                    "Invalid 'amount' value: " + amountObj,
                    e
                );
            }
            
            if (amount <= 0) {
                throw new ConfigParseException(
                    "Item amount must be positive, got: " + amount
                );
            }
        }
        
        ItemStack item = new ItemStack(material, amount);
        
        // Note: name and lore parsing would require ItemMeta manipulation
        // For now, we create a basic ItemStack
        // Full implementation with name/lore can be added later if needed
        
        return item;
    }
    
    /**
     * Parses the commands field from a reward map.
     */
    private List<String> parseRewardCommands(Map<String, Object> rewardMap, int index) 
            throws ConfigParseException {
        List<String> commands = new ArrayList<>();
        
        if (!rewardMap.containsKey("commands")) {
            return commands; // Commands are optional
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
    
    /**
     * Parses the teleport location from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return the parsed Location, or null if not configured
     * @throws ConfigParseException if the teleport location is invalid
     */
    private Location parseTeleportLocation(YamlConfiguration yaml) throws ConfigParseException {
        if (!yaml.contains("teleport-location")) {
            return null; // Teleport location is optional
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


    /**
     * Saves a MineConfig to a YAML file.
     *
     * @param config the MineConfig to save
     * @param configFile the path to the YAML configuration file
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if config or configFile is null
     */
    public void save(MineConfig config, Path configFile) throws IOException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (configFile == null) {
            throw new IllegalArgumentException("Config file path cannot be null");
        }

        YamlConfiguration yaml = new YamlConfiguration();

        // Serialize all sections
        saveSelections(yaml, config.getSelections());
        saveBlocks(yaml, config.getBlocks());
        yaml.set("fill-mode", config.getFillMode().name().toLowerCase(Locale.ROOT));
        yaml.set("mask.marker", config.getMaskMarkerMaterial());
        saveMaskPositions(yaml, config.getMaskPositions());
        saveRewards(yaml, config.getRewards());

        // Save reset settings
        yaml.set("reset.ticks", config.getResetTicks());
        yaml.set("reset.percent", config.getResetPercent());
        yaml.set("reset-on-percent", config.isResetOnPercentEnabled());
        yaml.set("reset-commands", config.getResetCommands());
        yaml.set("broadcast-reset", config.getBroadcastReset());

        // Save teleport settings
        yaml.set("teleport-on-reset", config.isTeleportOnReset());
        if (config.getTeleportLocation() != null) {
            yaml.set("teleport-location", LocationParser.format(config.getTeleportLocation()));
        }

        // Save action bar settings
        yaml.set("actionbar.enabled", config.isActionBarEnabled());
        yaml.set("actionbar.message", config.getActionBarMessage());
        yaml.set("actionbar.range", config.getActionBarRange());

        // Save timer format
        yaml.set("timer-format", config.getTimerFormat());

        // Save to file
        yaml.save(configFile.toFile());
    }

    /**
     * Saves selection coordinates to the YAML configuration.
     * Writes selection.1 through selection.N fields using LocationParser.format().
     *
     * @param yaml the YAML configuration
     * @param selections the list of selection locations
     */
    private void saveSelections(YamlConfiguration yaml, List<Location> selections) {
        for (int i = 0; i < selections.size(); i++) {
            Location location = selections.get(i);
            String key = "selection." + (i + 1);
            yaml.set(key, LocationParser.format(location));
        }
    }

    /**
     * Saves block contents to the YAML configuration.
     * Writes the contents section with block keys and weights.
     *
     * @param yaml the YAML configuration
     * @param blocks the map of block string key to weight
     */
    private void saveBlocks(YamlConfiguration yaml, Map<String, Double> blocks) {
        for (Map.Entry<String, Double> entry : blocks.entrySet()) {
            String blockKey = entry.getKey();
            Double weight = entry.getValue();

            // Store the block key as-is (already in correct format)
            yaml.set("contents." + blockKey, weight);
        }
    }
    
    private void saveMaskPositions(YamlConfiguration yaml, List<Location> maskPositions) {
        if (maskPositions == null || maskPositions.isEmpty()) {
            yaml.set("mask.positions", new ArrayList<String>());
            return;
        }
        List<String> lines = new ArrayList<>();
        for (Location loc : maskPositions) {
            lines.add(LocationParser.format(loc));
        }
        yaml.set("mask.positions", lines);
    }

    /**
     * Saves rewards to the YAML configuration.
     * Writes the random-rewards list with all reward properties.
     *
     * @param yaml the YAML configuration
     * @param rewards the list of rewards
     */
    private void saveRewards(YamlConfiguration yaml, List<Reward> rewards) {
        if (rewards.isEmpty()) {
            return; // Don't write empty rewards section
        }

        List<Map<String, Object>> rewardsList = new ArrayList<>();

        for (Reward reward : rewards) {
            Map<String, Object> rewardMap = new HashMap<>();

            // Save chance
            rewardMap.put("chance", reward.getChance());

            // Save prevent-drops
            rewardMap.put("prevent-drops", reward.isPreventDrops());

            // Save blocks (materials)
            List<String> blockNames = new ArrayList<>();
            for (Material material : reward.getMaterials()) {
                blockNames.add(material.name().toLowerCase());
            }
            rewardMap.put("blocks", blockNames);

            // Save items
            if (!reward.getItems().isEmpty()) {
                List<Map<String, Object>> itemsList = new ArrayList<>();
                for (ItemStack item : reward.getItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("type", item.getType().name().toLowerCase());
                    itemMap.put("amount", item.getAmount());
                    itemsList.add(itemMap);
                }
                rewardMap.put("items", itemsList);
            }

            // Save commands
            if (!reward.getCommands().isEmpty()) {
                rewardMap.put("commands", new ArrayList<>(reward.getCommands()));
            }

            rewardsList.add(rewardMap);
        }

        yaml.set("random-rewards", rewardsList);
    }

}
