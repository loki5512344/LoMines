package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.reward.RewardConfig.RewardEntry;
import dev.loki.lomines.data.config.reward.RewardConfig.RewardEntry.ItemReward;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.util.location.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/**
 * Clean config loader for section-based configuration.
 * Supports YAML inheritance from defaults.
 */
public final class ConfigLoader {

    private final Path dataFolder;
    private YamlConfiguration defaults;

    public ConfigLoader(Path dataFolder) {
        this.dataFolder = dataFolder;
        loadDefaults();
    }

    /**
     * Load or create defaults.yml.
     */
    private void loadDefaults() {
        Path defaultsPath = dataFolder.resolve("mines").resolve("_defaults.yml");
        defaults = new YamlConfiguration();

        if (Files.exists(defaultsPath)) {
            defaults = YamlConfiguration.loadConfiguration(defaultsPath.toFile());
        } else {
            // Create defaults with sensible values
            setDefaults(defaults);
            try {
                Files.createDirectories(defaultsPath.getParent());
                defaults.save(defaultsPath.toFile());
            } catch (IOException e) {
                // Ignore, use in-memory defaults
            }
        }
    }

    private void setDefaults(YamlConfiguration yaml) {
        yaml.set("reset.interval", "5m");
        yaml.set("reset.percent-trigger", 10.0);
        yaml.set("reset.percent-enabled", false);
        yaml.set("reset.commands", List.of());
        yaml.set("reset.broadcast", "");

        yaml.set("ui.actionbar.enabled", true);
        yaml.set("ui.actionbar.format", "<green>{mine}</green> <gray>{percent}%</gray> <dark_gray>({time})");
        yaml.set("ui.actionbar.range", 50.0);
        yaml.set("ui.timer-format", "mm:ss");

        yaml.set("teleport.enabled", false);

        yaml.set("rewards", List.of());
    }

