package dev.loki.lomines.data.config.ui;

import java.util.List;

/**
 * Configuration for holograms displayed above mines.
 *
 * @param enabled whether holograms are enabled
 * @param format  list of lines with placeholders {mine}, {percent}, {bar}, {time}
 * @param height  offset above the mine region (in blocks)
 */
public record HologramConfig(
        boolean enabled,
        List<String> format,
        double height
) {

    /**
     * Creates default disabled configuration.
     */
    public static HologramConfig disabled() {
        return new HologramConfig(
                false,
                List.of(),
                2.5
        );
    }

    /**
     * Creates default enabled configuration.
     */
    public static HologramConfig defaults() {
        return new HologramConfig(
                true,
                List.of(
                        "<gold><bold>{mine}",
                        "{bar} <yellow>{percent}%",
                        "<gray>Сброс через: <yellow>{time}"
                ),
                2.5
        );
    }
}
