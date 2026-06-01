package dev.loki.lomines.core.mine;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.common.MineLoader;
import dev.loki.lomines.core.service.MaskScanService;
import dev.loki.lomines.core.service.MineFileManager;
import dev.loki.lomines.core.service.MineRepository;
import dev.loki.lomines.data.config.loader.common.ConfigLoader;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.integration.worldguard.WorldGuardRegionService;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Registry for all active mines.
 * Manages the lifecycle of mines including loading, creating, and deleting.
 */
public final class Mines {

    private final LoMinesPlugin plugin;
    private final MineFileManager fileManager;
    private final MineRepository repository;
    private final MineLoader loader;
    private final MaskScanService maskScanService;
    private final WorldGuardRegionService worldGuardService;

    public Mines(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.fileManager = new MineFileManager(plugin.getDataFolder().toPath().resolve("mines"));
        this.repository = new MineRepository(plugin, fileManager);
        this.loader = new MineLoader(plugin, fileManager, repository);
        this.maskScanService = new MaskScanService(repository, fileManager);
        this.worldGuardService = new WorldGuardRegionService(plugin);
    }

    public void loadAll() throws IOException {
        loader.loadAll();
    }

    public void create(String name) throws IOException {
        create(name, null, null);
    }

    public void create(String name, Location corner1, Location corner2) throws IOException {
        if (repository.exists(name)) {
            throw new IllegalArgumentException("Mine already exists: " + name);
        }
        if (corner1 != null && corner2 != null && !corner1.getWorld().equals(corner2.getWorld())) {
            throw new IllegalArgumentException("Selection corners must be in the same world");
        }

        fileManager.ensureFolderExists();
        fileManager.createDefaultConfig(name, corner1, corner2);

        MineConfig config;
        try {
            config = fileManager.loadConfig(name);
        } catch (ConfigLoader.ConfigLoadException e) {
            throw new IOException("Failed to parse created mine configuration: " + e.getMessage(), e);
        }

        worldGuardService.createRegion(name, config);
        repository.createAndStart(name, config);
        plugin.loLogger().info("Created mine: " + name);
    }

    public void delete(String name) throws IOException {
        Mine mine = repository.remove(name);
        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }

        mine.stop();
        worldGuardService.deleteRegion(name, mine.getConfig());
        fileManager.deleteConfig(name);
    }

    public Mine get(String name) {
        return repository.get(name);
    }

    public Optional<Mine> find(String name) {
        return repository.find(name);
    }

    public Collection<Mine> getAll() {
        return repository.getAll();
    }

    public Optional<Mine> findByLocation(Location location) {
        return repository.findByLocation(location);
    }

    public int scanAndSaveMask(String mineName) throws IOException, ConfigLoader.ConfigLoadException {
        int count = maskScanService.scanAndSave(mineName);
        plugin.loLogger().info("Mask scan for '" + mineName + "': " + count + " marker block(s)");
        return count;
    }

    public void reloadMine(String name) throws IOException, ConfigLoader.ConfigLoadException {
        Mine oldMine = repository.find(name).orElse(null);
        repository.reload(name);

        if (oldMine != null) {
            Mine newMine = repository.find(name).orElse(null);
            if (newMine != null) {
                worldGuardService.updateRegion(name, newMine.getConfig());
            }
        }
    }

    /**
     * Updates mine configuration and saves to disk.
     * Used for modifying regions, blocks, etc.
     */
    public void updateMineConfig(String name, MineConfig newConfig) throws IOException {
        if (!repository.exists(name)) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }

        // Save new config to file
        fileManager.saveConfig(name, newConfig);

        // Reload to apply changes
        try {
            Mine oldMine = repository.find(name).orElse(null);
            repository.reload(name);

            if (oldMine != null) {
                Mine newMine = repository.find(name).orElse(null);
                if (newMine != null) {
                    worldGuardService.updateRegion(name, newMine.getConfig());
                }
            }
        } catch (ConfigLoader.ConfigLoadException e) {
            throw new IOException("Failed to reload mine after config update: " + e.getMessage(), e);
        }
    }

    public WorldGuardRegionService getWorldGuardService() {
        return worldGuardService;
    }

    public MineFileManager getFileManager() {
        return fileManager;
    }
}
