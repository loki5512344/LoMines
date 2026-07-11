package dev.loki.lomines.data.reward.parse.item;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.util.ValidationUtils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses reward materials (blocks) from configuration.
 */
public final class RewardMaterialParser {

    @SuppressWarnings("unchecked")
    public List<Material> parseMaterials(Map<String, Object> rewardMap) throws ConfigParseException {
        if (!rewardMap.containsKey("blocks")) {
            throw new ConfigParseException("Missing required 'blocks' field in reward");
        }
        Object blocksObj = rewardMap.get("blocks");
        if (!(blocksObj instanceof List)) {
            throw new ConfigParseException(
                    "Invalid 'blocks' type: expected list, got " +
                            (blocksObj != null ? blocksObj.getClass().getSimpleName() : "null")
            );
        }
        List<String> blocksList = (List<String>) blocksObj;
        List<Material> materials = new ArrayList<>();
        for (String blockName : blocksList) {
            if (blockName == null || blockName.trim().isEmpty()) {
                throw new ConfigParseException("Block name cannot be null or empty");
            }
            try {
                Material material = ValidationUtils.validateMaterial(blockName);
                materials.add(material);
            } catch (IllegalArgumentException e) {
                throw new ConfigParseException(
                        "Unknown material in reward blocks: '" + blockName + "'",
                        e
                );
            }
        }
        return materials;
    }
}
