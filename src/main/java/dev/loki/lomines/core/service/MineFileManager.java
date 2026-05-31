package dev.loki.lomines.core.service;

import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles file operations for mine configurations.
 */
public record MineFileManager(Path minesFolder) {

    public Path getMinesFolder() {
        return minesFolder;
    }

    public void ensureFolderExists() throws IOException {
        if (!Files.exists(minesFolder)) {
            Files.createDirectories(minesFolder);
        }
    }

    public void createDefaultConfig(String name, Location corner1, Location corner2) throws IOException {
        Path configFile = minesFolder.resolve(name + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();

        if (corner1 != null && corner2 != null) {
            yaml.set("selection.1", LocationParser.format(corner1));
            yaml.set("selection.2", LocationParser.format(corner2));
        } else {
            yaml.set("selection.1", "world;0;64;0;0;0");
            yaml.set("selection.2", "world;10;74;10;0;0");
        }

        yaml.set("contents.stone", 100.0);
        yaml.set("reset.ticks", 6000);
        yaml.set("reset.percent", 10.0);
        yaml.set("reset-on-percent", false);
        yaml.set("actionbar.enabled", true);
        yaml.set("actionbar.message", "&aMine: %mine% | Blocks: %blocks%/%total% (%percent%%)");
        yaml.set("actionbar.range", 50.0);
        yaml.set("timer-format", "mm:ss");
        yaml.set("teleport-on-reset", false);
        yaml.set("broadcast-reset", "");
        yaml.set("reset-commands", new ArrayList<String>());
        yaml.set("random-rewards", new ArrayList<Map<String, Object>>());
        yaml.set("fill-mode", "cuboid");
        yaml.set("mask.marker", "pink_concrete");
        yaml.set("mask.positions", new ArrayList<String>());

        yaml.save(configFile.toFile());
    }

    public MineConfig loadConfig(String name) throws IOException, ConfigParseException {
        Path configFile = minesFolder.resolve(name + ".yml");
        if (!Files.exists(configFile)) {
            throw new IOException("Mine config not found: " + configFile);
        }
        ConfigLoader loader = new ConfigLoader();
        return loader.load(configFile);
    }

    public void deleteConfig(String name) throws IOException {
        Path configFile = minesFolder.resolve(name + ".yml");
        if (Files.exists(configFile)) {
            Files.delete(configFile);
        }
    }

    public void saveMaskPositions(String name, String markerMaterial, List<Location> positions) throws IOException {
        Path configFile = minesFolder.resolve(name + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile.toFile());

        yaml.set("fill-mode", "mask");
        yaml.set("mask.marker", markerMaterial);

        List<String> lines = new ArrayList<>();
        for (Location loc : positions) {
            lines.add(LocationParser.format(loc));
        }
        yaml.set("mask.positions", lines);

        yaml.save(configFile.toFile());
    }
}
