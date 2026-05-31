package dev.loki.lomines.data.config.loader;

import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Loader for region configuration section.
 */
public final class RegionConfigLoader {

    /**
     * Parses region configuration from YAML.
     */
    public RegionConfig parse(YamlConfiguration yaml) {
        List<Location> selections = new ArrayList<>();

        // Try new format first
        for (int i = 1; i <= 10; i++) {
            String key = "region.selection." + i;
            if (yaml.contains(key)) {
                String locStr = yaml.getString(key);
                if (locStr != null && !locStr.isBlank()) {
                    selections.add(parseLocation(locStr));
                }
            }
        }

        // Legacy fallback
        if (selections.isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                String key = "selection." + i;
                if (yaml.contains(key)) {
                    String locStr = yaml.getString(key);
                    if (locStr != null && !locStr.isBlank()) {
                        selections.add(parseLocation(locStr));
                    }
                }
            }
        }

        if (selections.size() < 2) {
            throw new IllegalArgumentException("Mine must have at least 2 selection points (1 region)");
        }

        return RegionConfig.fromSelections(selections);
    }

    /**
     * Saves region configuration to YAML.
     */
    public void save(YamlConfiguration yaml, RegionConfig region) {
        int i = 1;
        for (var cuboid : region.regions()) {
            yaml.set("region.selection." + i, LocationParser.format(cuboid.getMin()));
            i++;
            yaml.set("region.selection." + i, LocationParser.format(cuboid.getMax()));
            i++;
        }
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
