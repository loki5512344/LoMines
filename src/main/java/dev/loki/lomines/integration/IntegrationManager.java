package dev.loki.lomines.integration;

import dev.loki.lomines.LoMinesPlugin;
import org.bukkit.Bukkit;

/**
 * Manages integrations with external plugins.
 */
public final class IntegrationManager {

    private final LoMinesPlugin plugin;
    // private PlaceholderAPIIntegration placeholderAPI;

    public IntegrationManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes all available integrations.
     */
    public void initAll() {
        // initPlaceholderAPI();
        checkWorldGuard();
        checkOraxen();
        checkItemsAdder();
    }

    // private void initPlaceholderAPI() {
    //     if (isPluginEnabled("PlaceholderAPI")) {
    //         try {
    //             placeholderAPI = new PlaceholderAPIIntegration(plugin);
    //             placeholderAPI.register();
    //             plugin.loLogger().info("PlaceholderAPI integration enabled");
    //         } catch (Exception e) {
    //             plugin.loLogger().warn("Failed to enable PlaceholderAPI integration: " + e.getMessage());
    //         }
    //     }
    // }

    private void checkWorldGuard() {
        if (isPluginEnabled("WorldGuard")) {
            plugin.loLogger().info("WorldGuard detected (integration not yet implemented)");
        }
    }

    private void checkOraxen() {
        if (isPluginEnabled("Oraxen")) {
            plugin.loLogger().info("Oraxen integration available");
        }
    }

    private void checkItemsAdder() {
        if (isPluginEnabled("ItemsAdder")) {
            plugin.loLogger().info("ItemsAdder integration available");
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
        // if (placeholderAPI != null) {
        //     placeholderAPI.unregister();
        // }
    }
}
