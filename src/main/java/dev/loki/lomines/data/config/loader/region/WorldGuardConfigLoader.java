package dev.loki.lomines.data.config.loader.region;

import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.integration.worldguard.WorldGuardConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader for WorldGuard configuration section.
 */
public final class WorldGuardConfigLoader {

    private final YamlConfiguration defaults;

    public WorldGuardConfigLoader(YamlConfiguration defaults) {
        this.defaults = defaults;
    }

    /**
     * Parses WorldGuard configuration from YAML.
     */
    public WorldGuardConfig parse(YamlConfiguration yaml) {
        ConfigurationSection wgSection = yaml.getConfigurationSection("worldguard");
        if (wgSection == null) {
            // Check defaults
            boolean defaultEnabled = defaults.getBoolean("worldguard.enabled", false);
            if (!defaultEnabled) {
                return WorldGuardConfig.disabled();
            }
        }

        boolean enabled = yaml.getBoolean("worldguard.enabled",
                defaults.getBoolean("worldguard.enabled", false));

        if (!enabled) {
            return WorldGuardConfig.disabled();
        }

        String template = yaml.getString("worldguard.region-template",
                defaults.getString("worldguard.region-template", "{mine_name}_{random_4}"));

        List<String> owners = yaml.getStringList("worldguard.owners");
        if (owners.isEmpty()) {
            owners = defaults.getStringList("worldguard.owners");
        }

        List<String> members = yaml.getStringList("worldguard.members");
        if (members.isEmpty()) {
            members = defaults.getStringList("worldguard.members");
        }

        List<String> flags = yaml.getStringList("worldguard.flags");
        if (flags.isEmpty()) {
            flags = defaults.getStringList("worldguard.flags");
        }
        if (flags.isEmpty()) {
            flags = List.of("passthrough=deny", "build=allow");
        }

        boolean protect = yaml.getBoolean("worldguard.protect-on-create",
                defaults.getBoolean("worldguard.protect-on-create", true));

        return WorldGuardConfig.builder()
                .enabled(true)
                .template(template)
                .owners(owners)
                .members(members)
                .flags(flags)
                .protectOnCreate(protect)
                .build();
    }

    /**
     * Saves WorldGuard configuration to YAML.
     */
    public void save(YamlConfiguration yaml, WorldGuardConfig config) {
        yaml.set("worldguard.enabled", config.enabled());

        if (config.enabled()) {
            yaml.set("worldguard.region-template", config.regionNameTemplate());
            yaml.set("worldguard.owners", config.owners());
            yaml.set("worldguard.members", config.members());
            yaml.set("worldguard.flags", config.flags());
            yaml.set("worldguard.protect-on-create", config.protectOnCreate());
        }
    }

    /**
     * Sets default values for WorldGuard configuration.
     */
    public void setDefaults(YamlConfiguration yaml) {
        yaml.set("worldguard.enabled", false);
        yaml.set("worldguard.region-template", "{mine_name}_{random_4}");
        yaml.set("worldguard.owners", List.of());
        yaml.set("worldguard.members", List.of());
        yaml.set("worldguard.flags", List.of("passthrough=deny", "build=allow"));
        yaml.set("worldguard.protect-on-create", true);
    }
}
