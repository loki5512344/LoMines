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
        if (!mine.getConfig().isActionBarEnabled()) {
            return;
        }

        Location center = calculateCenter();
        if (center == null || center.getWorld() == null) {
            return;
        }

        double range = mine.getConfig().getActionBarRange();
        String message = formatMessage();

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
     * Formats the action bar message with placeholders.
     *
     * @return The formatted message
     */
    private String formatMessage() {
        String template = mine.getConfig().getActionBarMessage();

        String formatted = template
                .replace("%mine%", mine.getName())
                .replace("%percent%", String.format("%.1f", mine.getPercentFilled()))
                .replace("%blocks%", String.valueOf(mine.getBlocks()))
                .replace("%total%", String.valueOf(mine.getTotalVolume()))
                .replace("%time%", formatTime());

        return formatted;
    }

    /**
     * Formats the time remaining until reset.
     *
     * @return Formatted time string
     */
    private String formatTime() {
        int remainingTicks = mine.getConfig().getResetTicks() - mine.getTicks();
        String format = mine.getConfig().getTimerFormat();
        return TimeFormatter.format(remainingTicks, format);
    }
}
