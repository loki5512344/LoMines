package dev.loki.lomines.handler;

import dev.loki.lomines.core.Mine;
import dev.loki.lomines.util.format.TimeFormatter;
import dev.loki.lomines.util.location.Cuboid;
import dev.lolib.utils.ActionBar;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Handles action bar message display for mines.
 * Sends formatted messages to players within range of the mine.
 * Updated for new configuration system (v2) with MiniMessage support.
 */
public final class ActionBarHandler {

    private final Mine mine;

    public ActionBarHandler(Mine mine) {
        this.mine = mine;
    }

    /**
     * Sends action bar messages to all players within range of the mine.
     */
    public void sendToNearbyPlayers() {
        if (!mine.getConfig().ui().actionBarEnabled()) {
            return;
        }

        Location center = calculateCenter();
        if (center == null || center.getWorld() == null) {
            return;
        }

        double range = mine.getConfig().ui().actionBarRange();
        var message = mine.getConfig().ui().formatActionBar(
                mine.getName(),
                mine.getPercentFilled(),
                formatTime(),
                mine.getBlocks(),
                mine.getTotalVolume()
        );

        center.getWorld().getNearbyEntities(center, range, range, range).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .forEach(player -> ActionBar.send(player, message));
    }

    /**
     * Calculates the center point of the mine.
     * Uses the center of the first region.
     *
     * @return The center location, or null if no regions exist
     */
    private Location calculateCenter() {
        if (mine.getRegions().isEmpty()) {
            return null;
        }

        Cuboid firstRegion = mine.getRegions().get(0);
        int centerX = (firstRegion.getMinX() + firstRegion.getMaxX()) / 2;
        int centerY = (firstRegion.getMinY() + firstRegion.getMaxY()) / 2;
        int centerZ = (firstRegion.getMinZ() + firstRegion.getMaxZ()) / 2;

        return new Location(firstRegion.getWorld(), centerX, centerY, centerZ);
    }

    /**
     * Formats the time remaining until reset.
     *
     * @return Formatted time string
     */
    private String formatTime() {
        int remainingTicks = (int) (mine.getConfig().reset().intervalTicks() - mine.getTicks());
        if (remainingTicks < 0) remainingTicks = 0;
        return mine.getConfig().ui().formatTimer(remainingTicks / 20);
    }
}
