package dev.loki.lomines.data.config.loader.entity;

import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loader for player spawn configuration section.
 */
public final class PlayerSpawnConfigLoader {

    /**
     * Parses player spawn configuration from YAML.
     */
    public PlayerSpawnConfig parse(YamlConfiguration yaml) {
        boolean enabled = yaml.getBoolean("player-spawn.enabled", false);
        if (!enabled) {
            return PlayerSpawnConfig.disabled();
        }

        String locStr = yaml.getString("player-spawn.location");
        if (locStr == null || locStr.isBlank()) {
            return PlayerSpawnConfig.disabled();
        }

        return PlayerSpawnConfig.at(parseLocation(locStr));
    }

    /**
     * Saves player spawn configuration to YAML.
     */
    public void save(YamlConfiguration yaml, PlayerSpawnConfig spawn) {
        yaml.set("player-spawn.enabled", spawn.enabled());
        spawn.getLocation().ifPresent(loc ->
                yaml.set("player-spawn.location", LocationParser.format(loc)));
    }

    /**
     * Sets default values for player spawn configuration.
     */
    public void setDefaults(YamlConfiguration yaml) {
        yaml.set("player-spawn.enabled", false);
        yaml.set("player-spawn.location", "");
    }

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
}
