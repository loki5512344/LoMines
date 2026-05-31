package dev.loki.lomines.data.config.loader.entity;

import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loader for teleport configuration section.
 */
public final class TeleportConfigLoader {

    /**
     * Parses teleport configuration from YAML.
     */
    public TeleportConfig parse(YamlConfiguration yaml) {
        boolean enabled = yaml.getBoolean("teleport.enabled", false);
        if (!enabled) {
            return TeleportConfig.disabled();
        }

        String locStr = yaml.getString("teleport.location");
        if (locStr == null || locStr.isBlank()) {
            return TeleportConfig.disabled();
        }

        return TeleportConfig.at(parseLocation(locStr));
    }

    /**
     * Saves teleport configuration to YAML.
     */
    public void save(YamlConfiguration yaml, TeleportConfig teleport) {
        yaml.set("teleport.enabled", teleport.enabled());
        teleport.getLocation().ifPresent(loc ->
                yaml.set("teleport.location", LocationParser.format(loc)));
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
