package dev.loki.lomines;

import dev.loki.lomines.command.AdminCommands;
import dev.loki.lomines.command.MaskCommands;
import dev.loki.lomines.command.PlayerCommands;
import dev.loki.lomines.command.StatsCommands;
import dev.loki.lomines.integration.IntegrationManager;
import dev.loki.lomines.listener.BlockBreakListener;
import dev.loki.lomines.listener.GroupGuiListener;
import dev.loki.lomines.listener.PlayerInteractListener;
import dev.loki.lomines.listener.PlayerJoinListener;
import dev.lolib.commands.CommandManager;

/**
 * Handles registration of commands, listeners, and integrations.
 */
final class RegistrationManager {

    private final LoMinesPlugin plugin;

    RegistrationManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    void registerCommands(CommandManager commandManager) {
        commandManager.register(new AdminCommands(plugin));
        commandManager.register(new PlayerCommands(plugin));
        commandManager.register(new StatsCommands(plugin));
        commandManager.register(new MaskCommands(plugin));

        plugin.loLogger().info("Commands registered");
    }

    void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GroupGuiListener(plugin), plugin);

        if (plugin.getConfig().getBoolean("statistics-enabled", true)) {
            plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin), plugin);
        }

        plugin.loLogger().info("Event listeners registered");
    }

    void initializeIntegrations(IntegrationManager integrationManager) {
        integrationManager.initAll();
        plugin.loLogger().info("Integrations initialized");
    }
}
