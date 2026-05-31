package dev.loki.lomines.data.config.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for mine block contents.
 * Type-safe, immutable, with validation.
 */
public record BlockConfig(
        Map<BlockKey, Double> weights,
        FillMode fillMode,
        MaskConfig mask
) {

    public BlockConfig {
        // Defensive copy and validation
        Map<BlockKey, Double> copy = new HashMap<>();
        double totalWeight = 0;

        for (var entry : weights.entrySet()) {
            BlockKey key = Objects.requireNonNull(entry.getKey(), "Block key cannot be null");
            Double weight = entry.getValue();

            if (weight == null || weight <= 0 || !Double.isFinite(weight)) {
                throw new IllegalArgumentException(
                        "Invalid weight for " + key.serialize() + ": " + weight + " (must be positive finite number)"
                );
            }

            totalWeight += weight;
            copy.put(key, weight);
        }

        if (copy.isEmpty()) {
            throw new IllegalArgumentException("Block config must have at least one block");
        }

        // Normalize weights to sum to 1.0
        if (Math.abs(totalWeight - 1.0) > 0.0001 && totalWeight > 0) {
            double finalTotal = totalWeight;
            copy.replaceAll((k, v) -> v / finalTotal);
        }

        weights = Collections.unmodifiableMap(new HashMap<>(copy));
        fillMode = Objects.requireNonNull(fillMode, "FillMode cannot be null");
        mask = mask != null ? mask : new MaskConfig(null, Map.of());
    }

    /**
     * Creates a simple vanilla-only config.
     */
    public static BlockConfig vanilla(Map<Material, Double> materials) {
        Map<BlockKey, Double> keys = new HashMap<>();
        for (var entry : materials.entrySet()) {
            keys.put(new BlockKey.Vanilla(entry.getKey()), entry.getValue());
        }
        return new BlockConfig(keys, FillMode.CUBOID, null);
    }

    /**
     * Gets the weight for a specific block key.
     */
    public double weightFor(BlockKey key) {
        return weights.getOrDefault(key, 0.0);
    }

    /**
     * Total number of unique blocks.
     */
    public int blockCount() {
        return weights.size();
    }

    /**
     * Whether this config uses custom blocks (Oraxen/ItemsAdder).
     */
    public boolean hasCustomBlocks() {
        return weights.keySet().stream()
                .anyMatch(k -> k instanceof BlockKey.Oraxen || k instanceof BlockKey.ItemsAdder);
    }

    /**
     * Configuration for mask fill mode.
     */
    public record MaskConfig(
            BlockKey marker,
            Map<String, Boolean> positions  // serialized location -> enabled
    ) {
        public MaskConfig {
            positions = positions != null
                    ? Collections.unmodifiableMap(new HashMap<>(positions))
                    : Map.of();
        }
    }
}
