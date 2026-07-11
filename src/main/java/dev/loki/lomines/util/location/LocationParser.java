package dev.loki.lomines.util.location;

import dev.loki.lomines.data.config.parser.ConfigParseException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and formatting Location objects to/from string format.
 * Format: "world;x;y;z;yaw;pitch"
 */
public final class LocationParser {

    private static final Pattern LOCATION_PATTERN =
            Pattern.compile("([^;]+);(-?\\d+\\.?\\d*);(-?\\d+\\.?\\d*);(-?\\d+\\.?\\d*);(-?\\d+\\.?\\d*);(-?\\d+\\.?\\d*)");

    private LocationParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parses a location string in the format "world;x;y;z;yaw;pitch".
     *
     * @param locationString the location string to parse
     * @return the parsed Location object
     * @throws ConfigParseException if the string format is invalid or the world doesn't exist
     */
    public static Location parse(String locationString) throws ConfigParseException {
        if (locationString == null || locationString.trim().isEmpty()) {
            throw new ConfigParseException("Location string cannot be null or empty");
        }

        Matcher matcher = LOCATION_PATTERN.matcher(locationString.trim());
        if (!matcher.matches()) {
            throw new ConfigParseException(
                    "Invalid location format: '" + locationString + "'. " +
                            "Expected format: 'world;x;y;z;yaw;pitch'"
            );
        }

        try {
            String worldName = matcher.group(1);
            double x = Double.parseDouble(matcher.group(2));
            double y = Double.parseDouble(matcher.group(3));
            double z = Double.parseDouble(matcher.group(4));
            float yaw = Float.parseFloat(matcher.group(5));
            float pitch = Float.parseFloat(matcher.group(6));

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new ConfigParseException(
                        "World '" + worldName + "' does not exist or is not loaded"
                );
            }

            return new Location(world, x, y, z, yaw, pitch);

        } catch (NumberFormatException e) {
            throw new ConfigParseException(
                    "Invalid number format in location string: '" + locationString + "'",
                    e
            );
        }
    }

    /**
     * Parses a simple location string in the format "world;x;y;z" without yaw/pitch.
     * Returns null if parsing fails.
     *
     * @param locationString the location string to parse
     * @return the parsed Location object, or null if parsing fails
     */
    public static Location parseSimple(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        String[] parts = locationString.split(";");
        if (parts.length < 4) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Formats a Location object to a string in the format "world;x;y;z;yaw;pitch".
     *
     * @param location the location to format
     * @return the formatted location string
     * @throws IllegalArgumentException if location or its world is null
     */
    public static String format(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        World world = location.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location world cannot be null");
        }

        return String.format(Locale.US, "%s;%.2f;%.2f;%.2f;%.2f;%.2f",
                world.getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }
}
