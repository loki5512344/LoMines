package dev.loki.lomines;

import dev.loki.lomines.command.admin.AdminCommands;
import dev.loki.lomines.command.admin.MaskCommands;
import dev.loki.lomines.command.admin.StatsCommands;
import dev.loki.lomines.command.common.LoMinesTabCompleter;
import dev.loki.lomines.command.player.PlayerCommands;
import dev.loki.lomines.integration.IntegrationManager;
import dev.loki.lomines.listener.BlockBreakListener;
import dev.loki.lomines.listener.GroupGuiListener;
import dev.loki.lomines.listener.MineEditGuiListener;
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

    void registerTabCompleter() {
        LoMinesTabCompleter tabCompleter = new LoMinesTabCompleter(plugin);
        plugin.getCommand("lm").setTabCompleter(tabCompleter);
        plugin.getCommand("lomines").setTabCompleter(tabCompleter);
        plugin.getCommand("mine").setTabCompleter(tabCompleter);
        plugin.getCommand("mines").setTabCompleter(tabCompleter);

        plugin.loLogger().info("Tab completer registered");
    }

    void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GroupGuiListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MineEditGuiListener(plugin), plugin);

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
