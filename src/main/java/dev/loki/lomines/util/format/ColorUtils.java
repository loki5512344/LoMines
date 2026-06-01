package dev.loki.lomines.util.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for converting color formats.
 * Supports: &#RRGGBB (HEX), &x&R&R&G&G&B&B (Bukkit HEX), MiniMessage
 */
public final class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&x([&0-9A-Fa-f]){12}");

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ColorUtils() {
        // Utility class
    }

    /**
     * Converts a string with color codes to a Component.
     * Supports:
     * - &#RRGGBB - HEX colors
     * - &x&R&R&G&G&B&B - Legacy Bukkit HEX
     * - &a, &b, ... - Legacy colors
     * - <color>, <#RRGGBB> - MiniMessage (pass-through)
     */
    public static Component format(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Check if it's already MiniMessage format
        if (text.contains("<") && text.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(text);
            } catch (Exception e) {
                // Fall through to legacy conversion
            }
        }

        // Convert &#RRGGBB to MiniMessage <#RRGGBB>
        String miniMessageText = convertHexToMiniMessage(text);

        // Convert legacy &x format if present
        miniMessageText = convertLegacyHexToMiniMessage(miniMessageText);

        // Convert remaining legacy codes (&a, &l, etc.) to MiniMessage
        miniMessageText = convertLegacyToMiniMessage(miniMessageText);

        try {
            return MINI_MESSAGE.deserialize(miniMessageText);
        } catch (Exception e) {
            // Fallback: use legacy serializer
            return LEGACY_SERIALIZER.deserialize(text);
        }
    }

    /**
     * Converts &#RRGGBB to <#RRGGBB> for MiniMessage.
     */
    private static String convertHexToMiniMessage(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "<#" + hex + ">");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Converts &x&R&R&G&G&B&B to <#RRGGBB> for MiniMessage.
     */
    private static String convertLegacyHexToMiniMessage(String text) {
        if (!text.contains("&x")) {
            return text;
        }

        // Pattern for &x&R&R&G&G&B&B
        Pattern bukkitHex = Pattern.compile("&x(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])");
        Matcher matcher = bukkitHex.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String r1 = matcher.group(1).substring(1);
            String r2 = matcher.group(2).substring(1);
            String g1 = matcher.group(3).substring(1);
            String g2 = matcher.group(4).substring(1);
            String b1 = matcher.group(5).substring(1);
            String b2 = matcher.group(6).substring(1);
            String hex = r1 + r2 + g1 + g2 + b1 + b2;
            matcher.appendReplacement(result, "<#" + hex + ">");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Converts legacy & codes to MiniMessage tags.
     */
    private static String convertLegacyToMiniMessage(String text) {
        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }

    /**
     * Strips all color codes from text.
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9A-Fa-fk-orK-OR]", "")
                .replaceAll("<[^>]+>", "");
    }

    /**
     * Converts to legacy color format (&a, &c, &x&R&R&G&G&B&B).
     * Useful for plugins that don't support MiniMessage.
     */
    public static String toLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = text;

        // Convert MiniMessage <#RRGGBB> to &x&R&R&G&G&B&B
        Pattern hexTag = Pattern.compile("<#([0-9A-Fa-f]{6})>");
        Matcher hexMatcher = hexTag.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            StringBuilder legacy = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                legacy.append("&").append(c);
            }
            hexMatcher.appendReplacement(sb, legacy.toString());
        }
        hexMatcher.appendTail(sb);
        result = sb.toString();

        // Convert MiniMessage named colors to legacy
        result = result
                .replace("<black>", "&0")
                .replace("<dark_blue>", "&1")
                .replace("<dark_green>", "&2")
                .replace("<dark_aqua>", "&3")
                .replace("<dark_red>", "&4")
                .replace("<dark_purple>", "&5")
                .replace("<gold>", "&6")
                .replace("<gray>", "&7")
                .replace("<dark_gray>", "&8")
                .replace("<blue>", "&9")
                .replace("<green>", "&a")
                .replace("<aqua>", "&b")
                .replace("<red>", "&c")
                .replace("<light_purple>", "&d")
                .replace("<yellow>", "&e")
                .replace("<white>", "&f")
                .replace("<obfuscated>", "&k")
                .replace("<bold>", "&l")
                .replace("<strikethrough>", "&m")
                .replace("<underlined>", "&n")
                .replace("<italic>", "&o")
                .replace("<reset>", "&r");

        // &#RRGGBB is already in a good format for most plugins
        return result;
    }
}
