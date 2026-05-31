package dev.loki.lomines.data.config.teleport;

import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for mine teleportation on reset.
 */
public record TeleportConfig(
        boolean enabled,
        Location location
) {

    public TeleportConfig {
        if (!enabled) {
            location = null;
        } else if (location != null) {
            // Defensive copy
            location = location.clone();
            Objects.requireNonNull(location.getWorld(), "Teleport world cannot be null");
        }
    }

    /**
     * Returns the teleport location if enabled.
     */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(enabled ? location : null);
    }

    /**
     * Disabled teleport config.
     */
    public static TeleportConfig disabled() {
        return new TeleportConfig(false, null);
    }

    /**
     * Creates enabled config with location.
     */
    public static TeleportConfig at(Location location) {
        return new TeleportConfig(true, location);
    }
}
