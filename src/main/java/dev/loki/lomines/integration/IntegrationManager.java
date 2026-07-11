package dev.loki.lomines.integration;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.integration.placeholder.LoMinesPlaceholderExpansion;
import org.bukkit.Bukkit;

/**
 * Manages integrations with external plugins.
 * Currently supports: WorldGuard (regions), PlaceholderAPI (placeholders)
 */
public final class IntegrationManager {

    private final LoMinesPlugin plugin;
    private LoMinesPlaceholderExpansion placeholderExpansion;

    public IntegrationManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes all available integrations.
     */
    public void initAll() {
        initPlaceholderAPI();
        checkWorldGuard();
    }

    /**
     * Registers PlaceholderAPI expansion if available.
     */
    private void initPlaceholderAPI() {
        if (isPluginEnabled("PlaceholderAPI")) {
            try {
                placeholderExpansion = new LoMinesPlaceholderExpansion(plugin);
                if (placeholderExpansion.register()) {
                    plugin.getLogger().info("PlaceholderAPI integration enabled");
                    plugin.getLogger().info("Available placeholders: %lomines_mine_<name>_percent%, %lomines_player_blocksmined%, etc.");
                } else {
                    plugin.getLogger().warning("Failed to register PlaceholderAPI expansion");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to enable PlaceholderAPI integration: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void checkWorldGuard() {
        if (isPluginEnabled("WorldGuard")) {
            plugin.getLogger().info("WorldGuard detected - auto-region creation enabled");
        }
    }

    /**
     * Checks if a plugin is enabled.
     */
    public boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null &&
                Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    /**
     * Unregisters all integrations.
     */
    public void shutdown() {
        if (placeholderExpansion != null) {
            try {
                placeholderExpansion.unregister();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unregister PlaceholderAPI expansion: " + e.getMessage());
            }
        }
    }
}
