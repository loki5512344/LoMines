package dev.loki.lomines.integration.hologram.provider;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hologram provider implementation for DecentHolograms plugin.
 */
public final class DecentHologramsProvider implements HologramProvider {

    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
    private final boolean available;

    public DecentHologramsProvider() {
        this.available = checkAvailability();
    }

    private boolean checkAvailability() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("DecentHolograms");
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean createHologram(String id, Location location, List<String> lines) {
        if (!available) return false;
        if (holograms.containsKey(id)) return false;

        try {
            Hologram hologram = DHAPI.createHologram(id, location, lines);
            holograms.put(id, hologram);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateHologram(String id, List<String> lines) {
        if (!available) return false;

        Hologram hologram = holograms.get(id);
        if (hologram == null) return false;

        try {
            DHAPI.setHologramLines(hologram, lines);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean moveHologram(String id, Location newLocation) {
        if (!available) return false;

        Hologram hologram = holograms.get(id);
        if (hologram == null) return false;

        try {
            hologram.setLocation(newLocation);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteHologram(String id) {
        if (!available) return false;

        Hologram hologram = holograms.remove(id);
        if (hologram == null) return false;

        try {
            hologram.destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(String id) {
        if (!available) return false;
        return DHAPI.getHologram(id) != null;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getName() {
        return "DecentHolograms";
    }
}
