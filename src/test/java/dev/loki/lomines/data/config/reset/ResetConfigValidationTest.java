package dev.loki.lomines.data.config.reset;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResetConfigValidationTest {

    @Test
    void testInvalidIntervalDefaultsToFiveMinutes() {
        ResetConfig config = ResetConfig.builder()
                .interval("invalid")
                .build();

        assertEquals(ResetConfig.DEFAULT_INTERVAL, config.interval());
    }

    @Test
    void testNegativeIntervalDefaults() {
        ResetConfig config = new ResetConfig(
                Duration.ofSeconds(-10),
                10.0,
                false,
                List.of(),
                ""
        );

        assertEquals(ResetConfig.DEFAULT_INTERVAL, config.interval());
    }

    @Test
    void testZeroIntervalDefaults() {
        ResetConfig config = new ResetConfig(
                Duration.ZERO,
                10.0,
                false,
                List.of(),
                ""
        );

        assertEquals(ResetConfig.DEFAULT_INTERVAL, config.interval());
    }

    @Test
    void testTooShortIntervalClamped() {
        ResetConfig config = ResetConfig.builder()
                .interval(Duration.ofMillis(500))
                .build();

        assertEquals(Duration.ofSeconds(1), config.interval());
    }

    @Test
    void testTooLongIntervalClamped() {
        ResetConfig config = ResetConfig.builder()
                .interval("48h")
                .build();

        assertEquals(Duration.ofHours(24), config.interval());
    }

    @Test
    void testInvalidPercentTriggerClamped() {
        ResetConfig negative = new ResetConfig(
                Duration.ofMinutes(5),
                -10.0,
                false,
                List.of(),
                ""
        );
        assertEquals(ResetConfig.DEFAULT_PERCENT_TRIGGER, negative.percentTrigger());

        ResetConfig over100 = new ResetConfig(
                Duration.ofMinutes(5),
                150.0,
                false,
                List.of(),
                ""
        );
        assertEquals(ResetConfig.DEFAULT_PERCENT_TRIGGER, over100.percentTrigger());
    }

    @Test
    void testPercentTriggerEnabled() {
        assertFalse(ResetConfig.defaults().isPercentTriggerEnabled());
        assertFalse(new ResetConfig(Duration.ofMinutes(5), 10.0, false, List.of(), "").isPercentTriggerEnabled());
        assertTrue(new ResetConfig(Duration.ofMinutes(5), 10.0, true, List.of(), "").isPercentTriggerEnabled());
        assertFalse(new ResetConfig(Duration.ofMinutes(5), 0.0, true, List.of(), "").isPercentTriggerEnabled());
    }

    @Test
    void testImmutableCommands() {
        ResetConfig config = ResetConfig.builder()
                .commands(List.of("cmd1"))
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                config.commands().add("cmd2"));
    }

    @Test
    void testNullCommandsDefaultsToEmpty() {
        ResetConfig config = new ResetConfig(
                Duration.ofMinutes(5),
                10.0,
                false,
                null,
                "test"
        );

        assertTrue(config.commands().isEmpty());
    }
}
