package dev.loki.lomines.util.selection;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player selections for mine region definition.
 * Stores a Selection object for each player, allowing multiple players
 * to have independent selections simultaneously.
 *
 * <p>This class is thread-safe and can be accessed from multiple threads.</p>
 *
 * <p><b>Validates:</b> Requirements 12.7, 16.7</p>
 */
public final class SelectionManager {

    private final Map<UUID, Selection> selections;

    /**
     * Creates a new SelectionManager with thread-safe storage.
     */
    public SelectionManager() {
        this.selections = new ConcurrentHashMap<>();
    }

    /**
     * Gets the selection for a player, creating a new one if it doesn't exist.
     *
     * @param playerId The player's UUID
     * @return The player's Selection object
     * @throws IllegalArgumentException if playerId is null
     */
    public Selection getSelection(UUID playerId) {
        validatePlayerId(playerId);
        return selections.computeIfAbsent(playerId, id -> new Selection());
    }

    /**
     * Gets the selection for a player if it exists.
     *
     * @param playerId The player's UUID
     * @return Optional containing the Selection if it exists, empty otherwise
     * @throws IllegalArgumentException if playerId is null
     */
    public Optional<Selection> findSelection(UUID playerId) {
        validatePlayerId(playerId);
        return Optional.ofNullable(selections.get(playerId));
    }

    /**
     * Checks if a player has a selection.
     *
     * @param playerId The player's UUID
     * @return true if the player has a selection, false otherwise
     * @throws IllegalArgumentException if playerId is null
     */
    public boolean hasSelection(UUID playerId) {
        validatePlayerId(playerId);
        return selections.containsKey(playerId);
    }

    /**
     * Clears the selection for a player, removing it from storage.
     *
     * @param playerId The player's UUID
     * @throws IllegalArgumentException if playerId is null
     */
    public void clearSelection(UUID playerId) {
        validatePlayerId(playerId);
        selections.remove(playerId);
    }

    /**
     * Validates that a player ID is not null.
     *
     * @param playerId The player ID to validate
     * @throws IllegalArgumentException if playerId is null
     */
    private void validatePlayerId(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }
    }
}
