package dev.loki.lomines.data.config.block;

import org.bukkit.Material;

import java.util.Objects;

/**
 * Type-safe key for identifying blocks across different plugins.
 * Supports vanilla blocks, Oraxen, and ItemsAdder.
 */
public sealed interface BlockKey permits BlockKey.Vanilla, BlockKey.Oraxen, BlockKey.ItemsAdder {

    /**
     * Deserializes a string to appropriate BlockKey type.
     *
     * @throws IllegalArgumentException if format is invalid
     */
    static BlockKey deserialize(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Block key cannot be null or empty");
        }

        String trimmed = key.trim().toLowerCase();

        if (trimmed.startsWith("oraxen:")) {
            return new Oraxen(trimmed.substring(7));
        }
        if (trimmed.startsWith("itemsadder:")) {
            return new ItemsAdder(trimmed.substring(11));
        }

        // Vanilla block
        Material material = Material.matchMaterial(trimmed);
        if (material == null || !material.isBlock()) {
            throw new IllegalArgumentException("Unknown vanilla block: " + key);
        }
        return new Vanilla(material);
    }

    /**
     * Serializes this key to string format for YAML storage.
     * Format: "stone" (vanilla), "oraxen:my_block", "itemsadder:custom_block"
     */
    String serialize();

    // --- Implementations ---

    record Vanilla(Material material) implements BlockKey {
        public Vanilla {
            Objects.requireNonNull(material, "Material cannot be null");
            if (!material.isBlock()) {
                throw new IllegalArgumentException(material + " is not a block");
            }
        }

        @Override
        public String serialize() {
            return material.name().toLowerCase();
        }
    }

    record Oraxen(String id) implements BlockKey {
        public Oraxen {
            Objects.requireNonNull(id, "Oraxen id cannot be null");
            if (id.isBlank()) {
                throw new IllegalArgumentException("Oraxen id cannot be blank");
            }
        }

        @Override
        public String serialize() {
            return "oraxen:" + id;
        }
    }

    record ItemsAdder(String id) implements BlockKey {
        public ItemsAdder {
            Objects.requireNonNull(id, "ItemsAdder id cannot be null");
            if (id.isBlank()) {
                throw new IllegalArgumentException("ItemsAdder id cannot be blank");
            }
        }

        @Override
        public String serialize() {
            return "itemsadder:" + id;
        }
    }
}
