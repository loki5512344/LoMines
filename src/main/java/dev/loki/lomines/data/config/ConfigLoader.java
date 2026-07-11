package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.loader.block.BlockConfigLoader;
import dev.loki.lomines.data.config.loader.entity.PlayerSpawnConfigLoader;
import dev.loki.lomines.data.config.loader.entity.TeleportConfigLoader;
import dev.loki.lomines.data.config.loader.region.RegionConfigLoader;
import dev.loki.lomines.data.config.loader.region.WorldGuardConfigLoader;
import dev.loki.lomines.data.config.loader.reward.RewardConfigLoader;
import dev.loki.lomines.data.config.loader.system.ResetConfigLoader;
import dev.loki.lomines.data.config.loader.system.UIConfigLoader;
import dev.loki.lomines.data.config.model.MineConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private final Path dataFolder;
    private final DefaultsMerger defaultsMerger;

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
        this.defaultsMerger = new DefaultsMerger(dataFolder);
        initLoaders();
    }

    private void initLoaders() {
        regionLoader = new RegionConfigLoader();
        blockLoader = new BlockConfigLoader();
        resetLoader = new ResetConfigLoader(defaultsMerger.getDefaults());
        rewardLoader = new RewardConfigLoader();
        teleportLoader = new TeleportConfigLoader();
        uiLoader = new UIConfigLoader(defaultsMerger.getDefaults());
        worldGuardLoader = new WorldGuardConfigLoader(defaultsMerger.getDefaults());
        playerSpawnLoader = new PlayerSpawnConfigLoader();
    }

    public MineConfig load(String mineName) throws ConfigLoadException {
        if (!Files.exists(dataFolder.resolve("mines").resolve(mineName + ".yml"))) {
            throw new ConfigLoadException("Mine not found: " + mineName);
        }

        Path configPath = dataFolder.resolve("mines").resolve(mineName + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configPath.toFile());
        defaultsMerger.mergeDefaults(yaml);

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

    public void save(MineConfig config) throws ConfigLoadException {
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
            Path configPath = dataFolder.resolve("mines").resolve(config.name() + ".yml");
            Files.createDirectories(configPath.getParent());
            yaml.save(configPath.toFile());
        } catch (IOException e) {
            throw new ConfigLoadException("Failed to save mine: " + e.getMessage(), e);
        }
    }

    public static class ConfigLoadException extends Exception {
        public ConfigLoadException(String message) {
            super(message);
        }

        public ConfigLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
