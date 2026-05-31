package dev.loki.lomines.data.config.reset;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResetConfigTest {

    @Test
    void testDefaultConfig() {
        ResetConfig config = ResetConfig.defaults();

        assertEquals(Duration.ofMinutes(5), config.interval());
        assertEquals(10.0, config.percentTrigger());
        assertFalse(config.percentEnabled());
        assertTrue(config.commands().isEmpty());
        assertEquals("", config.broadcastMessage());
    }

    @Test
    void testBuilder() {
        ResetConfig config = ResetConfig.builder()
                .interval("10m")
                .percentTrigger(25.0)
                .percentEnabled(true)
                .commands(List.of("broadcast Reset!", "eco give %player% 10"))
                .broadcastMessage("Mine reset!")
                .build();

        assertEquals(Duration.ofMinutes(10), config.interval());
        assertEquals(25.0, config.percentTrigger());
        assertTrue(config.percentEnabled());
        assertEquals(2, config.commands().size());
        assertEquals("Mine reset!", config.broadcastMessage());
    }

    @Test
    void testBuilderWithTicks() {
        ResetConfig config = ResetConfig.builder()
                .intervalTicks(1200) // 60 seconds
                .build();

        assertEquals(Duration.ofSeconds(60), config.interval());
        assertEquals(1200, config.intervalTicks());
    }

    @Test
    void testIntervalParsing() {
        assertEquals(Duration.ofSeconds(30), ResetConfig.builder().interval("30s").build().interval());
        assertEquals(Duration.ofMinutes(5), ResetConfig.builder().interval("5m").build().interval());
        assertEquals(Duration.ofHours(2), ResetConfig.builder().interval("2h").build().interval());
        assertEquals(Duration.ofDays(1), ResetConfig.builder().interval("1d").build().interval());
    }

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
                .interval("0.5s")
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
    void testIntervalDisplay() {
        assertEquals("30s", new ResetConfig(Duration.ofSeconds(30), 10.0, false, List.of(), "").intervalDisplay());
        assertEquals("5m", new ResetConfig(Duration.ofMinutes(5), 10.0, false, List.of(), "").intervalDisplay());
        assertEquals("2h", new ResetConfig(Duration.ofHours(2), 10.0, false, List.of(), "").intervalDisplay());
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
