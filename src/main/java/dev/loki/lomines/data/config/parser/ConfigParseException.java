package dev.loki.lomines.data.config.parser;

/**
 * Exception thrown when configuration parsing fails.
 * Provides descriptive error messages for invalid configuration data.
 */
public final class ConfigParseException extends Exception {

    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
