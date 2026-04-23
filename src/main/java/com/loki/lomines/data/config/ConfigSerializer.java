package com.loki.lomines.data.config;

import com.loki.lomines.util.LocationParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Serializes MineConfig objects to YAML format.
 * Responsible only for writing MineConfig data to YAML configuration.
 */
final class ConfigSerializer {

    /**
     * Serializes a MineConfig to YAML configuration.
     *
     * @param config the MineConfig to serialize
     * @param yaml the YAML configuration to write to
     */
    void serialize(MineConfig config, YamlConfiguration yaml) {
        saveSelections(yaml, config.getSelections());
        saveBlocks(yaml, config.getBlocks());
        yaml.set("fill-mode", config.getFillMode().name().toLowerCase(Locale.ROOT));
        yaml.set("mask.marker", config.getMaskMarkerMaterial());
        saveMaskPositions(yaml, config.getMaskPositions());
        saveRewards(yaml, config.getRewards());

        yaml.set("reset.ticks", config.getResetTicks());
        yaml.set("reset.percent", config.getResetPercent());
        yaml.set("reset-on-percent", config.isResetOnPercentEnabled());
        yaml.set("reset-commands", config.getResetCommands());
        yaml.set("broadcast-reset", config.getBroadcastReset());

        yaml.set("teleport-on-reset", config.isTeleportOnReset());
        if (config.getTeleportLocation() != null) {
            yaml.set("teleport-location", LocationParser.format(config.getTeleportLocation()));
        }

        yaml.set("actionbar.enabled", config.isActionBarEnabled());
        yaml.set("actionbar.message", config.getActionBarMessage());
        yaml.set("actionbar.range", config.getActionBarRange());

        yaml.set("timer-format", config.getTimerFormat());
    }

    /**
     * Saves selection coordinates to the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @param selections the list of selection locations
     */
    private void saveSelections(YamlConfiguration yaml, List<Location> selections) {
        for (int i = 0; i < selections.size(); i++) {
            Location location = selections.get(i);
            String key = "selection." + (i + 1);
            yaml.set(key, LocationParser.format(location));
        }
    }

    /**
     * Saves block contents to the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @param blocks the map of block string key to weight
     */
    private void saveBlocks(YamlConfiguration yaml, Map<String, Double> blocks) {
        for (Map.Entry<String, Double> entry : blocks.entrySet()) {
            String blockKey = entry.getKey();
            Double weight = entry.getValue();
            yaml.set("contents." + blockKey, weight);
        }
    }

    /**
     * Saves mask positions to the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @param maskPositions the list of mask positions
     */
    private void saveMaskPositions(YamlConfiguration yaml, List<Location> maskPositions) {
        if (maskPositions == null || maskPositions.isEmpty()) {
            yaml.set("mask.positions", new ArrayList<String>());
            return;
        }

        List<String> lines = new ArrayList<>();
        for (Location loc : maskPositions) {
            lines.add(LocationParser.format(loc));
        }
        yaml.set("mask.positions", lines);
    }

    /**
     * Saves rewards to the YAML configuration.
     *
     * @param yaml the YAML configuration
     * @param rewards the list of rewards
     */
    private void saveRewards(YamlConfiguration yaml, List<Reward> rewards) {
        if (rewards.isEmpty()) {
            return;
        }

        List<Map<String, Object>> rewardsList = new ArrayList<>();

        for (Reward reward : rewards) {
            Map<String, Object> rewardMap = new HashMap<>();

            rewardMap.put("chance", reward.getChance());
            rewardMap.put("prevent-drops", reward.isPreventDrops());

            List<String> blockNames = new ArrayList<>();
            for (Material material : reward.getMaterials()) {
                blockNames.add(material.name().toLowerCase());
            }
            rewardMap.put("blocks", blockNames);

            if (!reward.getItems().isEmpty()) {
                List<Map<String, Object>> itemsList = new ArrayList<>();
                for (ItemStack item : reward.getItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("type", item.getType().name().toLowerCase());
                    itemMap.put("amount", item.getAmount());
                    itemsList.add(itemMap);
                }
                rewardMap.put("items", itemsList);
            }

            if (!reward.getCommands().isEmpty()) {
                rewardMap.put("commands", new ArrayList<>(reward.getCommands()));
            }

            rewardsList.add(rewardMap);
        }

        yaml.set("random-rewards", rewardsList);
    }
}
