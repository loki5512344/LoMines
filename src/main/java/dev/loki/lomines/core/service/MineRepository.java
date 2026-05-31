package dev.loki.lomines.core.service;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.parser.ConfigParseException;
import org.bukkit.Location;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for managing mine instances.
 */
public final class MineRepository {

    private final Map<String, Mine> mines = new ConcurrentHashMap<>();
    private final LoMinesPlugin plugin;
    private final MineFileManager fileManager;

    public MineRepository(LoMinesPlugin plugin, MineFileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
    }

    public void add(String name, Mine mine) {
        mines.put(name.toLowerCase(), mine);
    }

    public Mine remove(String name) {
        return mines.remove(name.toLowerCase());
    }

    public Mine get(String name) {
        Mine mine = mines.get(name.toLowerCase());
        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }
        return mine;
    }

    public Optional<Mine> find(String name) {
        return Optional.ofNullable(mines.get(name.toLowerCase()));
    }

    public Collection<Mine> getAll() {
        return Collections.unmodifiableCollection(mines.values());
    }

    public Optional<Mine> findByLocation(Location location) {
        return mines.values().stream()
                .filter(mine -> mine.contains(location))
                .findFirst();
    }

    public boolean exists(String name) {
        return mines.containsKey(name.toLowerCase());
    }

    public void clear() {
        for (Mine mine : new ArrayList<>(mines.values())) {
            mine.stop();
        }
        mines.clear();
    }

    public Mine createAndStart(String name, MineConfig config) {
        Mine mine = new Mine(name, config, plugin);
        add(name, mine);
        mine.start();
        return mine;
    }

    public void stopAndRemove(String name) {
        Mine mine = remove(name);
        if (mine != null) {
            mine.stop();
        }
    }

    public void reload(String name) throws IOException, ConfigParseException {
        stopAndRemove(name);
        MineConfig config = fileManager.loadConfig(name);
        Mine mine = createAndStart(name, config);
        plugin.loLogger().info("Reloaded mine: " + name);
    }
}
