package dev.loki.lomines.integration.worldguard.config;

import java.util.List;

public record WorldGuardConfig(
        boolean enabled,
        String regionNameTemplate,
        List<String> owners,
        List<String> members,
        List<String> flags,
        boolean protectOnCreate
) {

    public WorldGuardConfig {
        enabled = enabled;
        regionNameTemplate = regionNameTemplate != null && !regionNameTemplate.isBlank()
                ? regionNameTemplate
                : RegionTemplateConfig.DEFAULT_TEMPLATE;
        owners = owners != null ? List.copyOf(owners) : List.of();
        members = members != null ? List.copyOf(members) : List.of();
        flags = flags != null && !flags.isEmpty() ? List.copyOf(flags) : List.of("block-break=allow");
        protectOnCreate = protectOnCreate;
    }

    public static WorldGuardConfig defaults() {
        return new WorldGuardConfig(
                true,
                RegionTemplateConfig.DEFAULT_TEMPLATE,
                List.of(),
                List.of(),
                List.of("block-break=allow"),
                true
        );
    }

    public static WorldGuardConfig disabled() {
        return new WorldGuardConfig(
                false,
                RegionTemplateConfig.DEFAULT_TEMPLATE,
                List.of(),
                List.of(),
                List.of(),
                false
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public String generateRegionName(String mineName) {
        return RegionTemplateConfig.resolveTemplate(regionNameTemplate, mineName);
    }

    public static class Builder {
        private boolean enabled = true;
        private String template = RegionTemplateConfig.DEFAULT_TEMPLATE;
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
