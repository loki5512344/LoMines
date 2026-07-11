package dev.loki.lomines.integration.worldguard.config;

import java.util.Random;

public final class RegionTemplateConfig {

    private static final Random RANDOM = new Random();
    public static final String DEFAULT_TEMPLATE = "{mine_name}_{random_4}";

    private RegionTemplateConfig() {
    }

    public static String generateRegionName(String regionNameTemplate, String mineName) {
        String result = regionNameTemplate;

        result = result.replace("{mine_name}", mineName);

        if (result.contains("{random_4}")) {
            int randomNum = RANDOM.nextInt(10000);
            result = result.replace("{random_4}", String.format("%04d", randomNum));
        }

        if (result.contains("{random_3}")) {
            int randomNum = RANDOM.nextInt(1000);
            result = result.replace("{random_3}", String.format("%03d", randomNum));
        }

        if (result.contains("{random_6}")) {
            result = result.replace("{random_6}", generateRandomString(6));
        }

        return result.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    private static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String resolveTemplate(String template, String mineName) {
        if (template == null || template.isBlank()) {
            return generateRegionName(DEFAULT_TEMPLATE, mineName);
        }
        return generateRegionName(template, mineName);
    }
}
