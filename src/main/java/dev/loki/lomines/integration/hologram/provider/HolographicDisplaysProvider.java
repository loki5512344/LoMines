package dev.loki.lomines.integration.hologram.provider;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import dev.loki.lomines.LoMinesPlugin;
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

    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
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
        if (!available) return false;
        if (holograms.containsKey(id)) return false;

        try {
            Plugin hdPlugin = Bukkit.getPluginManager().getPlugin("HolographicDisplays");
            if (hdPlugin == null) return false;

            Hologram hologram = HologramsAPI.createHologram(hdPlugin, location);
            for (String line : lines) {
                hologram.appendTextLine(line);
            }
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
        if (hologram == null || hologram.isDeleted()) return false;

        try {
            hologram.clearLines();
            for (String line : lines) {
                hologram.appendTextLine(line);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean moveHologram(String id, Location newLocation) {
        if (!available) return false;

        Hologram hologram = holograms.get(id);
        if (hologram == null || hologram.isDeleted()) return false;

        try {
            hologram.teleport(newLocation);
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
            hologram.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(String id) {
        if (!available) return false;

        Hologram hologram = holograms.get(id);
        return hologram != null && !hologram.isDeleted();
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
