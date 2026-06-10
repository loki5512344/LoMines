package dev.loki.lomines.data.config.reset;

import java.time.Duration;
import java.util.List;

/**
 * Configuration for mine reset behavior.
 */
public record ResetConfig(
        Duration interval,
        double percentTrigger,
        boolean percentEnabled,
        List<String> commands,
        String broadcastMessage
) {

    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(5);
    public static final double DEFAULT_PERCENT_TRIGGER = 10.0;

    public ResetConfig {
        // Normalize and validate interval
        if (interval == null || interval.isZero() || interval.isNegative()) {
            interval = DEFAULT_INTERVAL;
        }

        // Ensure interval is reasonable (1 second to 24 hours)
        if (interval.getSeconds() < 1) {
            interval = Duration.ofSeconds(1);
        } else if (interval.getSeconds() > 86400) {
            interval = Duration.ofHours(24);
        }

        // Validate percent trigger
        if (percentTrigger < 0 || percentTrigger > 100) {
            percentTrigger = DEFAULT_PERCENT_TRIGGER;
        }

        commands = commands != null ? List.copyOf(commands) : List.of();
        broadcastMessage = broadcastMessage != null ? broadcastMessage : "";
    }

    /**
     * Default config: 5 minutes, 10% trigger disabled.
     */
    public static ResetConfig defaults() {
        return new ResetConfig(
                DEFAULT_INTERVAL,
                DEFAULT_PERCENT_TRIGGER,
                false,
                List.of(),
                ""
        );
    }

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Interval in ticks (20 ticks = 1 second).
     */
    public long intervalTicks() {
        return interval.getSeconds() * 20;
    }

    /**
     * Human-readable interval string.
     */
    public String intervalDisplay() {
        long seconds = interval.getSeconds();
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else {
            return (seconds / 3600) + "h";
        }
    }

    /**
     * Whether reset should trigger based on percent mined.
     */
    public boolean isPercentTriggerEnabled() {
        return percentEnabled && percentTrigger > 0;
    }

    public static class Builder {
        private Duration interval = DEFAULT_INTERVAL;
        private double percentTrigger = DEFAULT_PERCENT_TRIGGER;
        private boolean percentEnabled = false;
        private List<String> commands = List.of();
        private String broadcastMessage = "";

        private static Duration parseDuration(String s) {
            s = s.trim().toLowerCase();
            try {
                if (s.endsWith("s")) {
                    return Duration.ofSeconds(Long.parseLong(s.substring(0, s.length() - 1)));
                } else if (s.endsWith("m")) {
                    return Duration.ofMinutes(Long.parseLong(s.substring(0, s.length() - 1)));
                } else if (s.endsWith("h")) {
                    return Duration.ofHours(Long.parseLong(s.substring(0, s.length() - 1)));
                } else if (s.endsWith("d")) {
                    return Duration.ofDays(Long.parseLong(s.substring(0, s.length() - 1)));
                } else {
                    // Assume seconds if no suffix
                    return Duration.ofSeconds(Long.parseLong(s));
                }
            } catch (NumberFormatException e) {
                return DEFAULT_INTERVAL;
            }
        }

        public Builder interval(Duration interval) {
            this.interval = interval;
            return this;
        }

        public Builder intervalTicks(long ticks) {
            this.interval = Duration.ofSeconds(ticks / 20);
            return this;
        }

        public Builder interval(String humanReadable) {
            this.interval = parseDuration(humanReadable);
            return this;
        }

        public Builder percentTrigger(double percent) {
            this.percentTrigger = percent;
            return this;
        }

        public Builder percentEnabled(boolean enabled) {
            this.percentEnabled = enabled;
            return this;
        }

        public Builder commands(List<String> commands) {
            this.commands = commands;
            return this;
        }

        public Builder broadcastMessage(String message) {
            this.broadcastMessage = message;
            return this;
        }

        public ResetConfig build() {
            return new ResetConfig(interval, percentTrigger, percentEnabled, commands, broadcastMessage);
        }
    }
}
