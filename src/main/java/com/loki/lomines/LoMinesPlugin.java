package com.loki.lomines;

import dev.lolib.core.LoPlugin;
import dev.lolib.commands.CommandManager;
import dev.lolib.core.dependency.DependencyManager;
import com.loki.lomines.core.MineTicker;
import com.loki.lomines.core.Mines;
import com.loki.lomines.data.StatsManager;
import com.loki.lomines.integration.IntegrationManager;
import com.loki.lomines.listener.BlockBreakListener;
import com.loki.lomines.listener.GroupGuiListener;
import com.loki.lomines.listener.PlayerInteractListener;
import com.loki.lomines.listener.PlayerJoinListener;
import com.loki.lomines.wand.GroupWandManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
            // Save default config if not exists
            saveDefaultConfig();
            
            // Create necessary directories
            createDirectories();
            
            // Initialize core components
            initializeComponents();
            
            // Load all mines from mines/ folder
            loadMines();
            
            this.mineTicker = new MineTicker(mines, this);
            mineTicker.start();
            
            // Register commands
            registerCommands();
            
            // Register event listeners
            registerListeners();
            
            // Initialize integrations (PlaceholderAPI, WorldGuard, etc.)
            initializeIntegrations();
            
            // Start statistics auto-save if enabled
            startStatisticsAutoSave();
            
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
            // Stop all mine tasks
            if (mines != null) {
                mines.getAll().forEach(mine -> mine.stop());
            }
            
            // Save statistics
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
        // Load Apache Commons Math3 for EnumeratedDistribution
        dependencyManager.add("org.apache.commons", "commons-math3", "3.6.1");

        // Load Apache Commons IO for file utilities
        dependencyManager.add("commons-io", "commons-io", "2.18.0");
    }
    
    /**
     * Creates necessary directories for plugin data.
     */
    private void createDirectories() throws IOException {
        Path dataFolder = getDataFolder().toPath();
        Path minesFolder = dataFolder.resolve("mines");
        
        if (!Files.exists(minesFolder)) {
            Files.createDirectories(minesFolder);
            loLogger().info("Created mines directory");
        }
    }
    
    /**
     * Initializes core plugin components.
     */
    private void initializeComponents() {
        this.mines = new Mines(this);
        this.groupWandManager = new GroupWandManager();
        this.statsManager = new StatsManager(this);
        this.commandManager = new CommandManager(this);
        this.integrationManager = new IntegrationManager(this);
        
        loLogger().info("Core components initialized");
    }
    
    /**
     * Loads all mines from the mines/ folder.
     */
    private void loadMines() {
        try {
            mines.loadAll();
            loLogger().info("Loaded " + mines.getAll().size() + " mine(s)");
        } catch (IOException e) {
            loLogger().error("Failed to load mines: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registers all plugin commands.
     */
    private void registerCommands() {
        commandManager.register(new com.loki.lomines.command.AdminCommands(this));
        commandManager.register(new com.loki.lomines.command.PlayerCommands(this));
        commandManager.register(new com.loki.lomines.command.StatsCommands(this));
        commandManager.register(new com.loki.lomines.command.MaskCommands(this));

        loLogger().info("Commands registered");
    }
    
    /**
     * Registers all event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new GroupGuiListener(this), this);
        
        FileConfiguration config = getConfig();
        if (config.getBoolean("statistics-enabled", true)) {
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        }
        
        loLogger().info("Event listeners registered");
    }
    
    /**
     * Initializes integrations with external plugins.
     */
    private void initializeIntegrations() {
        integrationManager.initAll();
        loLogger().info("Integrations initialized");
    }
    
    /**
     * Starts statistics auto-save if enabled in config.
     */
    private void startStatisticsAutoSave() {
        FileConfiguration config = getConfig();
        if (config.getBoolean("statistics-enabled", true)) {
            try {
                statsManager.load();
                statsManager.startAutoSave();
                loLogger().info("Statistics system enabled");
            } catch (IOException e) {
                loLogger().warn("Failed to load statistics: " + e.getMessage());
            }
        }
    }
    
    // Getters for component access
    
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
