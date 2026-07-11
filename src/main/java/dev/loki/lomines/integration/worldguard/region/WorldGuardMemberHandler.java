package dev.loki.lomines.integration.worldguard.region;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.integration.worldguard.config.WorldGuardConfig;

import java.util.UUID;

/**
 * Handles adding owners and members to WorldGuard regions.
 */
final class WorldGuardMemberHandler {

    private final LoMinesPlugin plugin;

    WorldGuardMemberHandler(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    void applyOwnersAndMembers(ProtectedCuboidRegion region, WorldGuardConfig wgConfig) {
        applyOwners(region, wgConfig);
        applyMembers(region, wgConfig);
    }

    private void applyOwners(ProtectedCuboidRegion region, WorldGuardConfig wgConfig) {
        for (String owner : wgConfig.owners()) {
            if (owner.startsWith("uuid:")) {
                try {
                    UUID uuid = UUID.fromString(owner.substring(5));
                    region.getOwners().addPlayer(uuid);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid owner UUID: " + owner);
                }
            } else {
                region.getOwners().addPlayer(owner);
            }
        }
    }

    private void applyMembers(ProtectedCuboidRegion region, WorldGuardConfig wgConfig) {
        for (String member : wgConfig.members()) {
            if (member.startsWith("uuid:")) {
                try {
                    UUID uuid = UUID.fromString(member.substring(5));
                    region.getMembers().addPlayer(uuid);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid member UUID: " + member);
                }
            } else {
                region.getMembers().addPlayer(member);
            }
        }
    }
}
