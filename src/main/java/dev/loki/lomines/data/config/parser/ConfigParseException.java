package dev.loki.lomines.data.config.parser;

/**
 * Exception for configuration parsing errors.
 */
public class ConfigParseException extends Exception {

    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
