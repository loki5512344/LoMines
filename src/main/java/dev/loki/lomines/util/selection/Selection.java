package dev.loki.lomines.util.selection;

import dev.loki.lomines.util.location.Cuboid;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages selection points for defining mine regions.
 * Stores up to 10 points (5 pairs) for multi-regional mines.
 * Points are organized in pairs where indices 2i and 2i+1 form a pair for i ∈ [0, 4].
 *
 * <p>This class is NOT thread-safe and should be used per-player.</p>
 */
public final class Selection {

    private static final int MAX_POINTS = 10;
    private static final int MAX_PAIRS = 5;

    private final Location[] points;

    /**
     * Creates a new empty selection.
     */
    public Selection() {
        this.points = new Location[MAX_POINTS];
    }

    /**
     * Sets a point at the specified index.
     *
     * @param index    The point index (0-9)
     * @param location The location to set
     * @throws IllegalArgumentException if index is out of bounds
     */
    public void setPoint(int index, Location location) {
        validateIndex(index);
        points[index] = location;
    }

    /**
     * Gets a point at the specified index.
     *
     * @param index The point index (0-9)
     * @return Optional containing the location if set, empty otherwise
     * @throws IllegalArgumentException if index is out of bounds
     */
    public Optional<Location> getPoint(int index) {
        validateIndex(index);
        return Optional.ofNullable(points[index]);
    }

    /**
     * Gets a pair of points as an array.
     * A pair consists of points at indices 2*pairIndex and 2*pairIndex+1.
     *
     * @param pairIndex The pair index (0-4)
     * @return Array of two locations [point1, point2], may contain nulls
     * @throws IllegalArgumentException if pairIndex is out of bounds
     */
    public Location[] getPair(int pairIndex) {
        validatePairIndex(pairIndex);
        int firstIndex = pairIndex * 2;
        int secondIndex = firstIndex + 1;
        return new Location[]{points[firstIndex], points[secondIndex]};
    }

    /**
     * Checks if a pair has both points set.
     *
     * @param pairIndex The pair index (0-4)
     * @return true if both points in the pair are set, false otherwise
     * @throws IllegalArgumentException if pairIndex is out of bounds
     */
    public boolean hasPair(int pairIndex) {
        validatePairIndex(pairIndex);
        int firstIndex = pairIndex * 2;
        int secondIndex = firstIndex + 1;
        return points[firstIndex] != null && points[secondIndex] != null;
    }

    /**
     * Clears all selection points.
     */
    public void clear() {
        for (int i = 0; i < MAX_POINTS; i++) {
            points[i] = null;
        }
    }

    /**
     * Converts all complete pairs to Cuboid regions.
     * Only pairs with both points set are converted.
     *
     * @return List of Cuboid regions created from complete pairs
     */
    public List<Cuboid> toCuboids() {
        List<Cuboid> cuboids = new ArrayList<>();

        for (int pairIndex = 0; pairIndex < MAX_PAIRS; pairIndex++) {
            if (hasPair(pairIndex)) {
                Location[] pair = getPair(pairIndex);
                cuboids.add(new Cuboid(pair[0], pair[1]));
            }
        }

        return cuboids;
    }

    /**
     * Validates that an index is within valid bounds [0, 9].
     *
     * @param index The index to validate
     * @throws IllegalArgumentException if index is out of bounds
     */
    private void validateIndex(int index) {
        if (index < 0 || index >= MAX_POINTS) {
            throw new IllegalArgumentException(
                    "Point index must be between 0 and " + (MAX_POINTS - 1) + ", got: " + index
            );
        }
    }

    /**
     * Validates that a pair index is within valid bounds [0, 4].
     *
     * @param pairIndex The pair index to validate
     * @throws IllegalArgumentException if pairIndex is out of bounds
     */
    private void validatePairIndex(int pairIndex) {
        if (pairIndex < 0 || pairIndex >= MAX_PAIRS) {
            throw new IllegalArgumentException(
                    "Pair index must be between 0 and " + (MAX_PAIRS - 1) + ", got: " + pairIndex
            );
        }
    }
}
