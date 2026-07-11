package dev.loki.lomines.util.format.color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HexColorConverter {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final Pattern LEGACY_HEX_PATTERN =
            Pattern.compile("&x(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])(&[0-9A-Fa-f])");
    private static final Pattern HEX_TAG_PATTERN = Pattern.compile("<#([0-9A-Fa-f]{6})>");

    private HexColorConverter() {
    }

    public static String convertHexToMiniMessage(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "<#" + hex + ">");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String convertLegacyHexToMiniMessage(String text) {
        if (!text.contains("&x")) {
            return text;
        }
        Matcher matcher = LEGACY_HEX_PATTERN.matcher(text);
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

    public static String convertMiniMessageToLegacyHex(String text) {
        Matcher matcher = HEX_TAG_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder legacy = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                legacy.append("&").append(c);
            }
            matcher.appendReplacement(sb, legacy.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
