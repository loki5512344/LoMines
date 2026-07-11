package dev.loki.lomines.data.config.reset;

import java.time.Duration;

public final class ResetConfigValidator {

    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(5);
    public static final double DEFAULT_PERCENT_TRIGGER = 10.0;

    private ResetConfigValidator() {
    }

    public static Duration validateInterval(Duration interval) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            interval = DEFAULT_INTERVAL;
        }
        if (interval.getSeconds() < 1) {
            interval = Duration.ofSeconds(1);
        } else if (interval.getSeconds() > 86400) {
            interval = Duration.ofHours(24);
        }
        return interval;
    }

    public static double validatePercentTrigger(double percentTrigger) {
        if (percentTrigger < 0 || percentTrigger > 100) {
            return DEFAULT_PERCENT_TRIGGER;
        }
        return percentTrigger;
    }

    public static Duration parseDuration(String s) {
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
                return Duration.ofSeconds(Long.parseLong(s));
            }
        } catch (NumberFormatException e) {
            return DEFAULT_INTERVAL;
        }
    }
}
