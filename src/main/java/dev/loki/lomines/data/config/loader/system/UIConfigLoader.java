package dev.loki.lomines.data.config.loader.system;

import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.data.config.ui.HologramConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.List;

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
                yaml.getString("ui.timer-format", UIConfig.DEFAULT_TIMER_FORMAT),
                parseHologram(yaml)
        );
    }

    private HologramConfig parseHologram(YamlConfiguration yaml) {
        boolean enabled = yaml.getBoolean("ui.hologram.enabled", false);
        List<String> format = yaml.getStringList("ui.hologram.format");
        if (format.isEmpty()) {
            format = HologramConfig.defaults().format();
        }
        double height = yaml.getDouble("ui.hologram.height", 2.5);
        return new HologramConfig(enabled, format, height);
    }

    /**
     * Saves UI configuration to YAML.
     */
    public void save(YamlConfiguration yaml, UIConfig ui) {
        yaml.set("ui.actionbar.enabled", ui.actionBarEnabled());
        yaml.set("ui.actionbar.format", ui.actionBarFormat());
        yaml.set("ui.actionbar.range", ui.actionBarRange());
        yaml.set("ui.timer-format", ui.timerFormat());
        yaml.set("ui.hologram.enabled", ui.hologram().enabled());
        yaml.set("ui.hologram.format", ui.hologram().format());
        yaml.set("ui.hologram.height", ui.hologram().height());
    }
}
