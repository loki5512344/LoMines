package dev.loki.lomines.data.config.parser;

import dev.loki.lomines.data.config.FillMode;
import dev.loki.lomines.data.reward.Reward;
import dev.loki.lomines.data.reward.RewardParser;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

/**
 * Parses YAML configuration into intermediate data structures.
 * Responsible only for reading YAML and converting to Java objects.
 * Does not perform validation - that's handled by ConfigValidator.
 */
public final class ConfigParser {

    private final RewardParser rewardParser;

    public ConfigParser(RewardParser rewardParser) {
        this.rewardParser = rewardParser;
    }

    /**
     * Parses selection coordinates from the YAML configuration.
     * Reads selection.1 through selection.10 fields.
     *
     * @param yaml the YAML configuration
     * @return list of parsed Location objects
     * @throws ConfigParseException if location parsing fails
     */
    public List<Location> parseSelections(YamlConfiguration yaml) throws ConfigParseException {
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
    public Map<String, Double> parseBlocks(YamlConfiguration yaml) throws ConfigParseException {
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
    public FillMode parseFillMode(YamlConfiguration yaml) {
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
    public String parseMaskMarker(YamlConfiguration yaml) {
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
    public List<Location> parseMaskPositions(YamlConfiguration yaml) throws ConfigParseException {
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
     * Delegates to RewardParser for actual parsing logic.
     *
     * @param yaml the YAML configuration
     * @return list of parsed Reward objects
     * @throws ConfigParseException if reward configuration is invalid
     */
    public List<Reward> parseRewards(YamlConfiguration yaml) throws ConfigParseException {
        return rewardParser.parseRewards(yaml);
    }

    /**
     * Parses the teleport location from the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @return the parsed Location, or null if not configured
     * @throws ConfigParseException if the teleport location is invalid
     */
    public Location parseTeleportLocation(YamlConfiguration yaml) throws ConfigParseException {
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
}
