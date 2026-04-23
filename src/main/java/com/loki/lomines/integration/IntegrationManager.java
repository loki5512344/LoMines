package com.loki.lomines.integration;

import com.loki.lomines.LoMinesPlugin;
import org.bukkit.Bukkit;

/**
 * Manages integrations with external plugins.
 * This is a placeholder implementation that will be completed in task 10.1.
 */
public final class IntegrationManager {
    
    private final LoMinesPlugin plugin;
    
    public IntegrationManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initializes all available integrations.
     */
    public void initAll() {
        // Check for PlaceholderAPI
        if (isPluginEnabled("PlaceholderAPI")) {
            plugin.loLogger().info("PlaceholderAPI integration available");
        }
        
        // Check for WorldGuard
        if (isPluginEnabled("WorldGuard")) {
            plugin.loLogger().info("WorldGuard integration available");
        }
        
        // Check for Oraxen
        if (isPluginEnabled("Oraxen")) {
            plugin.loLogger().info("Oraxen integration available");
        }
        
        // Check for ItemsAdder
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
}
