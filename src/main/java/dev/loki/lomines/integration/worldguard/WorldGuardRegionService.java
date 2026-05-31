package dev.loki.lomines.integration.worldguard;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.util.location.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing WorldGuard regions for mines.
 * Creates, updates, and deletes WorldGuard regions when mines are managed.
 */
public final class WorldGuardRegionService {

    private final LoMinesPlugin plugin;
    private final WorldGuardFlagParser flagParser;
    private boolean worldGuardEnabled;

    public WorldGuardRegionService(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.flagParser = new WorldGuardFlagParser(plugin);
        checkWorldGuard();
    }

    private void checkWorldGuard() {
        this.worldGuardEnabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null
                && Bukkit.getPluginManager().isPluginEnabled("WorldGuard");

        if (worldGuardEnabled) {
            plugin.loLogger().info("WorldGuard integration enabled - auto-region creation active");
        }
    }

    /**
     * Creates a WorldGuard region for a mine if enabled in config.
     */
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
                plugin.loLogger().info("WorldGuard region already exists: " + regionName);
                return regionName;
            }

            ProtectedCuboidRegion region = createRegionFromCuboids(regionName, config);
            if (region == null) {
                plugin.loLogger().warn("Failed to create region for mine " + mineName + ": no valid regions");
                return null;
            }

            applyOwnersAndMembers(region, wgConfig);
            applyFlags(region, wgConfig.flags());

            regionManager.addRegion(region);
            plugin.loLogger().info("Created WorldGuard region '" + regionName + "' for mine " + mineName);
            return regionName;

        } catch (Exception e) {
            plugin.loLogger().error("Failed to create WorldGuard region for mine " + mineName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates an existing WorldGuard region when mine regions change.
     */
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
            plugin.loLogger().error("Failed to update WorldGuard region: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a WorldGuard region for a mine.
     */
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
                plugin.loLogger().info("Deleted WorldGuard region: " + regionName);
                return true;
            }

            return false;
        } catch (Exception e) {
            plugin.loLogger().error("Failed to delete WorldGuard region: " + e.getMessage());
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

    private ProtectedCuboidRegion createRegionFromCuboids(String regionName, MineConfig config) {
        List<Cuboid> regions = config.region().regions();
        if (regions.isEmpty()) {
            return null;
        }

        Cuboid first = regions.get(0);
        BlockVector3 min = BlockVector3.at(first.getMinX(), first.getMinY(), first.getMinZ());
        BlockVector3 max = BlockVector3.at(first.getMaxX(), first.getMaxY(), first.getMaxZ());

        ProtectedCuboidRegion mainRegion = new ProtectedCuboidRegion(regionName, min, max);

        if (regions.size() > 1) {
            plugin.loLogger().warn("Mine " + regionName + " has multiple regions - " +
                    "only the first region is fully protected by WorldGuard");
        }

        return mainRegion;
    }

    private void applyOwnersAndMembers(ProtectedCuboidRegion region, WorldGuardConfig wgConfig) {
        // Set owners
        for (String owner : wgConfig.owners()) {
            if (owner.startsWith("uuid:")) {
                try {
                    UUID uuid = UUID.fromString(owner.substring(5));
                    region.getOwners().addPlayer(uuid);
                } catch (IllegalArgumentException e) {
                    plugin.loLogger().warn("Invalid owner UUID: " + owner);
                }
            } else {
                region.getOwners().addPlayer(owner);
            }
        }

        // Set members
        for (String member : wgConfig.members()) {
            if (member.startsWith("uuid:")) {
                try {
                    UUID uuid = UUID.fromString(member.substring(5));
                    region.getMembers().addPlayer(uuid);
                } catch (IllegalArgumentException e) {
                    plugin.loLogger().warn("Invalid member UUID: " + member);
                }
            } else {
                region.getMembers().addPlayer(member);
            }
        }
    }

    private void applyFlags(ProtectedCuboidRegion region, List<String> flags) {
        for (String flagEntry : flags) {
            flagParser.parseAndSetFlag(region, flagEntry);
        }
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
}
