package dev.loki.lomines.data.config.spawn;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for player spawn locations when stuck in blocks.
 * Supports multiple spawn points for a single mine.
 * Used to teleport players out when they are suffocating after mine reset.
 */
public record PlayerSpawnConfig(boolean enabled, List<Location> locations) {

    public PlayerSpawnConfig(boolean enabled, List<Location> locations) {
        this.enabled = enabled;
        if (!enabled || locations == null) {
            this.locations = List.of();
        } else {
            // Defensive copy with validation
            List<Location> copy = new ArrayList<>();
            for (Location loc : locations) {
                if (loc != null) {
                    Location cloned = loc.clone();
                    Objects.requireNonNull(cloned.getWorld(), "Spawn world cannot be null");
                    copy.add(cloned);
                }
            }
            this.locations = List.copyOf(copy);
        }
    }

    /**
     * Disabled spawn config (default).
     */
    public static PlayerSpawnConfig disabled() {
        return new PlayerSpawnConfig(false, null);
    }

    /**
     * Creates enabled config with single location.
     */
    public static PlayerSpawnConfig at(Location location) {
        return new PlayerSpawnConfig(true, List.of(location));
    }

    /**
     * Creates enabled config with multiple locations.
     */
    public static PlayerSpawnConfig at(List<Location> locations) {
        return new PlayerSpawnConfig(true, locations);
    }

    /**
     * Returns the primary spawn location (first in list) if enabled.
     */
    public Optional<Location> getLocation() {
        return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
    }

    /**
     * Returns all spawn locations.
     */
    @Override
    public List<Location> locations() {
        return locations;
    }

    /**
     * Returns true if spawn is enabled and has at least one location.
     */
    @Override
    public boolean enabled() {
        return enabled && !locations.isEmpty();
    }

    /**
     * Returns the number of configured spawn locations.
     */
    public int count() {
        return locations.size();
    }

    /**
     * Creates a new config with an additional location.
     */
    public PlayerSpawnConfig addLocation(Location location) {
        List<Location> newList = new ArrayList<>(locations);
        newList.add(location);
        return new PlayerSpawnConfig(true, newList);
    }

    /**
     * Creates a new config without the location at given index.
     */
    public PlayerSpawnConfig removeLocation(int index) {
        if (index < 0 || index >= locations.size()) {
            return this;
        }
        List<Location> newList = new ArrayList<>(locations);
        newList.remove(index);
        return new PlayerSpawnConfig(!newList.isEmpty(), newList);
    }

    /**
     * Clears all locations (disables spawn).
     */
    public PlayerSpawnConfig clear() {
        return disabled();
    }
}
