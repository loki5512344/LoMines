package dev.loki.lomines.data.config.ui;

import dev.loki.lomines.util.format.ColorUtils;
import net.kyori.adventure.text.Component;

/**
 * Configuration for UI elements (action bar, messages).
 */
public record UIConfig(
        boolean actionBarEnabled,
        String actionBarFormat,
        double actionBarRange,
        String timerFormat,
        HologramConfig hologram
) {

    public static final String DEFAULT_ACTIONBAR_FORMAT = "<green>{mine}</green> <gray>{percent}%</gray> <dark_gray>({time})";
    public static final String DEFAULT_TIMER_FORMAT = "mm:ss";
    public UIConfig {
        hologram = hologram != null ? hologram : HologramConfig.disabled();
        actionBarFormat = actionBarFormat != null && !actionBarFormat.isBlank()
                ? actionBarFormat
                : DEFAULT_ACTIONBAR_FORMAT;
        timerFormat = timerFormat != null && !timerFormat.isBlank()
                ? timerFormat
                : DEFAULT_TIMER_FORMAT;
        actionBarRange = Math.max(1, actionBarRange);
    }

    /**
     * Default configuration.
     */
    public static UIConfig defaults() {
        return new UIConfig(true, DEFAULT_ACTIONBAR_FORMAT, 50.0, DEFAULT_TIMER_FORMAT, HologramConfig.defaults());
    }

    /**
     * Disabled configuration (no UI).
     */
    public static UIConfig disabled() {
        return new UIConfig(false, "", 0, DEFAULT_TIMER_FORMAT, HologramConfig.disabled());
    }

    /**
     * Parses the action bar format with placeholders.
     * Placeholders: {mine}, {percent}, {time}, {blocks}, {total}
     * Supports colors: &#RRGGBB, &a..&f, <color>
     */
    public Component formatActionBar(String mineName, double percent, String timeStr, int blocks, int total) {
        String parsed = actionBarFormat
                .replace("{mine}", mineName)
                .replace("{percent}", String.format("%.1f", percent))
                .replace("{time}", timeStr)
                .replace("{blocks}", String.valueOf(blocks))
                .replace("{total}", String.valueOf(total));

        return ColorUtils.format(parsed);
    }

    /**
     * Formats a duration according to timerFormat.
     * Supports: mm:ss, m:ss, HH:mm:ss, H:mm:ss
     */
    public String formatTimer(long seconds) {
        long hours = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return switch (timerFormat.toLowerCase()) {
            case "hh:mm:ss", "h:mm:ss" -> String.format("%d:%02d:%02d", hours, mins, secs);
            case "mm:ss", "m:ss" -> String.format("%02d:%02d", mins, secs);
            default -> String.format("%02d:%02d", mins, secs);
        };
    }

    /**
     * Range squared for efficient distance checks.
     */
    public double actionBarRangeSquared() {
        return actionBarRange * actionBarRange;
    }
}