    /**
     * Load a mine configuration from file.
     */
    public MineConfig load(String mineName) throws ConfigLoadException {
        Path configPath = dataFolder.resolve("mines").resolve(mineName + ".yml");

        if (!Files.exists(configPath)) {
            throw new ConfigLoadException("Mine not found: " + mineName);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configPath.toFile());

        // Merge with defaults (YAML inheritance)
        mergeDefaults(yaml);

        try {
            return MineConfig.builder(mineName)
                    .region(parseRegion(yaml))
                    .blocks(parseBlocks(yaml))
                    .reset(parseReset(yaml))
                    .rewards(parseRewards(yaml))
                    .teleport(parseTeleport(yaml))
                    .ui(parseUI(yaml))
                    .build();
        } catch (Exception e) {
            throw new ConfigLoadException("Failed to load mine '" + mineName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Save a mine configuration to file.
     */
    public void save(MineConfig config) throws ConfigLoadException {
        Path configPath = dataFolder.resolve("mines").resolve(config.name() + ".yml");

        YamlConfiguration yaml = new YamlConfiguration();

        // Save regions
        saveRegion(yaml, config.region());

        // Save blocks
        saveBlocks(yaml, config.blocks());

        // Save reset
        saveReset(yaml, config.reset());

        // Save rewards
        saveRewards(yaml, config.rewards());

        // Save teleport
        saveTeleport(yaml, config.teleport());

        // Save UI
        saveUI(yaml, config.ui());

        try {
            Files.createDirectories(configPath.getParent());
            yaml.save(configPath.toFile());
        } catch (IOException e) {
            throw new ConfigLoadException("Failed to save mine: " + e.getMessage(), e);
        }
    }

    // --- Parsers ---

    private RegionConfig parseRegion(YamlConfiguration yaml) {
        List<Location> selections = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String key = "region.selection." + i;
            if (yaml.contains(key)) {
                String locStr = yaml.getString(key);
                if (locStr != null && !locStr.isBlank()) {
                    selections.add(parseLocation(locStr));
                }
            }
        }

        if (selections.isEmpty()) {
            // Legacy fallback
            for (int i = 1; i <= 10; i++) {
                String key = "selection." + i;
                if (yaml.contains(key)) {
                    String locStr = yaml.getString(key);
                    if (locStr != null && !locStr.isBlank()) {
                        selections.add(parseLocation(locStr));
                    }
                }
            }
        }

        if (selections.size() < 2) {
            throw new IllegalArgumentException("Mine must have at least 2 selection points (1 region)");
        }

        return RegionConfig.fromSelections(selections);
    }

    private BlockConfig parseBlocks(YamlConfiguration yaml) {
        ConfigurationSection contents = yaml.getConfigurationSection("blocks.contents");
        if (contents == null) {
            contents = yaml.getConfigurationSection("contents"); // Legacy fallback
        }

        if (contents == null) {
            throw new IllegalArgumentException("Missing 'blocks.contents' section");
        }

        Map<BlockKey, Double> weights = new HashMap<>();
        for (String key : contents.getKeys(false)) {
            double weight = contents.getDouble(key);
            if (weight <= 0) continue;
            weights.put(BlockKey.deserialize(key), weight);
        }

        FillMode fillMode = FillMode.valueOf(
                yaml.getString("blocks.fill-mode", "CUBOID").toUpperCase()
        );

        // Parse mask config if applicable
        BlockConfig.MaskConfig mask = null;
        if (fillMode == FillMode.MASK) {
            String markerStr = yaml.getString("blocks.mask.marker", "pink_concrete");
            BlockKey marker = BlockKey.deserialize(markerStr);

            Map<String, Boolean> positions = new HashMap<>();
            List<String> posList = yaml.getStringList("blocks.mask.positions");
            for (String pos : posList) {
                positions.put(pos, true);
            }

            mask = new BlockConfig.MaskConfig(marker, positions);
        }

        return new BlockConfig(weights, fillMode, mask);
    }

    private ResetConfig parseReset(YamlConfiguration yaml) {
        String intervalStr = yaml.getString("reset.interval", defaults.getString("reset.interval", "5m"));

        return ResetConfig.builder()
                .interval(intervalStr)
                .percentTrigger(yaml.getDouble("reset.percent-trigger", defaults.getDouble("reset.percent-trigger", 10.0)))
                .percentEnabled(yaml.getBoolean("reset.percent-enabled", defaults.getBoolean("reset.percent-enabled", false)))
                .commands(yaml.getStringList("reset.commands"))
                .broadcastMessage(yaml.getString("reset.broadcast", ""))
                .build();
    }

    private RewardConfig parseRewards(YamlConfiguration yaml) {
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

    private TeleportConfig parseTeleport(YamlConfiguration yaml) {
        boolean enabled = yaml.getBoolean("teleport.enabled", false);
        if (!enabled) {
            return TeleportConfig.disabled();
        }

        String locStr = yaml.getString("teleport.location");
        if (locStr == null) {
            return TeleportConfig.disabled();
        }

        return TeleportConfig.at(parseLocation(locStr));
    }

    private UIConfig parseUI(YamlConfiguration yaml) {
        boolean actionBarEnabled = yaml.getBoolean("ui.actionbar.enabled",
                defaults.getBoolean("ui.actionbar.enabled", true));

        return new UIConfig(
                actionBarEnabled,
                yaml.getString("ui.actionbar.format", UIConfig.DEFAULT_ACTIONBAR_FORMAT),
                yaml.getDouble("ui.actionbar.range", 50.0),
                yaml.getString("ui.timer-format", UIConfig.DEFAULT_TIMER_FORMAT)
        );
    }

    // --- Savers ---

    private void saveRegion(YamlConfiguration yaml, RegionConfig region) {
        int i = 1;
        for (Cuboid cuboid : region.regions()) {
            yaml.set("region.selection." + i, LocationParser.format(cuboid.getMin()));
            i++;
            yaml.set("region.selection." + i, LocationParser.format(cuboid.getMax()));
            i++;
        }
    }

    private void saveBlocks(YamlConfiguration yaml, BlockConfig blocks) {
        for (var entry : blocks.weights().entrySet()) {
            yaml.set("blocks.contents." + entry.getKey().serialize(), entry.getValue());
        }
        yaml.set("blocks.fill-mode", blocks.fillMode().name().toLowerCase());

        if (blocks.fillMode() == FillMode.MASK && blocks.mask() != null) {
            yaml.set("blocks.mask.marker", blocks.mask().marker().serialize());
            yaml.set("blocks.mask.positions", new ArrayList<>(blocks.mask().positions().keySet()));
        }
    }

    private void saveReset(YamlConfiguration yaml, ResetConfig reset) {
        yaml.set("reset.interval", reset.intervalDisplay());
        yaml.set("reset.percent-trigger", reset.percentTrigger());
        yaml.set("reset.percent-enabled", reset.percentEnabled());
        yaml.set("reset.commands", reset.commands());
        yaml.set("reset.broadcast", reset.broadcastMessage());
    }

    private void saveRewards(YamlConfiguration yaml, RewardConfig rewards) {
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

    private void saveTeleport(YamlConfiguration yaml, TeleportConfig teleport) {
        yaml.set("teleport.enabled", teleport.enabled());
        teleport.getLocation().ifPresent(loc ->
                yaml.set("teleport.location", LocationParser.format(loc)));
    }

    private void saveUI(YamlConfiguration yaml, UIConfig ui) {
        yaml.set("ui.actionbar.enabled", ui.actionBarEnabled());
        yaml.set("ui.actionbar.format", ui.actionBarFormat());
        yaml.set("ui.actionbar.range", ui.actionBarRange());
        yaml.set("ui.timer-format", ui.timerFormat());
    }

    // --- Helpers ---

    private Location parseLocation(String str) {
        String[] parts = str.split(";");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid location format: " + str);
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            throw new IllegalArgumentException("Unknown world: " + parts[0]);
        }

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void mergeDefaults(YamlConfiguration yaml) {
        // Simple shallow merge - could be enhanced for deep merging
        for (String key : defaults.getKeys(true)) {
            if (!yaml.contains(key)) {
                yaml.set(key, defaults.get(key));
            }
        }
    }

    /**
     * Exception for config loading errors.
     */
    public static class ConfigLoadException extends Exception {
        public ConfigLoadException(String message) {
            super(message);
        }
        public ConfigLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
