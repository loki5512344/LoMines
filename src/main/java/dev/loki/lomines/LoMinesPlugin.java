package dev.loki.lomines;

import dev.loki.lomines.core.mine.service.MineTicker;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.data.stats.service.StatsManager;
import dev.loki.lomines.integration.IntegrationManager;
import dev.loki.lomines.integration.hologram.HologramManager;
import dev.loki.lomines.wand.WandParticleService;
import dev.loki.lomines.wand.group.GroupWandManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoMinesPlugin extends JavaPlugin {

    private Mines mines;
    private MineTicker mineTicker;
    private GroupWandManager groupWandManager;
    private StatsManager statsManager;
    private IntegrationManager integrationManager;
    private WandParticleService wandParticleService;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();

            ComponentInitializer initializer = new ComponentInitializer(this);
            initializer.createDirectories();

            ComponentInitializer.Components components = initializer.initialize();
            this.mines = components.mines();
            this.groupWandManager = components.groupWandManager();
            this.statsManager = components.statsManager();
            this.integrationManager = components.integrationManager();
            this.wandParticleService = new WandParticleService(this);
            this.hologramManager = new HologramManager(this);

            initializer.loadMines(mines);
            this.mineTicker = initializer.startTicker(mines);

            RegistrationManager registrationManager = new RegistrationManager(this);
            registrationManager.registerCommands();
            registrationManager.registerListeners();
            registrationManager.initializeIntegrations(integrationManager);

            initializer.startStatistics(statsManager);

            getLogger().info("LoMines has been enabled!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable LoMines: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
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
                getLogger().info("Statistics saved successfully");
            }
            if (integrationManager != null) {
                integrationManager.shutdown();
            }
            if (wandParticleService != null) {
                wandParticleService.stopAll();
            }
            if (hologramManager != null) {
                hologramManager.shutdown();
            }
            getLogger().info("LoMines has been disabled!");
        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
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

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public WandParticleService getWandParticleService() {
        return wandParticleService;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
