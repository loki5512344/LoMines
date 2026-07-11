package dev.loki.lomines.integration.hologram;

import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.util.format.color.ColorUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public final class HologramRenderer {

    public Location calculateLocation(Mine mine, double height) {
        if (mine.getRegions().isEmpty()) {
            return null;
        }
        var region = mine.getRegions().get(0);
        double centerX = (region.getMinX() + region.getMaxX()) / 2.0 + 0.5;
        double centerZ = (region.getMinZ() + region.getMaxZ()) / 2.0 + 0.5;
        double centerY = region.getMaxY() + height;
        return new Location(region.getWorld(), centerX, centerY, centerZ);
    }

    public List<String> formatLines(List<String> format, Mine mine) {
        List<String> result = new ArrayList<>();
        for (String line : format) {
            String formatted = line
                    .replace("{mine}", mine.getName())
                    .replace("{percent}", String.format("%.1f", mine.getPercentFilled()))
                    .replace("{bar}", createProgressBar(mine.getPercentFilled()))
                    .replace("{time}", formatResetTime(mine));
            result.add(ColorUtils.toLegacy(formatted));
        }
        return result;
    }

    private String createProgressBar(double percent) {
        int filled = (int) Math.round(percent / 10.0);
        int empty = 10 - filled;
        return "&#00FF00" + "█".repeat(Math.max(0, filled))
                + "&#808080" + "░".repeat(Math.max(0, empty));
    }

    private String formatResetTime(Mine mine) {
        int ticks = mine.getTicks();
        int seconds = (int) ((6000 - ticks) / 20.0);
        if (seconds < 0) {
            seconds = 0;
        }
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
