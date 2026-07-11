package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.loader.region.WorldGuardConfigLoader;
import dev.loki.lomines.data.config.loader.entity.PlayerSpawnConfigLoader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DefaultsMerger {

    private final Path dataFolder;
    private YamlConfiguration defaults;

    public DefaultsMerger(Path dataFolder) {
        this.dataFolder = dataFolder;
        loadDefaults();
    }

    public YamlConfiguration getDefaults() {
        return defaults;
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

        WorldGuardConfigLoader wgLoader = new WorldGuardConfigLoader(new YamlConfiguration());
        wgLoader.setDefaults(yaml);
        PlayerSpawnConfigLoader psLoader = new PlayerSpawnConfigLoader();
        psLoader.setDefaults(yaml);
    }

    public void mergeDefaults(YamlConfiguration yaml) {
        for (String key : defaults.getKeys(true)) {
            if (!yaml.contains(key)) {
                yaml.set(key, defaults.get(key));
            }
        }
    }
}
