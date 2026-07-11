package dev.loki.lomines.command;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.command.admin.manage.AdminCommands;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminCommands class.
 */
class AdminCommandsTest {

    @Mock
    private LoMinesPlugin plugin;

    @Mock
    private Mines mines;

    @Mock
    private CommandSender sender;

    @Mock
    private Mine mine;

    private AdminCommands commands;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getMines()).thenReturn(mines);
        commands = new AdminCommands(plugin);
    }

    @Test
    void testCreateMineSuccess() throws IOException {
        String mineName = "testmine";

        commands.handle(sender, "create", new String[]{mineName});

        verify(mines).create(mineName);
        verify(sender, times(2)).sendMessage(any(Component.class));
    }

    @Test
    void testCreateMineAlreadyExists() throws IOException {
        String mineName = "existing";
        doThrow(new IllegalArgumentException("Mine already exists")).when(mines).create(mineName);

        commands.handle(sender, "create", new String[]{mineName});

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testDeleteMineSuccess() throws IOException {
        String mineName = "testmine";

        commands.handle(sender, "delete", new String[]{mineName});

        verify(mines).delete(mineName);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testDeleteMineNotFound() throws IOException {
        String mineName = "nonexistent";
        doThrow(new IllegalArgumentException("Mine not found")).when(mines).delete(mineName);

        commands.handle(sender, "delete", new String[]{mineName});

        verify(sender).sendMessage(any(Component.class));
    }
}
