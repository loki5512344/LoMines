package dev.loki.lomines.util;

import org.bukkit.Material;

import java.util.Optional;

/**
 * Utility class for validation operations.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Validates and parses a material name.
     *
     * @param materialName the material name to validate
     * @return the Material if valid
     * @throws IllegalArgumentException if material is invalid
     */
    public static Material validateMaterial(String materialName) {
        if (materialName == null || materialName.isBlank()) {
            throw new IllegalArgumentException("Material name cannot be null or empty");
        }

        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid material: " + materialName);
        }
    }

    /**
     * Safely parses a material name, returning Optional.
     *
     * @param materialName the material name to parse
     * @return Optional containing the Material if valid, empty otherwise
     */
    public static Optional<Material> parseMaterial(String materialName) {
        if (materialName == null || materialName.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Material.valueOf(materialName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Parses a material name with a fallback default.
     *
     * @param materialName    the material name to parse
     * @param defaultMaterial the default material if parsing fails
     * @return the parsed Material or the default
     */
    public static Material parseMaterialOrDefault(String materialName, Material defaultMaterial) {
        return parseMaterial(materialName).orElse(defaultMaterial);
    }
}
