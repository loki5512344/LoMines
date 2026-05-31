package dev.loki.lomines.core.service;

import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.util.location.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles file operations for mine configurations.
 * Updated for new section-based configuration (v2).
 */
public record MineFileManager(Path minesFolder, ConfigLoader configLoader) {

    public MineFileManager(Path minesFolder) {
        this(minesFolder, new ConfigLoader(minesFolder.getParent()));
    }

    public Path getMinesFolder() {
        return minesFolder;
    }

    public void ensureFolderExists() throws IOException {
        if (!Files.exists(minesFolder)) {
            Files.createDirectories(minesFolder);
        }
    }

    /**
     * Creates a default mine configuration with new format.
     */
    public void createDefaultConfig(String name, Location corner1, Location corner2) throws IOException {
        ensureFolderExists();

        // Create default region
        Location loc1 = corner1 != null ? corner1 : new Location(
                org.bukkit.Bukkit.getWorlds().get(0), 0, 64, 0);
        Location loc2 = corner2 != null ? corner2 : new Location(
                org.bukkit.Bukkit.getWorlds().get(0), 10, 74, 10);

        Cuboid cuboid = new Cuboid(loc1, loc2);
        RegionConfig region = RegionConfig.single(cuboid);

        // Create default blocks (stone only)
        Map<BlockKey, Double> weights = new HashMap<>();
        weights.put(new BlockKey.Vanilla(Material.STONE), 1.0);
        BlockConfig blocks = new BlockConfig(weights, FillMode.CUBOID, null);

        // Build mine config with defaults
        MineConfig config = MineConfig.builder(name)
                .region(region)
                .blocks(blocks)
                .reset(ResetConfig.defaults())
                .rewards(RewardConfig.empty())
                .teleport(TeleportConfig.disabled())
                .ui(UIConfig.defaults())
                .build();

        // Save using new loader
        configLoader.save(config);
    }

    public MineConfig loadConfig(String name) throws IOException, ConfigLoader.ConfigLoadException {
        return configLoader.load(name);
    }

    public void deleteConfig(String name) throws IOException {
        Path configFile = minesFolder.resolve(name + ".yml");
        if (Files.exists(configFile)) {
            Files.delete(configFile);
        }
    }

    /**
     * Saves mask positions for mask fill mode.
     */
    public void saveMaskPositions(String name, BlockKey markerMaterial, java.util.List<Location> positions) throws IOException, ConfigLoader.ConfigLoadException {
        MineConfig config = loadConfig(name);

        // Build new mask config
        Map<String, Boolean> posMap = new HashMap<>();
        for (Location loc : positions) {
            posMap.put(LocationParser.format(loc), true);
        }
        BlockConfig.MaskConfig mask = new BlockConfig.MaskConfig(markerMaterial, posMap);

        // Create new block config with mask
        BlockConfig newBlocks = new BlockConfig(
                config.blocks().weights(),
                FillMode.MASK,
                mask
        );

        // Build updated config
        MineConfig updated = MineConfig.builder(name)
                .region(config.region())
                .blocks(newBlocks)
                .reset(config.reset())
                .rewards(config.rewards())
                .teleport(config.teleport())
                .ui(config.ui())
                .build();

        configLoader.save(updated);
    }
}
