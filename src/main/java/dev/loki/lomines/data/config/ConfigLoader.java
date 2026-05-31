package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.data.config.parser.ConfigParser;
import dev.loki.lomines.data.reward.Reward;
import dev.loki.lomines.data.reward.RewardParser;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Facade for loading and saving MineConfig objects from/to YAML files.
 * Coordinates the work between ConfigParser, ConfigValidator, and ConfigSerializer.
 * Follows the Single Responsibility Principle by delegating specific tasks.
 */
public final class ConfigLoader {

    private final ConfigParser parser;
    private final ConfigValidator validator;
    private final ConfigSerializer serializer;

    public ConfigLoader() {
        RewardParser rewardParser = new RewardParser();
        this.parser = new ConfigParser(rewardParser);
        this.validator = new ConfigValidator();
        this.serializer = new ConfigSerializer();
    }

    /**
     * Loads a MineConfig from a YAML file.
     * Parses the file, validates all data, and builds the MineConfig object.
     *
     * @param configFile the path to the YAML configuration file
     * @return the loaded MineConfig object
     * @throws IOException          if the file cannot be read
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

        // Parse all sections
        List<Location> selections = parser.parseSelections(yaml);
        Map<String, Double> blocks = parser.parseBlocks(yaml);
        FillMode fillMode = parser.parseFillMode(yaml);
        String maskMarker = parser.parseMaskMarker(yaml);
        List<Location> maskPositions = parser.parseMaskPositions(yaml);
        List<Reward> rewards = parser.parseRewards(yaml);
        Location teleportLocation = parser.parseTeleportLocation(yaml);

        // Validate all data
        validator.validateSelections(selections);
        validator.validateBlocks(blocks);
        validator.validateMaskMarker(maskMarker);
        validator.validateRewards(rewards);

        // Build and return config
        return MineConfig.builder()
                .selections(selections)
                .blocks(blocks)
                .fillMode(fillMode)
                .maskMarkerMaterial(maskMarker)
                .maskPositions(maskPositions)
                .rewards(rewards)
                .resetTicks(yaml.getInt("reset.ticks", 6000))
                .resetPercent(yaml.getDouble("reset.percent", 10.0))
                .resetOnPercentEnabled(yaml.getBoolean("reset-on-percent", false))
                .actionBarEnabled(yaml.getBoolean("actionbar.enabled", true))
                .actionBarMessage(yaml.getString("actionbar.message", ""))
                .actionBarRange(yaml.getDouble("actionbar.range", 50.0))
                .timerFormat(yaml.getString("timer-format", "mm:ss"))
                .teleportOnReset(yaml.getBoolean("teleport-on-reset", false))
                .teleportLocation(teleportLocation)
                .resetCommands(yaml.getStringList("reset-commands"))
                .broadcastReset(yaml.getString("broadcast-reset", ""))
                .build();
    }

    /**
     * Saves a MineConfig to a YAML file.
     * Serializes the config and writes it to the specified file.
     *
     * @param config     the MineConfig to save
     * @param configFile the path to the YAML configuration file
     * @throws IOException              if the file cannot be written
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
        serializer.serialize(config, yaml);
        yaml.save(configFile.toFile());
    }
}
