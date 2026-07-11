package dev.loki.lomines.util;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationParserTest {

    @Test
    void testParseValidLocation() throws ConfigParseException {
        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            when(mockWorld.getName()).thenReturn("world");
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Location location = LocationParser.parse("world;100.5;64.0;-200.75;90.0;45.0");

            assertNotNull(location);
            assertEquals(mockWorld, location.getWorld());
            assertEquals(100.5, location.getX(), 0.001);
            assertEquals(64.0, location.getY(), 0.001);
            assertEquals(-200.75, location.getZ(), 0.001);
            assertEquals(90.0f, location.getYaw(), 0.001);
            assertEquals(45.0f, location.getPitch(), 0.001);
        }
    }

    @Test
    void testParseIntegerCoordinates() throws ConfigParseException {
        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            when(mockWorld.getName()).thenReturn("world_nether");
            bukkit.when(() -> Bukkit.getWorld("world_nether")).thenReturn(mockWorld);

            Location location = LocationParser.parse("world_nether;0;128;0;0;0");

            assertNotNull(location);
            assertEquals(0.0, location.getX(), 0.001);
            assertEquals(128.0, location.getY(), 0.001);
            assertEquals(0.0, location.getZ(), 0.001);
        }
    }

    @Test
    void testParseNegativeCoordinates() throws ConfigParseException {
        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Location location = LocationParser.parse("world;-100;-64;-200;-180;-90");

            assertNotNull(location);
            assertEquals(-100.0, location.getX(), 0.001);
            assertEquals(-64.0, location.getY(), 0.001);
            assertEquals(-200.0, location.getZ(), 0.001);
            assertEquals(-180.0f, location.getYaw(), 0.001);
            assertEquals(-90.0f, location.getPitch(), 0.001);
        }
    }

    @Test
    void testFormatValidLocation() {
        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        Location location = new Location(mockWorld, 100.5, 64.0, -200.75, 90.0f, 45.0f);
        String formatted = LocationParser.format(location);

        assertEquals("world;100.50;64.00;-200.75;90.00;45.00", formatted);
    }

    @Test
    void testFormatIntegerCoordinates() {
        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world_nether");

        Location location = new Location(mockWorld, 0, 128, 0, 0, 0);
        String formatted = LocationParser.format(location);

        assertEquals("world_nether;0.00;128.00;0.00;0.00;0.00", formatted);
    }

    @Test
    void testFormatNullLocation() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LocationParser.format(null)
        );
        assertTrue(exception.getMessage().contains("Location cannot be null"));
    }

    @Test
    void testFormatNullWorld() {
        Location location = new Location(null, 0, 0, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LocationParser.format(location)
        );
        assertTrue(exception.getMessage().contains("world cannot be null"));
    }

    @Test
    void testRoundTrip() throws ConfigParseException {
        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            when(mockWorld.getName()).thenReturn("world");
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Location original = new Location(mockWorld, 100.5, 64.0, -200.75, 90.0f, 45.0f);
            String formatted = LocationParser.format(original);
            Location parsed = LocationParser.parse(formatted);

            assertEquals(original.getX(), parsed.getX(), 0.01);
            assertEquals(original.getY(), parsed.getY(), 0.01);
            assertEquals(original.getZ(), parsed.getZ(), 0.01);
            assertEquals(original.getYaw(), parsed.getYaw(), 0.01);
            assertEquals(original.getPitch(), parsed.getPitch(), 0.01);
        }
    }
}
