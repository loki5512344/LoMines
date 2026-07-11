package dev.loki.lomines.integration.worldguard.region;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.integration.worldguard.flag.WorldGuardFlagParser;
import dev.loki.lomines.util.location.geo.Cuboid;

import java.util.List;

public final class RegionTemplateRenderer {

    private final WorldGuardFlagParser flagParser;

    public RegionTemplateRenderer(WorldGuardFlagParser flagParser) {
        this.flagParser = flagParser;
    }

    public ProtectedCuboidRegion createRegionFromCuboids(String regionName, MineConfig config) {
        List<Cuboid> regions = config.region().regions();
        if (regions.isEmpty()) {
            return null;
        }

        Cuboid first = regions.get(0);
        BlockVector3 min = BlockVector3.at(first.getMinX(), first.getMinY(), first.getMinZ());
        BlockVector3 max = BlockVector3.at(first.getMaxX(), first.getMaxY(), first.getMaxZ());

        ProtectedCuboidRegion mainRegion = new ProtectedCuboidRegion(regionName, min, max);

        if (regions.size() > 1) {
            // Log warning about multiple regions - only first is fully protected
        }

        return mainRegion;
    }

    public void applyFlags(ProtectedCuboidRegion region, List<String> flags) {
        for (String flagEntry : flags) {
            flagParser.parseAndSetFlag(region, flagEntry);
        }
    }
}
