package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.service.MaskScanService;
import dev.loki.lomines.core.service.MineFileManager;
import dev.loki.lomines.core.service.MineRepository;
import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.integration.worldguard.WorldGuardRegionService;
import org.bukkit.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Registry for all active mines.
 * Manages the lifecycle of mines including loading, creating, and deleting.
 * Updated for new configuration system (v2).
 */
public final class Mines {

    private final LoMinesPlugin plugin;
    private final MineFileManager fileManager;
    private final MineRepository repository;
    private final MaskScanService maskScanService;
    private final WorldGuardRegionService worldGuardService;

    public Mines(LoMinesPlugin plugin) {
        this.plugin = plugin;
        Path minesFolder = plugin.getDataFolder().toPath().resolve("mines");
        this.fileManager = new MineFileManager(minesFolder);
        this.repository = new MineRepository(plugin, fileManager);
        this.maskScanService = new MaskScanService(repository, fileManager);
        this.worldGuardService = new WorldGuardRegionService(plugin);
    }

    /**
     * Loads all mines from the mines/ folder.
     * Clears and replaces any previously loaded mines (used for full reload).
     */
    public void loadAll() throws IOException {
        repository.clear();

        Path minesFolder = fileManager.getMinesFolder();
        if (!Files.exists(minesFolder)) {
            return;
        }

        try (Stream<Path> paths = Files.list(minesFolder)) {
            paths.filter(path -> path.toString().endsWith(".yml"))
                    .filter(path -> !path.getFileName().toString().startsWith("_")) // Skip defaults
                    .forEach(configFile -> loadMineFromFile(configFile));
        }
    }

    private void loadMineFromFile(Path configFile) {
        try {
            String fileName = configFile.getFileName().toString();
            String mineName = fileName.substring(0, fileName.length() - 4);

            MineConfig config = fileManager.loadConfig(mineName);
            repository.createAndStart(mineName, config);

            plugin.loLogger().info("Loaded mine: " + mineName);
        } catch (IOException | ConfigLoader.ConfigLoadException e) {
            plugin.loLogger().error("Failed to load mine from " + configFile.getFileName() + ": " + e.getMessage());
        }
    }

    /**
     * Creates a new mine with the given name and default cuboid corners.
     */
    public void create(String name) throws IOException {
        create(name, null, null);
    }

    /**
     * Creates a new mine; optional corners define {@code selection.1} and {@code selection.2} (same world required).
     * If WorldGuard integration is enabled, creates a region for the mine.
     */
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

        // Create WorldGuard region if enabled
        String regionName = worldGuardService.createRegion(name, config);
        if (regionName != null) {
            plugin.loLogger().info("Created WorldGuard region '" + regionName + "' for mine: " + name);
        }

        repository.createAndStart(name, config);
        plugin.loLogger().info("Created mine: " + name);
    }

    /**
     * Deletes a mine by name.
     * Also deletes the associated WorldGuard region if it exists.
     */
    public void delete(String name) throws IOException {
        Mine mine = repository.remove(name);

        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }

        mine.stop();

        // Delete WorldGuard region if exists
        MineConfig config = mine.getConfig();
        boolean wgDeleted = worldGuardService.deleteRegion(name, config);
        if (wgDeleted) {
            plugin.loLogger().info("Deleted WorldGuard region for mine: " + name);
        }

        fileManager.deleteConfig(name);
    }

    /**
     * Gets a mine by name.
     *
     * @throws IllegalArgumentException if mine not found
     */
    public Mine get(String name) {
        return repository.get(name);
    }

    /**
     * Finds a mine by name, returning Optional.
     */
    public Optional<Mine> find(String name) {
        return repository.find(name);
    }

    /**
     * Returns all mines.
     */
    public Collection<Mine> getAll() {
        return repository.getAll();
    }

    /**
     * Finds a mine that contains the given location.
     */
    public Optional<Mine> findByLocation(Location location) {
        return repository.findByLocation(location);
    }

    /**
     * Scans cuboid regions for {@code mask.marker} blocks, writes {@code fill-mode: mask} and positions to disk, then reloads the mine.
     * Must run on the main thread (world access).
     */
    public int scanAndSaveMask(String mineName) throws IOException, ConfigLoader.ConfigLoadException {
        int count = maskScanService.scanAndSave(mineName);
        plugin.loLogger().info("Mask scan for '" + mineName + "': " + count + " marker block(s)");
        return count;
    }

    /**
     * Reloads one mine from disk (stop, replace instance, start tasks).
     * Updates WorldGuard region if mine regions changed.
     */
    public void reloadMine(String name) throws IOException, ConfigLoader.ConfigLoadException {
        // Get old config before reload
        Mine oldMine = repository.find(name).orElse(null);

        repository.reload(name);

        // Update WorldGuard region if mine exists
        if (oldMine != null) {
            Mine newMine = repository.find(name).orElse(null);
            if (newMine != null) {
                worldGuardService.updateRegion(name, newMine.getConfig());
            }
        }
    }

    /**
     * Returns the WorldGuard region service for direct access.
     */
    public WorldGuardRegionService getWorldGuardService() {
        return worldGuardService;
    }
}
