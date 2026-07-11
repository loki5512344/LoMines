package dev.loki.lomines.util;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class LocationParserErrorTest {

    @Test
    void testParseNullString() {
        ConfigParseException exception = assertThrows(
                ConfigParseException.class,
                () -> LocationParser.parse(null)
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testParseEmptyString() {
        ConfigParseException exception = assertThrows(
                ConfigParseException.class,
                () -> LocationParser.parse("")
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testParseInvalidFormat() {
        ConfigParseException exception = assertThrows(
                ConfigParseException.class,
                () -> LocationParser.parse("world;100;64")
        );
        assertTrue(exception.getMessage().contains("Invalid location format"));
        assertTrue(exception.getMessage().contains("Expected format"));
    }

    @Test
    void testParseInvalidNumbers() {
        ConfigParseException exception = assertThrows(
                ConfigParseException.class,
                () -> LocationParser.parse("world;abc;64;0;0;0")
        );
        assertTrue(exception.getMessage().contains("Invalid location format"));
    }

    @Test
    void testParseNonExistentWorld() {
        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("nonexistent")).thenReturn(null);

            ConfigParseException exception = assertThrows(
                    ConfigParseException.class,
                    () -> LocationParser.parse("nonexistent;0;64;0;0;0")
            );
            assertTrue(exception.getMessage().contains("does not exist"));
            assertTrue(exception.getMessage().contains("nonexistent"));
        }
    }
}
