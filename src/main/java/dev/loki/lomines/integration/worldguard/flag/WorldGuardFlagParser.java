package dev.loki.lomines.integration.worldguard.flag;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.loki.lomines.LoMinesPlugin;

/**
 * Parses and applies WorldGuard flags from string entries.
 */
public final class WorldGuardFlagParser {

    private final LoMinesPlugin plugin;

    public WorldGuardFlagParser(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Parses and sets a flag from a "key=value" string entry.
     *
     * @param region    the region to set the flag on
     * @param flagEntry the flag entry in format "flag=value"
     */
    public void parseAndSetFlag(ProtectedRegion region, String flagEntry) {
        String[] parts = flagEntry.split("=", 2);
        if (parts.length != 2) {
            return;
        }

        String flagName = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        try {
            switch (flagName) {
                case "passthrough" -> setStateFlag(region, Flags.PASSTHROUGH, value);
                case "build" -> setStateFlag(region, Flags.BUILD, value);
                case "pvp" -> setStateFlag(region, Flags.PVP, value);
                case "tnt" -> setStateFlag(region, Flags.TNT, value);
                case "creeper-explosion" -> setStateFlag(region, Flags.CREEPER_EXPLOSION, value);
                case "greeting" -> region.setFlag(Flags.GREET_MESSAGE, value);
                case "farewell" -> region.setFlag(Flags.FAREWELL_MESSAGE, value);
                default -> plugin.getLogger().warning("Unknown WorldGuard flag: " + flagName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set flag " + flagName + ": " + e.getMessage());
        }
    }

    private void setStateFlag(ProtectedRegion region, StateFlag flag, String value) {
        region.setFlag(flag, "allow".equalsIgnoreCase(value)
                ? StateFlag.State.ALLOW
                : StateFlag.State.DENY);
    }
}
