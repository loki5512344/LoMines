package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.loader.block.BlockConfigLoader;
import dev.loki.lomines.data.config.loader.entity.PlayerSpawnConfigLoader;
import dev.loki.lomines.data.config.loader.entity.TeleportConfigLoader;
import dev.loki.lomines.data.config.loader.region.RegionConfigLoader;
import dev.loki.lomines.data.config.loader.region.WorldGuardConfigLoader;
import dev.loki.lomines.data.config.loader.reward.RewardConfigLoader;
import dev.loki.lomines.data.config.loader.system.ResetConfigLoader;
import dev.loki.lomines.data.config.loader.system.UIConfigLoader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Clean config loader for section-based configuration.
 * Supports YAML inheritance from defaults.
 * Delegates to specialized loaders for each section.
 */
public final class ConfigLoader {

    private final Path dataFolder;
    private YamlConfiguration defaults;

    // Section loaders
    private RegionConfigLoader regionLoader;
    private BlockConfigLoader blockLoader;
    private ResetConfigLoader resetLoader;
    private RewardConfigLoader rewardLoader;
    private TeleportConfigLoader teleportLoader;
    private UIConfigLoader uiLoader;
    private WorldGuardConfigLoader worldGuardLoader;
    private PlayerSpawnConfigLoader playerSpawnLoader;

    public ConfigLoader(Path dataFolder) {
        this.dataFolder = dataFolder;
        loadDefaults();
        initLoaders();
    }

    private void loadDefaults() {
        Path defaultsPath = dataFolder.resolve("mines").resolve("_defaults.yml");
        defaults = new YamlConfiguration();

        if (Files.exists(defaultsPath)) {
            defaults = YamlConfiguration.loadConfiguration(defaultsPath.toFile());
        } else {
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

        worldGuardLoader.setDefaults(yaml);
        playerSpawnLoader.setDefaults(yaml);
    }

    private void initLoaders() {
        regionLoader = new RegionConfigLoader();
        blockLoader = new BlockConfigLoader();
        resetLoader = new ResetConfigLoader(defaults);
        rewardLoader = new RewardConfigLoader();
        teleportLoader = new TeleportConfigLoader();
        uiLoader = new UIConfigLoader(defaults);
        worldGuardLoader = new WorldGuardConfigLoader(defaults);
        playerSpawnLoader = new PlayerSpawnConfigLoader();
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
        mergeDefaults(yaml);

        try {
            return MineConfig.builder(mineName)
                    .region(regionLoader.parse(yaml))
                    .blocks(blockLoader.parse(yaml))
                    .reset(resetLoader.parse(yaml))
                    .rewards(rewardLoader.parse(yaml))
                    .teleport(teleportLoader.parse(yaml))
                    .ui(uiLoader.parse(yaml))
                    .worldGuard(worldGuardLoader.parse(yaml))
                    .playerSpawn(playerSpawnLoader.parse(yaml))
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

        regionLoader.save(yaml, config.region());
        blockLoader.save(yaml, config.blocks());
        resetLoader.save(yaml, config.reset());
        rewardLoader.save(yaml, config.rewards());
        teleportLoader.save(yaml, config.teleport());
        uiLoader.save(yaml, config.ui());
        worldGuardLoader.save(yaml, config.worldGuard());
        playerSpawnLoader.save(yaml, config.playerSpawn());

        try {
            Files.createDirectories(configPath.getParent());
            yaml.save(configPath.toFile());
        } catch (IOException e) {
            throw new ConfigLoadException("Failed to save mine: " + e.getMessage(), e);
        }
    }

    private void mergeDefaults(YamlConfiguration yaml) {
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
