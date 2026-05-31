package dev.loki.lomines.data.config.spawn;

import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for player spawn location when stuck in blocks.
 * Used to teleport players out when they are suffocating after mine reset.
 */
public record PlayerSpawnConfig(
        boolean enabled,
        Location location
) {

    public PlayerSpawnConfig {
        if (!enabled) {
            location = null;
        } else if (location != null) {
            // Defensive copy
            location = location.clone();
            Objects.requireNonNull(location.getWorld(), "Spawn world cannot be null");
        }
    }

    /**
     * Returns the spawn location if enabled.
     */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(enabled ? location : null);
    }

    /**
     * Disabled spawn config (default).
     */
    public static PlayerSpawnConfig disabled() {
        return new PlayerSpawnConfig(false, null);
    }

    /**
     * Creates enabled config with location.
     */
    public static PlayerSpawnConfig at(Location location) {
        return new PlayerSpawnConfig(true, location);
    }
}
