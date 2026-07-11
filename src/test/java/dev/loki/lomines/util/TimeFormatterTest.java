package dev.loki.lomines.util;

import dev.loki.lomines.util.format.TimeFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeFormatterTest {

    @Test
    void testFormatMinutesSeconds() {
        // 1 minute = 60 seconds = 1200 ticks
        assertEquals("01:00", TimeFormatter.format(1200, "mm:ss"));

        // 2 minutes 30 seconds = 150 seconds = 3000 ticks
        assertEquals("02:30", TimeFormatter.format(3000, "mm:ss"));

        // 0 seconds
        assertEquals("00:00", TimeFormatter.format(0, "mm:ss"));

        // 59 seconds = 1180 ticks
        assertEquals("00:59", TimeFormatter.format(1180, "mm:ss"));

        // 10 minutes 5 seconds = 605 seconds = 12100 ticks
        assertEquals("10:05", TimeFormatter.format(12100, "mm:ss"));
    }

    @Test
    void testFormatHoursMinutesSeconds() {
        // 1 hour = 3600 seconds = 72000 ticks
        assertEquals("01:00:00", TimeFormatter.format(72000, "hh:mm:ss"));

        // 1 hour 30 minutes 45 seconds = 5445 seconds = 108900 ticks
        assertEquals("01:30:45", TimeFormatter.format(108900, "hh:mm:ss"));

        // 0 seconds
        assertEquals("00:00:00", TimeFormatter.format(0, "hh:mm:ss"));

        // 2 hours 5 minutes 3 seconds = 7503 seconds = 150060 ticks
        assertEquals("02:05:03", TimeFormatter.format(150060, "hh:mm:ss"));

        // 23 hours 59 minutes 59 seconds = 86399 seconds = 1727980 ticks
        assertEquals("23:59:59", TimeFormatter.format(1727980, "hh:mm:ss"));
    }

    @Test
    void testFormatSeconds() {
        // 100 ticks = 5 seconds
        assertEquals("5", TimeFormatter.format(100, "s"));

        // 1200 ticks = 60 seconds
        assertEquals("60", TimeFormatter.format(1200, "s"));

        // 0 ticks = 0 seconds
        assertEquals("0", TimeFormatter.format(0, "s"));

        // 6000 ticks = 300 seconds (5 minutes)
        assertEquals("300", TimeFormatter.format(6000, "s"));
    }

    @Test
    void testFormatMinutes() {
        // 1200 ticks = 60 seconds = 1 minute
        assertEquals("1", TimeFormatter.format(1200, "m"));

        // 6000 ticks = 300 seconds = 5 minutes
        assertEquals("5", TimeFormatter.format(6000, "m"));

        // 0 ticks = 0 minutes
        assertEquals("0", TimeFormatter.format(0, "m"));

        // 72000 ticks = 3600 seconds = 60 minutes
        assertEquals("60", TimeFormatter.format(72000, "m"));

        // 1100 ticks = 55 seconds = 0 minutes (truncated)
        assertEquals("0", TimeFormatter.format(1100, "m"));
    }

    @Test
    void testFormatHours() {
        // 72000 ticks = 3600 seconds = 1 hour
        assertEquals("1", TimeFormatter.format(72000, "h"));

        // 144000 ticks = 7200 seconds = 2 hours
        assertEquals("2", TimeFormatter.format(144000, "h"));

        // 0 ticks = 0 hours
        assertEquals("0", TimeFormatter.format(0, "h"));

        // 360000 ticks = 18000 seconds = 5 hours
        assertEquals("5", TimeFormatter.format(360000, "h"));

        // 71999 ticks = 3599 seconds = 0 hours (truncated)
        assertEquals("0", TimeFormatter.format(71999, "h"));
    }

    @Test
    void testFormatCaseInsensitive() {
        assertEquals("01:00", TimeFormatter.format(1200, "MM:SS"));
        assertEquals("01:00:00", TimeFormatter.format(72000, "HH:MM:SS"));
        assertEquals("5", TimeFormatter.format(100, "S"));
        assertEquals("1", TimeFormatter.format(1200, "M"));
        assertEquals("1", TimeFormatter.format(72000, "H"));
    }

    @Test
    void testFormatResetTimerExample() {
        // Typical reset timer: 6000 ticks (5 minutes)
        assertEquals("05:00", TimeFormatter.format(6000, "mm:ss"));

        // After 1 minute: 4800 ticks remaining
        assertEquals("04:00", TimeFormatter.format(4800, "mm:ss"));

        // After 4 minutes 30 seconds: 600 ticks remaining
        assertEquals("00:30", TimeFormatter.format(600, "mm:ss"));

        // Just before reset: 20 ticks remaining (1 second)
        assertEquals("00:01", TimeFormatter.format(20, "mm:ss"));
    }
}
