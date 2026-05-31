package dev.loki.lomines.data.config.loader.system;

import dev.loki.lomines.data.config.ui.UIConfig;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loader for UI configuration section.
 */
public final class UIConfigLoader {

    private final YamlConfiguration defaults;

    public UIConfigLoader(YamlConfiguration defaults) {
        this.defaults = defaults;
    }

    /**
     * Parses UI configuration from YAML.
     */
    public UIConfig parse(YamlConfiguration yaml) {
        boolean actionBarEnabled = yaml.getBoolean("ui.actionbar.enabled",
                defaults.getBoolean("ui.actionbar.enabled", true));

        return new UIConfig(
                actionBarEnabled,
                yaml.getString("ui.actionbar.format", UIConfig.DEFAULT_ACTIONBAR_FORMAT),
                yaml.getDouble("ui.actionbar.range", 50.0),
                yaml.getString("ui.timer-format", UIConfig.DEFAULT_TIMER_FORMAT)
        );
    }

    /**
     * Saves UI configuration to YAML.
     */
    public void save(YamlConfiguration yaml, UIConfig ui) {
        yaml.set("ui.actionbar.enabled", ui.actionBarEnabled());
        yaml.set("ui.actionbar.format", ui.actionBarFormat());
        yaml.set("ui.actionbar.range", ui.actionBarRange());
        yaml.set("ui.timer-format", ui.timerFormat());
    }
}
