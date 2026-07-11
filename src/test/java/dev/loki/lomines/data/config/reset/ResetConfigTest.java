package dev.loki.lomines.data.config.reset;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testIntervalDisplay() {
        assertEquals("30s", new ResetConfig(Duration.ofSeconds(30), 10.0, false, List.of(), "").intervalDisplay());
        assertEquals("5m", new ResetConfig(Duration.ofMinutes(5), 10.0, false, List.of(), "").intervalDisplay());
        assertEquals("2h", new ResetConfig(Duration.ofHours(2), 10.0, false, List.of(), "").intervalDisplay());
    }
}
