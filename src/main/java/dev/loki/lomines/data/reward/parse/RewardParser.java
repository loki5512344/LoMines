package dev.loki.lomines.data.reward.parse;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.data.reward.entity.Reward;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RewardParser {

    private final RewardEntryParser entryParser;

    public RewardParser() {
        this.entryParser = new RewardEntryParser();
    }

    public List<Reward> parseRewards(YamlConfiguration yaml) throws ConfigParseException {
        List<Reward> rewards = new ArrayList<>();

        if (!yaml.contains("random-rewards")) {
            return rewards;
        }

        List<?> rewardsList = yaml.getList("random-rewards");
        if (rewardsList == null) {
            return rewards;
        }

        for (int i = 0; i < rewardsList.size(); i++) {
            Object rewardObj = rewardsList.get(i);

            if (!(rewardObj instanceof Map)) {
                throw new ConfigParseException(
                        "Invalid reward at index " + i + ": expected map, got " +
                                (rewardObj != null ? rewardObj.getClass().getSimpleName() : "null")
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> rewardMap = (Map<String, Object>) rewardObj;

            try {
                Reward reward = entryParser.parseReward(rewardMap);
                rewards.add(reward);
            } catch (ConfigParseException e) {
                throw new ConfigParseException(
                        "Error parsing reward at index " + i + ": " + e.getMessage(),
                        e
                );
            }
        }

        return rewards;
    }
}
