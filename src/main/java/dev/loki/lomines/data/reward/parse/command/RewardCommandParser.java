package dev.loki.lomines.data.reward.parse.command;

import dev.loki.lomines.data.config.parser.ConfigParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses reward commands from configuration.
 */
public final class RewardCommandParser {

    @SuppressWarnings("unchecked")
    public List<String> parseCommands(Map<String, Object> rewardMap) throws ConfigParseException {
        List<String> commands = new ArrayList<>();
        if (!rewardMap.containsKey("commands")) {
            return commands;
        }
        Object commandsObj = rewardMap.get("commands");
        if (!(commandsObj instanceof List)) {
            throw new ConfigParseException(
                    "Invalid 'commands' type: expected list, got " +
                            (commandsObj != null ? commandsObj.getClass().getSimpleName() : "null")
            );
        }
        List<String> commandsList = (List<String>) commandsObj;
        for (String command : commandsList) {
            if (command == null || command.trim().isEmpty()) {
                throw new ConfigParseException("Command cannot be null or empty");
            }
            commands.add(command);
        }
        return commands;
    }
}
