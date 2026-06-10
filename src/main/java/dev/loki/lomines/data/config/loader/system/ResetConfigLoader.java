package dev.loki.lomines.data.config.loader.system;

import dev.loki.lomines.data.config.reset.ResetConfig;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loader for reset configuration section.
 */
public final class ResetConfigLoader {

    private final YamlConfiguration defaults;

    public ResetConfigLoader(YamlConfiguration defaults) {
        this.defaults = defaults;
    }

    /**
     * Parses reset configuration from YAML.
     */
    public ResetConfig parse(YamlConfiguration yaml) {
        String intervalStr = yaml.getString("reset.interval",
                defaults.getString("reset.interval", "5m"));

        return ResetConfig.builder()
                .interval(intervalStr)
                .percentTrigger(yaml.getDouble("reset.percent-trigger",
                        defaults.getDouble("reset.percent-trigger", 10.0)))
                .percentEnabled(yaml.getBoolean("reset.percent-enabled",
                        defaults.getBoolean("reset.percent-enabled", false)))
                .commands(yaml.getStringList("reset.commands"))
                .broadcastMessage(yaml.getString("reset.broadcast", ""))
                .build();
    }

    /**
     * Saves reset configuration to YAML.
     */
    public void save(YamlConfiguration yaml, ResetConfig reset) {
        yaml.set("reset.interval", reset.intervalDisplay());
        yaml.set("reset.percent-trigger", reset.percentTrigger());
        yaml.set("reset.percent-enabled", reset.percentEnabled());
        yaml.set("reset.commands", reset.commands());
        yaml.set("reset.broadcast", reset.broadcastMessage());
    }
}
