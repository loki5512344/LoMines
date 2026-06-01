package dev.loki.lomines.data.config.teleport;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for mine teleportation on reset.
 * Supports multiple teleport locations for a single mine.
 */
public final class TeleportConfig {

    private final boolean enabled;
    private final List<Location> locations;

    public TeleportConfig(boolean enabled, List<Location> locations) {
        this.enabled = enabled;
        if (!enabled || locations == null) {
            this.locations = List.of();
        } else {
            // Defensive copy with validation
            List<Location> copy = new ArrayList<>();
            for (Location loc : locations) {
                if (loc != null) {
                    Location cloned = loc.clone();
                    Objects.requireNonNull(cloned.getWorld(), "Teleport world cannot be null");
                    copy.add(cloned);
                }
            }
            this.locations = List.copyOf(copy);
        }
    }

    /**
     * Returns the primary teleport location (first in list) if enabled.
     */
    public Optional<Location> getLocation() {
        return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
    }

    /**
     * Returns all teleport locations.
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     * Returns true if teleport is enabled and has at least one location.
     */
    public boolean enabled() {
        return enabled && !locations.isEmpty();
    }

    /**
     * Returns the number of configured teleport locations.
     */
    public int count() {
        return locations.size();
    }

    /**
     * Disabled teleport config.
     */
    public static TeleportConfig disabled() {
        return new TeleportConfig(false, null);
    }

    /**
     * Creates enabled config with single location.
     */
    public static TeleportConfig at(Location location) {
        return new TeleportConfig(true, List.of(location));
    }

    /**
     * Creates enabled config with multiple locations.
     */
    public static TeleportConfig at(List<Location> locations) {
        return new TeleportConfig(true, locations);
    }

    /**
     * Creates a new config with an additional location.
     */
    public TeleportConfig addLocation(Location location) {
        List<Location> newList = new ArrayList<>(locations);
        newList.add(location);
        return new TeleportConfig(true, newList);
    }

    /**
     * Creates a new config without the location at given index.
     */
    public TeleportConfig removeLocation(int index) {
        if (index < 0 || index >= locations.size()) {
            return this;
        }
        List<Location> newList = new ArrayList<>(locations);
        newList.remove(index);
        return new TeleportConfig(!newList.isEmpty(), newList);
    }

    /**
     * Clears all locations (disables teleport).
     */
    public TeleportConfig clear() {
        return disabled();
    }
}
