package dev.loki.lomines.data.config.loader;

import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.data.config.block.FillMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader for blocks configuration section.
 */
public final class BlockConfigLoader {

    /**
     * Parses block configuration from YAML.
     */
    public BlockConfig parse(YamlConfiguration yaml) {
        ConfigurationSection contents = yaml.getConfigurationSection("blocks.contents");
        if (contents == null) {
            contents = yaml.getConfigurationSection("contents"); // Legacy fallback
        }

        if (contents == null) {
            throw new IllegalArgumentException("Missing 'blocks.contents' section");
        }

        Map<BlockKey, Double> weights = new HashMap<>();
        for (String key : contents.getKeys(false)) {
            double weight = contents.getDouble(key);
            if (weight <= 0) continue;
            weights.put(BlockKey.deserialize(key), weight);
        }

        FillMode fillMode = FillMode.valueOf(
                yaml.getString("blocks.fill-mode", "CUBOID").toUpperCase()
        );

        // Parse mask config if applicable
        BlockConfig.MaskConfig mask = null;
        if (fillMode == FillMode.MASK) {
            String markerStr = yaml.getString("blocks.mask.marker", "pink_concrete");
            BlockKey marker = BlockKey.deserialize(markerStr);

            Map<String, Boolean> positions = new HashMap<>();
            List<String> posList = yaml.getStringList("blocks.mask.positions");
            for (String pos : posList) {
                positions.put(pos, true);
            }

            mask = new BlockConfig.MaskConfig(marker, positions);
        }

        return new BlockConfig(weights, fillMode, mask);
    }

    /**
     * Saves block configuration to YAML.
     */
    public void save(YamlConfiguration yaml, BlockConfig blocks) {
        for (var entry : blocks.weights().entrySet()) {
            yaml.set("blocks.contents." + entry.getKey().serialize(), entry.getValue());
        }
        yaml.set("blocks.fill-mode", blocks.fillMode().name().toLowerCase());

        if (blocks.fillMode() == FillMode.MASK && blocks.mask() != null) {
            yaml.set("blocks.mask.marker", blocks.mask().marker().serialize());
            yaml.set("blocks.mask.positions", new ArrayList<>(blocks.mask().positions().keySet()));
        }
    }
}
