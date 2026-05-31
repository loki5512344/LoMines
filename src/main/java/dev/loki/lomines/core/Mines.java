package dev.loki.lomines.core;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.service.MaskScanService;
import dev.loki.lomines.core.service.MineFileManager;
import dev.loki.lomines.core.service.MineRepository;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.parser.ConfigParseException;
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
 */
public final class Mines {

    private final LoMinesPlugin plugin;
    private final MineFileManager fileManager;
    private final MineRepository repository;
    private final MaskScanService maskScanService;

    public Mines(LoMinesPlugin plugin) {
        this.plugin = plugin;
        Path minesFolder = plugin.getDataFolder().toPath().resolve("mines");
        this.fileManager = new MineFileManager(minesFolder);
        this.repository = new MineRepository(plugin, fileManager);
        this.maskScanService = new MaskScanService(repository, fileManager);
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
        } catch (IOException | ConfigParseException e) {
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
        } catch (ConfigParseException e) {
            throw new IOException("Failed to parse created mine configuration: " + e.getMessage(), e);
        }

        repository.createAndStart(name, config);
        plugin.loLogger().info("Created mine: " + name);
    }

    /**
     * Deletes a mine by name.
     */
    public void delete(String name) throws IOException {
        Mine mine = repository.remove(name);

        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }

        mine.stop();
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
    public int scanAndSaveMask(String mineName) throws IOException, ConfigParseException {
        int count = maskScanService.scanAndSave(mineName);
        plugin.loLogger().info("Mask scan for '" + mineName + "': " + count + " marker block(s)");
        return count;
    }

    /**
     * Reloads one mine from disk (stop, replace instance, start tasks).
     */
    public void reloadMine(String name) throws IOException, ConfigParseException {
        repository.reload(name);
    }
}
