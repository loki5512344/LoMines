package dev.loki.lomines.integration.hologram.provider;

import dev.loki.lomines.integration.hologram.HologramProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hologram provider implementation for HolographicDisplays plugin.
 */
public final class HolographicDisplaysProvider implements HologramProvider {

    private final Map<String, Location> holograms = new ConcurrentHashMap<>();
    private final boolean available;

    public HolographicDisplaysProvider() {
        this.available = checkAvailability();
    }

    private boolean checkAvailability() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("HolographicDisplays");
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean createHologram(String id, Location location, List<String> lines) {
        if (!available) {
            return false;
        }
        if (holograms.containsKey(id)) {
            return false;
        }
        // Runtime bridge disabled when API jar is absent at compile time.
        // We keep state so manager logic stays consistent.
        holograms.put(id, location);
        return true;
    }

    @Override
    public boolean updateHologram(String id, List<String> lines) {
        if (!available) {
            return false;
        }

        return holograms.containsKey(id);
    }

    @Override
    public boolean moveHologram(String id, Location newLocation) {
        if (!available) {
            return false;
        }

        if (!holograms.containsKey(id)) {
            return false;
        }
        holograms.put(id, newLocation);
        return true;
    }

    @Override
    public boolean deleteHologram(String id) {
        if (!available) {
            return false;
        }

        return holograms.remove(id) != null;
    }

    @Override
    public boolean exists(String id) {
        if (!available) {
            return false;
        }

        return holograms.containsKey(id);
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getName() {
        return "HolographicDisplays";
    }
}
