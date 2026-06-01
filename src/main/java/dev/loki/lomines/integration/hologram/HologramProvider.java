package dev.loki.lomines.integration.hologram;

import org.bukkit.Location;

import java.util.List;

/**
 * Interface for hologram providers.
 * Implementations handle creation and updates of holograms.
 */
public interface HologramProvider {

    /**
     * Creates a new hologram at the specified location.
     *
     * @param id unique identifier for this hologram
     * @param location where to place the hologram
     * @param lines initial lines to display
     * @return true if hologram was created successfully
     */
    boolean createHologram(String id, Location location, List<String> lines);

    /**
     * Updates the lines of an existing hologram.
     *
     * @param id the hologram identifier
     * @param lines new lines to display
     * @return true if updated successfully
     */
    boolean updateHologram(String id, List<String> lines);

    /**
     * Moves a hologram to a new location.
     *
     * @param id the hologram identifier
     * @param newLocation new location
     * @return true if moved successfully
     */
    boolean moveHologram(String id, Location newLocation);

    /**
     * Deletes a hologram.
     *
     * @param id the hologram identifier
     * @return true if deleted successfully
     */
    boolean deleteHologram(String id);

    /**
     * Checks if a hologram exists.
     *
     * @param id the hologram identifier
     * @return true if hologram exists
     */
    boolean exists(String id);

    /**
     * Checks if this provider is available (plugin is installed).
     *
     * @return true if the provider can be used
     */
    boolean isAvailable();

    /**
     * Gets the name of this provider.
     *
     * @return provider name
     */
    String getName();
}
