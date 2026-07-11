package dev.loki.lomines.data.config.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIConfigTest {

    @Test
    void testDefaults() {
        UIConfig config = UIConfig.defaults();

        assertTrue(config.actionBarEnabled());
        assertEquals(UIConfig.DEFAULT_ACTIONBAR_FORMAT, config.actionBarFormat());
        assertEquals(50.0, config.actionBarRange());
        assertEquals(UIConfig.DEFAULT_TIMER_FORMAT, config.timerFormat());
    }

    @Test
    void testDisabled() {
        UIConfig config = UIConfig.disabled();

        assertFalse(config.actionBarEnabled());
        assertEquals(UIConfig.DEFAULT_ACTIONBAR_FORMAT, config.actionBarFormat());
        assertEquals(1.0, config.actionBarRange());
    }

    @Test
    void testCustomConfig() {
        UIConfig config = new UIConfig(
                true,
                "<red>{mine}</red>",
                100.0,
                "HH:mm:ss",
                null
        );

        assertTrue(config.actionBarEnabled());
        assertEquals("<red>{mine}</red>", config.actionBarFormat());
        assertEquals(100.0, config.actionBarRange());
        assertEquals("HH:mm:ss", config.timerFormat());
    }

    @Test
    void testNullFormatDefaults() {
        UIConfig config = new UIConfig(true, null, 50.0, null, null);

        assertEquals(UIConfig.DEFAULT_ACTIONBAR_FORMAT, config.actionBarFormat());
        assertEquals(UIConfig.DEFAULT_TIMER_FORMAT, config.timerFormat());
    }

    @Test
    void testBlankFormatDefaults() {
        UIConfig config = new UIConfig(true, "   ", 50.0, "   ", null);

        assertEquals(UIConfig.DEFAULT_ACTIONBAR_FORMAT, config.actionBarFormat());
        assertEquals(UIConfig.DEFAULT_TIMER_FORMAT, config.timerFormat());
    }

    @Test
    void testNegativeRangeClamped() {
        UIConfig config = new UIConfig(true, "test", -10.0, "mm:ss", null);

        assertEquals(1.0, config.actionBarRange());
    }

    @Test
    void testRangeSquared() {
        UIConfig config = new UIConfig(true, "test", 50.0, "mm:ss", null);

        assertEquals(2500.0, config.actionBarRangeSquared(), 0.001);
    }

    @Test
    void testFormatActionBar() {
        UIConfig config = UIConfig.defaults();
        Component result = config.formatActionBar("TestMine", 50.5, "02:30", 500, 1000);

        String serialized = MiniMessage.miniMessage().serialize(result);
        assertTrue(serialized.contains("TestMine"));
        assertTrue(serialized.contains("50.5"));
        assertTrue(serialized.contains("02:30"));
    }

    @Test
    void testFormatTimerMmSs() {
        UIConfig config = new UIConfig(true, "", 0, "mm:ss", null);

        assertEquals("00:30", config.formatTimer(30));
        assertEquals("05:00", config.formatTimer(300));
        assertEquals("59:59", config.formatTimer(3599));
    }

    @Test
    void testFormatTimerHhMmSs() {
        UIConfig config = new UIConfig(true, "", 0, "HH:mm:ss", null);

        assertEquals("0:00:30", config.formatTimer(30));
        assertEquals("0:05:00", config.formatTimer(300));
        assertEquals("1:00:00", config.formatTimer(3600));
        assertEquals("2:30:45", config.formatTimer(9045));
    }

    @Test
    void testFormatTimerDefault() {
        UIConfig config = UIConfig.defaults();

        assertEquals("02:30", config.formatTimer(150));
        assertEquals("10:00", config.formatTimer(600));
    }
}
