package dev.loki.lomines.util;

import org.bukkit.command.CommandSender;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Унифицированная обработка ошибок и отправка сообщений.
 */
public class ErrorHandler {
    private final Logger logger;
    private final String prefix;

    public ErrorHandler(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    /**
     * Отправить сообщение об ошибке игроку.
     */
    public void sendError(CommandSender sender, String message) {
        sender.sendMessage(MessageFormatter.error(message));
    }

    /**
     * Отправить сообщение об ошибке игроку с причиной.
     */
    public void sendError(CommandSender sender, String message, String reason) {
        sender.sendMessage(MessageFormatter.error(message + ": " + reason));
    }

    /**
     * Залогировать ошибку.
     */
    public void logError(String message) {
        logger.log(Level.SEVERE, prefix + message);
    }

    /**
     * Залогировать ошибку с исключением.
     */
    public void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, prefix + message, throwable);
    }

    /**
     * Отправить ошибку игроку и залогировать.
     */
    public void handleError(CommandSender sender, String userMessage, String logMessage) {
        sendError(sender, userMessage);
        logError(logMessage);
    }

    /**
     * Отправить ошибку игроку и залогировать с исключением.
     */
    public void handleError(CommandSender sender, String userMessage, String logMessage, Throwable throwable) {
        sendError(sender, userMessage);
        logError(logMessage, throwable);
    }

    /**
     * Обработать ошибку валидации.
     */
    public void handleValidationError(CommandSender sender, String fieldName, String value) {
        sendError(sender, "Некорректное значение для " + fieldName + ": " + value);
    }

    /**
     * Обработать ошибку "не найдено".
     */
    public void handleNotFound(CommandSender sender, String entityType, String identifier) {
        sendError(sender, entityType + " '" + identifier + "' не найден");
    }

    /**
     * Обработать ошибку доступа.
     */
    public void handlePermissionDenied(CommandSender sender, String action) {
        sendError(sender, "У вас нет прав для: " + action);
    }
}
