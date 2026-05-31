package dev.loki.lomines.data.config.region;

import dev.loki.lomines.util.location.Cuboid;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for mine regions.
 * Immutable, validates regions on construction.
 */
public record RegionConfig(List<Cuboid> regions) {

    public RegionConfig {
        if (regions == null || regions.isEmpty()) {
            throw new IllegalArgumentException("Mine must have at least one region");
        }

        // Defensive copy and validate
        List<Cuboid> copy = List.copyOf(regions);

        // Check all regions are in same world
        String worldName = copy.get(0).getWorld().getName();
        for (Cuboid region : copy) {
            if (!region.getWorld().getName().equals(worldName)) {
                throw new IllegalArgumentException(
                        "All regions must be in the same world. Expected: " + worldName
                );
            }
        }

        regions = Collections.unmodifiableList(copy);
    }

    /**
     * Creates a single-region config.
     */
    public static RegionConfig single(Cuboid region) {
        return new RegionConfig(List.of(region));
    }

    /**
     * Creates from paired locations (selection.1 + selection.2 = region 1).
     * Selections must be even number.
     */
    public static RegionConfig fromSelections(List<Location> selections) {
        if (selections.size() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Selections must be in pairs (even number). Got: " + selections.size()
            );
        }

        var regions = new java.util.ArrayList<Cuboid>();
        for (int i = 0; i < selections.size(); i += 2) {
            Location loc1 = selections.get(i);
            Location loc2 = selections.get(i + 1);

            if (!Objects.equals(loc1.getWorld(), loc2.getWorld())) {
                throw new IllegalArgumentException(
                        "Selection pair " + (i / 2 + 1) + " has locations in different worlds"
                );
            }

            regions.add(new Cuboid(loc1, loc2));
        }

        return new RegionConfig(regions);
    }

    /**
     * Total volume of all regions.
     */
    public int totalVolume() {
        return regions.stream().mapToInt(Cuboid::getVolume).sum();
    }

    /**
     * World name (all regions share the same world).
     */
    public String worldName() {
        return regions.get(0).getWorld().getName();
    }

    /**
     * Checks if location is within any region.
     */
    public boolean contains(Location location) {
        return regions.stream().anyMatch(r -> r.contains(location));
    }

    /**
     * Number of regions.
     */
    public int regionCount() {
        return regions.size();
    }
}
