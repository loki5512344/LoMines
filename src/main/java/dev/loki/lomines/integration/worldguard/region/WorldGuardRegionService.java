package dev.loki.lomines.integration.worldguard.region;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.integration.worldguard.config.WorldGuardConfig;
import dev.loki.lomines.integration.worldguard.flag.WorldGuardFlagParser;
import org.bukkit.Bukkit;
import org.bukkit.World;

public final class WorldGuardRegionService {

    private final LoMinesPlugin plugin;
    private final WorldGuardFlagParser flagParser;
    private final WorldGuardMemberHandler memberHandler;
    private final RegionTemplateRenderer templateRenderer;
    private boolean worldGuardEnabled;

    public WorldGuardRegionService(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.flagParser = new WorldGuardFlagParser(plugin);
        this.memberHandler = new WorldGuardMemberHandler(plugin);
        this.templateRenderer = new RegionTemplateRenderer(flagParser);
        checkWorldGuard();
    }

    private void checkWorldGuard() {
        this.worldGuardEnabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null
                && Bukkit.getPluginManager().isPluginEnabled("WorldGuard");

        if (worldGuardEnabled) {
            plugin.getLogger().info("WorldGuard integration enabled");
        }
    }

    public String createRegion(String mineName, MineConfig config) {
        if (!worldGuardEnabled) {
            return null;
        }
        WorldGuardConfig wgConfig = config.worldGuard();
        if (wgConfig == null || !wgConfig.enabled()) {
            return null;
        }
        String regionName = wgConfig.generateRegionName(mineName);
        try {
            RegionManager regionManager = getRegionManager(config.worldName());
            if (regionManager == null) {
                return null;
            }
            if (regionManager.hasRegion(regionName)) {
                plugin.getLogger().info("WorldGuard region already exists: " + regionName);
                return regionName;
            }
            ProtectedCuboidRegion region = templateRenderer.createRegionFromCuboids(regionName, config);
            if (region == null) {
                plugin.getLogger().warning("Failed to create region for mine " + mineName + ": no valid regions");
                return null;
            }
            memberHandler.applyOwnersAndMembers(region, wgConfig);
            templateRenderer.applyFlags(region, wgConfig.flags());
            regionManager.addRegion(region);
            plugin.getLogger().info("Created WorldGuard region '" + regionName + "' for mine " + mineName);
            return regionName;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create WorldGuard region for mine " + mineName + ": " + e.getMessage());
            return null;
        }
    }

    public boolean updateRegion(String mineName, MineConfig config) {
        if (!worldGuardEnabled) {
            return false;
        }
        WorldGuardConfig wgConfig = config.worldGuard();
        if (wgConfig == null || !wgConfig.enabled()) {
            return false;
        }
        String regionName = wgConfig.generateRegionName(mineName);
        try {
            RegionManager regionManager = getRegionManager(config.worldName());
            if (regionManager == null) {
                return false;
            }
            if (regionManager.hasRegion(regionName)) {
                regionManager.removeRegion(regionName);
            }
            return createRegion(mineName, config) != null;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update WorldGuard region: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRegion(String mineName, MineConfig config) {
        if (!worldGuardEnabled) {
            return false;
        }
        WorldGuardConfig wgConfig = config.worldGuard();
        if (wgConfig == null) {
            return false;
        }
        String regionName = wgConfig.generateRegionName(mineName);
        try {
            RegionManager regionManager = getRegionManager(config.worldName());
            if (regionManager == null) {
                return false;
            }
            if (regionManager.hasRegion(regionName)) {
                regionManager.removeRegion(regionName);
                plugin.getLogger().info("Deleted WorldGuard region: " + regionName);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to delete WorldGuard region: " + e.getMessage());
            return false;
        }
    }

    private RegionManager getRegionManager(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
}
