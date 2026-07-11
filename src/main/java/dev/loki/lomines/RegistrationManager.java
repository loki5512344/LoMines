package dev.loki.lomines;

import dev.loki.lomines.command.LmCommand;
import dev.loki.lomines.integration.IntegrationManager;
import dev.loki.lomines.listener.block.BlockBreakListener;
import dev.loki.lomines.listener.gui.GroupGuiListener;
import dev.loki.lomines.listener.gui.MineEditGuiListener;
import dev.loki.lomines.listener.player.PlayerInteractListener;
import dev.loki.lomines.listener.player.PlayerJoinListener;

final class RegistrationManager {

    private final LoMinesPlugin plugin;

    RegistrationManager(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    void registerCommands() {
        LmCommand executor = new LmCommand(plugin);
        for (String alias : new String[]{"lm", "lomines", "mine", "mines"}) {
            var cmd = plugin.getCommand(alias);
            if (cmd != null) {
                cmd.setExecutor(executor);
                cmd.setTabCompleter(executor);
            }
        }
        plugin.getLogger().info("Commands registered");
    }

    void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GroupGuiListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MineEditGuiListener(plugin), plugin);

        if (plugin.getConfig().getBoolean("statistics-enabled", true)) {
            plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin), plugin);
        }
        plugin.getLogger().info("Event listeners registered");
    }

    void initializeIntegrations(IntegrationManager integrationManager) {
        integrationManager.initAll();
        plugin.getLogger().info("Integrations initialized");
    }
}
