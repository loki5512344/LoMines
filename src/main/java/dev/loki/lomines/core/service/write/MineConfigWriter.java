package dev.loki.lomines.core.service.write;

import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.integration.worldguard.config.WorldGuardConfig;
import dev.loki.lomines.util.location.geo.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class MineConfigWriter {

    private final Path minesFolder;
    private final ConfigLoader configLoader;

    public MineConfigWriter(Path minesFolder, ConfigLoader configLoader) {
        this.minesFolder = minesFolder;
        this.configLoader = configLoader;
    }

    public void createDefaultConfig(String name, Location corner1, Location corner2) throws IOException {
        Location loc1 = corner1 != null ? corner1 : new Location(
                org.bukkit.Bukkit.getWorlds().get(0), 0, 64, 0);
        Location loc2 = corner2 != null ? corner2 : new Location(
                org.bukkit.Bukkit.getWorlds().get(0), 10, 74, 10);

        Cuboid cuboid = new Cuboid(loc1, loc2);
        RegionConfig region = RegionConfig.single(cuboid);

        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 1.0);
        BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

        WorldGuardConfig wgConfig = loadWorldGuardDefaults();

        MineConfig config = MineConfig.builder(name)
                .region(region)
                .blocks(blocks)
                .reset(ResetConfig.defaults())
                .rewards(RewardConfig.empty())
                .teleport(TeleportConfig.disabled())
                .ui(UIConfig.defaults())
                .worldGuard(wgConfig)
                .playerSpawn(PlayerSpawnConfig.disabled())
                .build();

        try {
            configLoader.save(config);
        } catch (ConfigLoader.ConfigLoadException e) {
            throw new IOException("Failed to save default mine config: " + e.getMessage(), e);
        }
    }

    public void saveMaskPositions(String name, BlockKey markerMaterial,
                                   java.util.List<Location> positions) throws IOException, ConfigLoader.ConfigLoadException {
        MineConfig config = configLoader.load(name);

        Map<String, Boolean> posMap = new HashMap<>();
        for (Location loc : positions) {
            posMap.put(LocationParser.format(loc), true);
        }
        BlockConfig.MaskConfig mask = new BlockConfig.MaskConfig(markerMaterial, posMap);

        BlockConfig newBlocks = new BlockConfig(
                config.blocks().weights(),
                FillMode.MASK,
                mask
        );

        MineConfig updated = MineConfig.builder(name)
                .region(config.region())
                .blocks(newBlocks)
                .reset(config.reset())
                .rewards(config.rewards())
                .teleport(config.teleport())
                .ui(config.ui())
                .worldGuard(config.worldGuard())
                .playerSpawn(config.playerSpawn())
                .build();

        configLoader.save(updated);
    }

    private WorldGuardConfig loadWorldGuardDefaults() {
        Path defaultsPath = minesFolder.resolve("_defaults.yml");
        if (!Files.exists(defaultsPath)) {
            return WorldGuardConfig.disabled();
        }
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(defaultsPath.toFile());
            ConfigurationSection wgSection = yaml.getConfigurationSection("worldguard");
            if (wgSection == null) {
                return WorldGuardConfig.disabled();
            }
            boolean enabled = wgSection.getBoolean("enabled", false);
            if (!enabled) {
                return WorldGuardConfig.disabled();
            }
            String template = wgSection.getString("region-template", "{mine_name}_{random_4}");
            return WorldGuardConfig.builder()
                    .enabled(true)
                    .template(template)
                    .owners(wgSection.getStringList("owners"))
                    .members(wgSection.getStringList("members"))
                    .flags(wgSection.getStringList("flags"))
                    .protectOnCreate(wgSection.getBoolean("protect-on-create", true))
                    .build();
        } catch (Exception e) {
            return WorldGuardConfig.disabled();
        }
    }
}
