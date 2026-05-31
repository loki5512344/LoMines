package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.data.reward.Reward;
import dev.loki.lomines.util.ValidationUtils;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

/**
 * Validates configuration data according to business rules.
 * Responsible only for validation - throws ConfigParseException on invalid data.
 */
final class ConfigValidator {

    /**
     * Validates selection coordinates.
     *
     * @param selections the list of selections to validate
     * @throws ConfigParseException if selections are invalid
     */
    void validateSelections(List<Location> selections) throws ConfigParseException {
        if (selections.isEmpty()) {
            throw new ConfigParseException(
                    "No selection coordinates found. At least one selection pair (2 points) is required"
            );
        }

        if (selections.size() % 2 != 0) {
            throw new ConfigParseException(
                    "Invalid number of selection coordinates: " + selections.size() +
                            ". Selections must be in pairs (even number)"
            );
        }
    }

    /**
     * Validates block contents configuration.
     *
     * @param blocks the map of block keys to weights
     * @throws ConfigParseException if blocks are invalid
     */
    void validateBlocks(Map<String, Double> blocks) throws ConfigParseException {
        if (blocks.isEmpty()) {
            throw new ConfigParseException(
                    "No blocks defined in 'contents' section. At least one block is required"
            );
        }

        for (Map.Entry<String, Double> entry : blocks.entrySet()) {
            String key = entry.getKey();
            Double weight = entry.getValue();

            if (weight <= 0) {
                throw new ConfigParseException(
                        "Block weight must be positive for '" + key + "', got: " + weight
                );
            }

            // Validate vanilla materials (custom blocks are validated at runtime)
            if (!key.startsWith("oraxen:") && !key.startsWith("itemsadder:")) {
                validateMaterial(key);
            }
        }
    }

    /**
     * Validates a material name.
     *
     * @param materialName the material name to validate
     * @throws ConfigParseException if the material is invalid
     */
    void validateMaterial(String materialName) throws ConfigParseException {
        try {
            ValidationUtils.validateMaterial(materialName);
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException(
                    "Unknown material: '" + materialName + "'. Must be a valid Minecraft material name",
                    e
            );
        }
    }

    /**
     * Validates mask marker material.
     *
     * @param marker the mask marker material name
     * @throws ConfigParseException if the marker is invalid
     */
    void validateMaskMarker(String marker) throws ConfigParseException {
        try {
            ValidationUtils.validateMaterial(marker);
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException("Invalid mask.marker material: '" + marker + "'", e);
        }
    }

    /**
     * Validates a reward configuration.
     *
     * @param reward the reward to validate
     * @throws ConfigParseException if the reward is invalid
     */
    void validateReward(Reward reward) throws ConfigParseException {
        double chance = reward.getChance();
        if (chance < 0 || chance > 100) {
            throw new ConfigParseException(
                    "Reward chance must be between 0 and 100, got: " + chance
            );
        }

        if (reward.getMaterials().isEmpty()) {
            throw new ConfigParseException("Reward 'blocks' list cannot be empty");
        }

        // Validate item amounts
        for (org.bukkit.inventory.ItemStack item : reward.getItems()) {
            if (item.getAmount() <= 0) {
                throw new ConfigParseException(
                        "Item amount must be positive, got: " + item.getAmount()
                );
            }
        }
    }

    /**
     * Validates all rewards in a list.
     *
     * @param rewards the list of rewards to validate
     * @throws ConfigParseException if any reward is invalid
     */
    void validateRewards(List<Reward> rewards) throws ConfigParseException {
        for (Reward reward : rewards) {
            validateReward(reward);
        }
    }
}
