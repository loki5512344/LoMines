package dev.loki.lomines.data.config.loader.region;

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
     * Supports up to 100 selection points (50 regions).
     */
    public RegionConfig parse(YamlConfiguration yaml) {
        List<Location> selections = new ArrayList<>();

        // Try new format first (region.selection.X)
        selections.addAll(parseSelections(yaml, "region.selection.", 100));

        // Legacy fallback (selection.X)
        if (selections.isEmpty()) {
            selections.addAll(parseSelections(yaml, "selection.", 100));
        }

        if (selections.size() < 2) {
            throw new IllegalArgumentException("Mine must have at least 2 selection points (1 region)");
        }

        return RegionConfig.fromSelections(selections);
    }

    /**
     * Parses selection points from YAML with given prefix.
     * Supports indexed format: prefix.1, prefix.2, etc.
     */
    private List<Location> parseSelections(YamlConfiguration yaml, String prefix, int maxPoints) {
        List<Location> selections = new ArrayList<>();

        for (int i = 1; i <= maxPoints; i++) {
            String key = prefix + i;
            if (yaml.contains(key)) {
                String locStr = yaml.getString(key);
                if (locStr != null && !locStr.isBlank()) {
                    try {
                        selections.add(parseLocation(locStr));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid locations but continue parsing
                    }
                }
            } else {
                // Stop at first missing index (assuming sequential)
                // But check at least a few more for gaps
                if (i > 20) {
                    boolean hasMore = false;
                    for (int j = i + 1; j <= Math.min(i + 5, maxPoints); j++) {
                        if (yaml.contains(prefix + j)) {
                            hasMore = true;
                            break;
                        }
                    }
                    if (!hasMore) break;
                }
            }
        }

        return selections;
    }

    /**
     * Saves region configuration to YAML.
     */
    public void save(YamlConfiguration yaml, RegionConfig region) {
        int i = 1;
        for (var cuboid : region.regions()) {
            Location min = new Location(cuboid.getWorld(), cuboid.getMinX(), cuboid.getMinY(), cuboid.getMinZ());
            yaml.set("region.selection." + i, LocationParser.format(min));
            i++;
            Location max = new Location(cuboid.getWorld(), cuboid.getMaxX(), cuboid.getMaxY(), cuboid.getMaxZ());
            yaml.set("region.selection." + i, LocationParser.format(max));
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
