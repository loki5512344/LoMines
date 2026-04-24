package com.loki.lomines;

import com.loki.lomines.core.MineTicker;
import com.loki.lomines.core.Mines;
import com.loki.lomines.data.stats.StatsManager;
import com.loki.lomines.integration.IntegrationManager;
import com.loki.lomines.wand.GroupWandManager;
import dev.lolib.commands.CommandManager;
import dev.lolib.core.LoPlugin;
import dev.lolib.core.dependency.DependencyManager;

/**
 * Main plugin class for LoMines.
 * Extends LoPlugin from LoAPI for Paper/Folia compatibility.
 */
public final class LoMinesPlugin extends LoPlugin {

    private Mines mines;
    private MineTicker mineTicker;
    private GroupWandManager groupWandManager;
    private StatsManager statsManager;
    private CommandManager commandManager;
    private IntegrationManager integrationManager;

    @Override
    protected void enable() {
        try {
            saveDefaultConfig();

            ComponentInitializer initializer = new ComponentInitializer(this);
            initializer.createDirectories();

            ComponentInitializer.Components components = initializer.initialize();
            this.mines = components.mines();
            this.groupWandManager = components.groupWandManager();
            this.statsManager = components.statsManager();
            this.commandManager = components.commandManager();
            this.integrationManager = components.integrationManager();

            initializer.loadMines(mines);
            this.mineTicker = initializer.startTicker(mines);

            RegistrationManager registrationManager = new RegistrationManager(this);
            registrationManager.registerCommands(commandManager);
            registrationManager.registerListeners();
            registrationManager.initializeIntegrations(integrationManager);

            initializer.startStatistics(statsManager);

            loLogger().info("LoMines has been enabled!");
        } catch (Exception e) {
            loLogger().error("Failed to enable LoMines: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    protected void disable() {
        try {
            if (mineTicker != null) {
                mineTicker.stop();
                mineTicker = null;
            }

            if (mines != null) {
                mines.getAll().forEach(mine -> mine.stop());
            }

            if (statsManager != null) {
                statsManager.stopAutoSave();
                statsManager.save();
                loLogger().info("Statistics saved successfully");
            }

            loLogger().info("LoMines has been disabled!");
        } catch (Exception e) {
            loLogger().error("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("removal")
    protected void dependencies(DependencyManager dependencyManager) {
        dependencyManager.add("org.apache.commons", "commons-math3", "3.6.1");
        dependencyManager.add("commons-io", "commons-io", "2.18.0");
    }

    public Mines getMines() {
        return mines;
    }

    public GroupWandManager getGroupWandManager() {
        return groupWandManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}
