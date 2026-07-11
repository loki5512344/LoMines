package dev.loki.lomines.data.reward.parse.item;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.util.ValidationUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Parses ItemStack objects from YAML reward item configurations.
 * Handles material type, amount, and basic item properties.
 */
public final class RewardItemParser {

    /**
     * Parses a single reward item from a map configuration.
     *
     * @param itemMap the item configuration map
     * @param index   the index of the item in the list (for error messages)
     * @return the parsed ItemStack
     * @throws ConfigParseException if the item configuration is invalid
     */
    public ItemStack parseItem(Map<String, Object> itemMap, int index) throws ConfigParseException {
        if (!itemMap.containsKey("type")) {
            throw new ConfigParseException("Missing required 'type' field in item at index " + index);
        }

        String typeName = String.valueOf(itemMap.get("type"));
        Material material = parseMaterial(typeName);
        int amount = parseAmount(itemMap);

        return new ItemStack(material, amount);
    }

    /**
     * Parses a material from a string name.
     *
     * @param typeName the material name
     * @return the parsed Material
     * @throws ConfigParseException if the material is unknown
     */
    private Material parseMaterial(String typeName) throws ConfigParseException {
        try {
            return ValidationUtils.validateMaterial(typeName);
        } catch (IllegalArgumentException e) {
            throw new ConfigParseException(
                    "Unknown material in item type: '" + typeName + "'",
                    e
            );
        }
    }

    /**
     * Parses the amount field from an item configuration.
     *
     * @param itemMap the item configuration map
     * @return the parsed amount (defaults to 1 if not specified)
     * @throws ConfigParseException if the amount value is invalid
     */
    private int parseAmount(Map<String, Object> itemMap) throws ConfigParseException {
        if (!itemMap.containsKey("amount")) {
            return 1;
        }

        Object amountObj = itemMap.get("amount");
        try {
            if (amountObj instanceof Number) {
                return ((Number) amountObj).intValue();
            } else if (amountObj instanceof String) {
                return Integer.parseInt((String) amountObj);
            } else {
                throw new ConfigParseException(
                        "Invalid 'amount' type: expected number, got " +
                                (amountObj != null ? amountObj.getClass().getSimpleName() : "null")
                );
            }
        } catch (NumberFormatException e) {
            throw new ConfigParseException("Invalid 'amount' value: " + amountObj, e);
        }
    }
}
