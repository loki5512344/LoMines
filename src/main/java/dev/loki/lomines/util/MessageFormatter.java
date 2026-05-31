package dev.loki.lomines.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Utility class for formatting messages with consistent styling.
 */
public final class MessageFormatter {

    private static final String PREFIX = "[LoMines] ";

    private MessageFormatter() {
    }

    /**
     * Creates a success message (green).
     */
    public static Component success(String message) {
        return Component.text(PREFIX + message, NamedTextColor.GREEN);
    }

    /**
     * Creates an error message (red).
     */
    public static Component error(String message) {
        return Component.text(PREFIX + message, NamedTextColor.RED);
    }

    /**
     * Creates a warning message (yellow).
     */
    public static Component warning(String message) {
        return Component.text(PREFIX + message, NamedTextColor.YELLOW);
    }

    /**
     * Creates an info message (gray).
     */
    public static Component info(String message) {
        return Component.text(PREFIX + message, NamedTextColor.GRAY);
    }

    /**
     * Creates a highlighted message (aqua, bold).
     */
    public static Component highlight(String message) {
        return Component.text(message, NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true);
    }

    /**
     * Creates a plain message without prefix.
     */
    public static Component plain(String message) {
        return Component.text(message);
    }
}
