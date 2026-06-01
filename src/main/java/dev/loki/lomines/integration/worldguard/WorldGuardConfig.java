package dev.loki.lomines.integration.worldguard;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Configuration for WorldGuard auto-region creation.
 * Simplified: only controls who can break blocks in the mine.
 */
public record WorldGuardConfig(
        boolean enabled,
        String regionNameTemplate,
        List<String> owners,
        List<String> members,
        String blockBreak,
        boolean protectOnCreate
) {

    private static final Random RANDOM = new Random();
    private static final String DEFAULT_TEMPLATE = "{mine_name}_{random_4}";

    public WorldGuardConfig {
        enabled = enabled;
        regionNameTemplate = regionNameTemplate != null && !regionNameTemplate.isBlank()
                ? regionNameTemplate
                : DEFAULT_TEMPLATE;
        owners = owners != null ? List.copyOf(owners) : List.of();
        members = members != null ? List.copyOf(members) : List.of();
        // block-break: allow = все могут ломать, deny = никто не может, -g non_members = только владельцы
        blockBreak = blockBreak != null && !blockBreak.isBlank() ? blockBreak : "allow";
        protectOnCreate = protectOnCreate;
    }

    /**
     * Generates a region name from the template.
     *
     * @param mineName the name of the mine
     * @return the generated region name
     */
    public String generateRegionName(String mineName) {
        String result = regionNameTemplate;

        // Replace {mine_name}
        result = result.replace("{mine_name}", mineName);

        // Replace {random_4} with 4 random digits
        if (result.contains("{random_4}")) {
            int randomNum = RANDOM.nextInt(10000);
            result = result.replace("{random_4}", String.format("%04d", randomNum));
        }

        // Replace {random_3} with 3 random digits
        if (result.contains("{random_3}")) {
            int randomNum = RANDOM.nextInt(1000);
            result = result.replace("{random_3}", String.format("%03d", randomNum));
        }

        // Replace {random_6} with 6 random alphanumeric
        if (result.contains("{random_6}")) {
            result = result.replace("{random_6}", generateRandomString(6));
        }

        // Sanitize for WorldGuard (only alphanumeric, underscore, hyphen)
        return result.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Default configuration with auto-region enabled.
     * Everyone can break blocks (allow).
     */
    public static WorldGuardConfig defaults() {
        return new WorldGuardConfig(
                true,
                DEFAULT_TEMPLATE,
                List.of(),
                List.of(),
                "allow", // allow = все могут ломать
                true
        );
    }

    /**
     * Disabled configuration.
     */
    public static WorldGuardConfig disabled() {
        return new WorldGuardConfig(
                false,
                DEFAULT_TEMPLATE,
                List.of(),
                List.of(),
                "allow",
                false
        );
    }

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean enabled = true;
        private String template = DEFAULT_TEMPLATE;
        private List<String> owners = List.of();
        private List<String> members = List.of();
        private String blockBreak = "allow";
        private boolean protectOnCreate = true;

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder template(String template) {
            this.template = template;
            return this;
        }

        public Builder owners(List<String> owners) {
            this.owners = owners;
            return this;
        }

        public Builder members(List<String> members) {
            this.members = members;
            return this;
        }

        public Builder blockBreak(String blockBreak) {
            this.blockBreak = blockBreak;
            return this;
        }

        public Builder protectOnCreate(boolean protect) {
            this.protectOnCreate = protect;
            return this;
        }

        public WorldGuardConfig build() {
            return new WorldGuardConfig(enabled, template, owners, members, blockBreak, protectOnCreate);
        }
    }
