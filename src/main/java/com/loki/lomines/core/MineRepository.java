package com.loki.lomines.core;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.data.config.ConfigParseException;
import com.loki.lomines.data.config.MineConfig;
import org.bukkit.Location;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for managing mine instances.
 */
final class MineRepository {

    private final Map<String, Mine> mines = new ConcurrentHashMap<>();
    private final LoMinesPlugin plugin;
    private final MineFileManager fileManager;

    MineRepository(LoMinesPlugin plugin, MineFileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
    }

    void add(String name, Mine mine) {
        mines.put(name.toLowerCase(), mine);
    }

    Mine remove(String name) {
        return mines.remove(name.toLowerCase());
    }

    Mine get(String name) {
        Mine mine = mines.get(name.toLowerCase());
        if (mine == null) {
            throw new IllegalArgumentException("Mine not found: " + name);
        }
        return mine;
    }

    Optional<Mine> find(String name) {
        return Optional.ofNullable(mines.get(name.toLowerCase()));
    }

    Collection<Mine> getAll() {
        return Collections.unmodifiableCollection(mines.values());
    }

    Optional<Mine> findByLocation(Location location) {
        return mines.values().stream()
            .filter(mine -> mine.contains(location))
            .findFirst();
    }

    boolean exists(String name) {
        return mines.containsKey(name.toLowerCase());
    }

    void clear() {
        for (Mine mine : new ArrayList<>(mines.values())) {
            mine.stop();
        }
        mines.clear();
    }

    Mine createAndStart(String name, MineConfig config) {
        Mine mine = new Mine(name, config, plugin);
        add(name, mine);
        mine.start();
        return mine;
    }

    void stopAndRemove(String name) {
        Mine mine = remove(name);
        if (mine != null) {
            mine.stop();
        }
    }

    void reload(String name) throws IOException, ConfigParseException {
        stopAndRemove(name);
        MineConfig config = fileManager.loadConfig(name);
        Mine mine = createAndStart(name, config);
        plugin.loLogger().info("Reloaded mine: " + name);
    }
}
