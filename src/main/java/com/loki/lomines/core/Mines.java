package com.loki.lomines.core;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.data.ConfigLoader;
import com.loki.lomines.data.ConfigParseException;
import com.loki.lomines.data.MineConfig;
import com.loki.lomines.util.LocationParser;
import com.loki.lomines.util.MaskScanner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Registry for all active mines.
 * Manages the lifecycle of mines including loading, creating, and deleting.
 */
public final class Mines {
    
    private final Map<String, Mine> mines = new ConcurrentHashMap<>();
    private final LoMinesPlugin plugin;
    private final Path minesFolder;
    
    public Mines(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.minesFolder = plugin.getDataFolder().toPath().resolve("mines");
    }
    
    /**
     * Loads all mines from the mines/ folder.
     * Clears and replaces any previously loaded mines (used for full reload).
     */
    public void loadAll() throws IOException {
        for (Mine m : new ArrayList<>(mines.values())) {
            m.stop();
        }
        mines.clear();
        
        if (!Files.exists(minesFolder)) {
            return;
        }
        
        ConfigLoader loader = new ConfigLoader();
        
        try (Stream<Path> paths = Files.list(minesFolder)) {
            paths.filter(path -> path.toString().endsWith(".yml"))
                .forEach(configFile -> {
                    try {
                        String fileName = configFile.getFileName().toString();
                        String mineName = fileName.substring(0, fileName.length() - 4);
                        
                        MineConfig config = loader.load(configFile);
                        Mine mine = new Mine(mineName, config, plugin);
                        
                        mines.put(mineName.toLowerCase(), mine);
                        mine.start();
                        
                        plugin.loLogger().info("Loaded mine: " + mineName);
                    } catch (IOException | ConfigParseException e) {
                        plugin.loLogger().error("Failed to load mine from " + configFile.getFileName() + ": " + e.getMessage());
                    }
                });
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
        String key = name.toLowerCase();
        if (mines.containsKey(key)) {
            throw new IllegalArgumentException("Mine already exists: " + name);
        }
        if (corner1 != null && corner2 != null && !corner1.getWorld().equals(corner2.getWorld())) {
            throw new IllegalArgumentException("Selection corners must be in the same world");
        }
        
        // Ensure mines folder exists
        if (!Files.exists(minesFolder)) {
            Files.createDirectories(minesFolder);
        }
        
        // Create empty YAML configuration file
        Path configFile = minesFolder.resolve(name + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        
        // Set default empty configuration
        if (corner1 != null && corner2 != null) {
            yaml.set("selection.1", LocationParser.format(corner1));
            yaml.set("selection.2", LocationParser.format(corner2));
        } else {
            yaml.set("selection.1", "world;0;64;0;0;0");
            yaml.set("selection.2", "world;10;74;10;0;0");
        }
        yaml.set("contents.stone", 100.0);
        yaml.set("reset.ticks", 6000);
        yaml.set("reset.percent", 10.0);
        yaml.set("reset-on-percent", false);
        yaml.set("actionbar.enabled", true);
        yaml.set("actionbar.message", "&aMine: %mine% | Blocks: %blocks%/%total% (%percent%%)");
        yaml.set("actionbar.range", 50.0);
        yaml.set("timer-format", "mm:ss");
        yaml.set("teleport-on-reset", false);
        yaml.set("broadcast-reset", "");
        yaml.set("reset-commands", new ArrayList<String>());
        yaml.set("random-rewards", new ArrayList<Map<String, Object>>());
        yaml.set("fill-mode", "cuboid");
        yaml.set("mask.marker", "pink_concrete");
        yaml.set("mask.positions", new ArrayList<String>());
        
        yaml.save(configFile.toFile());
        
        // Load the mine configuration
        ConfigLoader loader = new ConfigLoader();
        MineConfig config;
        try {
            config = loader.load(configFile);
        } catch (ConfigParseException e) {
            throw new IOException("Failed to parse created mine configuration: " + e.getMessage(), e);
        }
        
        // Create and register the mine
        Mine mine = new Mine(name, config, plugin);
        mines.put(key, mine);
        mine.start();
        
        plugin.loLogger().info("Created mine: " + name);
    }
    
    /**
     * Deletes a mine by name.
     */
    public void delete(String name) throws IOException {
        String key = name.toLowerCase();
        Mine mine = mines.remove(key);
        
        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }
        
        // Stop the mine
        mine.stop();
        
        // Delete configuration file
        Path configFile = minesFolder.resolve(name + ".yml");
        if (Files.exists(configFile)) {
            Files.delete(configFile);
        }
    }
    
    /**
     * Gets a mine by name.
     * @throws IllegalArgumentException if mine not found
     */
    public Mine get(String name) {
        Mine mine = mines.get(name.toLowerCase());
        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }
        return mine;
    }
    
    /**
     * Finds a mine by name, returning Optional.
     */
    public Optional<Mine> find(String name) {
        return Optional.ofNullable(mines.get(name.toLowerCase()));
    }
    
    /**
     * Returns all mines.
     */
    public Collection<Mine> getAll() {
        return Collections.unmodifiableCollection(mines.values());
    }
    
    /**
     * Finds a mine that contains the given location.
     */
    public Optional<Mine> findByLocation(Location location) {
        return mines.values().stream()
            .filter(mine -> mine.contains(location))
            .findFirst();
    }
    
    /**
     * Scans cuboid regions for {@code mask.marker} blocks, writes {@code fill-mode: mask} and positions to disk, then reloads the mine.
     * Must run on the main thread (world access).
     */
    public int scanAndSaveMask(String mineName) throws IOException, ConfigParseException {
        Mine mine = get(mineName);
        Material marker;
        try {
            marker = Material.valueOf(mine.getConfig().getMaskMarkerMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            marker = Material.PINK_CONCRETE;
        }
        List<Location> found = MaskScanner.scan(mine.getRegions(), marker);
        Path configFile = minesFolder.resolve(mineName + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile.toFile());
        yaml.set("fill-mode", "mask");
        yaml.set("mask.marker", mine.getConfig().getMaskMarkerMaterial());
        List<String> lines = new ArrayList<>();
        for (Location loc : found) {
            lines.add(LocationParser.format(loc));
        }
        yaml.set("mask.positions", lines);
        yaml.save(configFile.toFile());
        plugin.loLogger().info("Mask scan for '" + mineName + "': " + found.size() + " marker block(s)");
        reloadMine(mineName);
        return found.size();
    }
    
    /**
     * Reloads one mine from disk (stop, replace instance, start tasks).
     */
    public void reloadMine(String name) throws IOException, ConfigParseException {
        String key = name.toLowerCase();
        Mine old = mines.remove(key);
        if (old != null) {
            old.stop();
        }
        Path configFile = minesFolder.resolve(name + ".yml");
        if (!Files.exists(configFile)) {
            throw new IOException("Mine config not found: " + configFile);
        }
        ConfigLoader loader = new ConfigLoader();
        MineConfig config = loader.load(configFile);
        Mine mine = new Mine(name, config, plugin);
        mines.put(key, mine);
        mine.start();
        plugin.loLogger().info("Reloaded mine: " + name);
    }
}
