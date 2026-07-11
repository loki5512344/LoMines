package dev.loki.lomines.data.config.reset;

import java.time.Duration;
import java.util.List;

public record ResetConfig(
        Duration interval,
        double percentTrigger,
        boolean percentEnabled,
        List<String> commands,
        String broadcastMessage
) {

    public ResetConfig {
        interval = ResetConfigValidator.validateInterval(interval);
        percentTrigger = ResetConfigValidator.validatePercentTrigger(percentTrigger);
        commands = commands != null ? List.copyOf(commands) : List.of();
        broadcastMessage = broadcastMessage != null ? broadcastMessage : "";
    }

    public static final Duration DEFAULT_INTERVAL = ResetConfigValidator.DEFAULT_INTERVAL;
    public static final double DEFAULT_PERCENT_TRIGGER = ResetConfigValidator.DEFAULT_PERCENT_TRIGGER;

    public static ResetConfig defaults() {
        return new ResetConfig(
                DEFAULT_INTERVAL,
                DEFAULT_PERCENT_TRIGGER,
                false,
                List.of(),
                ""
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public long intervalTicks() {
        return interval.getSeconds() * 20;
    }

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

    public boolean isPercentTriggerEnabled() {
        return percentEnabled && percentTrigger > 0;
    }

    public static class Builder {
        private Duration interval = DEFAULT_INTERVAL;
        private double percentTrigger = DEFAULT_PERCENT_TRIGGER;
        private boolean percentEnabled = false;
        private List<String> commands = List.of();
        private String broadcastMessage = "";

        public Builder interval(Duration interval) {
            this.interval = interval;
            return this;
        }

        public Builder interval(String humanReadable) {
            this.interval = ResetConfigValidator.parseDuration(humanReadable);
            return this;
        }

        public Builder intervalTicks(long ticks) {
            this.interval = Duration.ofSeconds(ticks / 20);
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
