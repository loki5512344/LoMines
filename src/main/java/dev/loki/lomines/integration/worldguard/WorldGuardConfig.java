package dev.loki.lomines.integration.worldguard;

import java.util.List;
import java.util.Random;

/**
 * Configuration for WorldGuard auto-region creation.
 * Supports any WorldGuard flags, default is only block-break=allow.
 */
public record WorldGuardConfig(
        boolean enabled,
        String regionNameTemplate,
        List<String> owners,
        List<String> members,
        List<String> flags,
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
        // Default: only block-break=allow. User can add any flags they want.
        flags = flags != null && !flags.isEmpty() ? List.copyOf(flags) : List.of("block-break=allow");
        protectOnCreate = protectOnCreate;
    }

    /**
     * Default configuration with auto-region enabled.
     * Default flag: block-break=allow (everyone can mine).
     * User can add any other flags.
     */
    public static WorldGuardConfig defaults() {
        return new WorldGuardConfig(
                true,
                DEFAULT_TEMPLATE,
                List.of(),
                List.of(),
                List.of("block-break=allow"),
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
                List.of(),
                false
        );
    }

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private boolean enabled = true;
        private String template = DEFAULT_TEMPLATE;
        private List<String> owners = List.of();
        private List<String> members = List.of();
        private List<String> flags = List.of("block-break=allow");
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

        public Builder flags(List<String> flags) {
            this.flags = flags;
            return this;
        }

        public Builder protectOnCreate(boolean protect) {
            this.protectOnCreate = protect;
            return this;
        }

        public WorldGuardConfig build() {
            return new WorldGuardConfig(enabled, template, owners, members, flags, protectOnCreate);
        }
    }
}