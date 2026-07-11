package dev.loki.lomines.util;

import dev.loki.lomines.util.format.TimeFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeFormatterEdgeTest {

    @Test
    void testFormatNegativeTicks() {
        assertEquals("00:00", TimeFormatter.format(-100, "mm:ss"));
        assertEquals("00:00:00", TimeFormatter.format(-1000, "hh:mm:ss"));
        assertEquals("0", TimeFormatter.format(-50, "s"));
    }

    @Test
    void testFormatNullFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TimeFormatter.format(1200, null)
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testFormatEmptyFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TimeFormatter.format(1200, "")
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testFormatWhitespaceFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TimeFormatter.format(1200, "   ")
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testFormatUnknownFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TimeFormatter.format(1200, "invalid")
        );
        assertTrue(exception.getMessage().contains("Unknown time format"));
        assertTrue(exception.getMessage().contains("invalid"));
        assertTrue(exception.getMessage().contains("Supported formats"));
    }

    @Test
    void testFormatLargeValues() {
        assertEquals("833:20", TimeFormatter.format(1000000, "mm:ss"));
        assertEquals("13:53:20", TimeFormatter.format(1000000, "hh:mm:ss"));
        assertEquals("50000", TimeFormatter.format(1000000, "s"));
        assertEquals("833", TimeFormatter.format(1000000, "m"));
        assertEquals("13", TimeFormatter.format(1000000, "h"));
    }
}
