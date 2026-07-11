package dev.loki.lomines.util.format.color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ColorUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ColorUtils() {
    }

    public static Component format(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        if (text.contains("<") && text.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(text);
            } catch (Exception ignored) {
            }
        }

        String miniMessageText = HexColorConverter.convertHexToMiniMessage(text);
        miniMessageText = HexColorConverter.convertLegacyHexToMiniMessage(miniMessageText);
        miniMessageText = LegacyColorConverter.convertLegacyToMiniMessage(miniMessageText);

        try {
            return MINI_MESSAGE.deserialize(miniMessageText);
        } catch (Exception e) {
            return LEGACY_SERIALIZER.deserialize(text);
        }
    }

    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9A-Fa-fk-orK-OR]", "")
                .replaceAll("<[^>]+>", "");
    }

    public static String toLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = HexColorConverter.convertMiniMessageToLegacyHex(text);
        result = LegacyColorConverter.convertMiniMessageToLegacy(result);
        return result;
    }
}
