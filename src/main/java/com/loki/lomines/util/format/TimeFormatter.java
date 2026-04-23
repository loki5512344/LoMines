package com.loki.lomines.util.format;

/**
 * Utility class for formatting time from Minecraft ticks to human-readable formats.
 * Minecraft runs at 20 ticks per second.
 */
public final class TimeFormatter {
    
    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    
    private TimeFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Formats time from Minecraft ticks to a human-readable string.
     *
     * @param ticks the number of Minecraft ticks
     * @param format the desired format: "mm:ss", "hh:mm:ss", "s", "m", or "h"
     * @return the formatted time string
     * @throws IllegalArgumentException if the format is not recognized
     */
    public static String format(int ticks, String format) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format cannot be null or empty");
        }
        
        if (ticks < 0) {
            ticks = 0;
        }
        
        int totalSeconds = ticks / TICKS_PER_SECOND;
        
        return switch (format.toLowerCase()) {
            case "mm:ss" -> formatMinutesSeconds(totalSeconds);
            case "hh:mm:ss" -> formatHoursMinutesSeconds(totalSeconds);
            case "s" -> String.valueOf(totalSeconds);
            case "m" -> String.valueOf(totalSeconds / SECONDS_PER_MINUTE);
            case "h" -> String.valueOf(totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR));
            default -> throw new IllegalArgumentException(
                "Unknown time format: '" + format + "'. " +
                "Supported formats: mm:ss, hh:mm:ss, s, m, h"
            );
        };
    }
    
    /**
     * Formats time as "mm:ss" (minutes:seconds).
     */
    private static String formatMinutesSeconds(int totalSeconds) {
        int minutes = totalSeconds / SECONDS_PER_MINUTE;
        int seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Formats time as "hh:mm:ss" (hours:minutes:seconds).
     */
    private static String formatHoursMinutesSeconds(int totalSeconds) {
        int hours = totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR);
        int minutes = (totalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
        int seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
