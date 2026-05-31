package dev.loki.lomines.command;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.core.Mines;
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

import static org.mockito.Mockito.*;

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

        commands.create(sender, mineName);

        verify(mines).create(mineName);
        verify(sender, times(2)).sendMessage(any(Component.class));
    }

    @Test
    void testCreateMineAlreadyExists() throws IOException {
        String mineName = "existing";
        doThrow(new IllegalArgumentException("Mine already exists")).when(mines).create(mineName);

        commands.create(sender, mineName);

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testDeleteMineSuccess() throws IOException {
        String mineName = "testmine";

        commands.delete(sender, mineName);

        verify(mines).delete(mineName);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testDeleteMineNotFound() throws IOException {
        String mineName = "nonexistent";
        doThrow(new IllegalArgumentException("Mine not found")).when(mines).delete(mineName);

        commands.delete(sender, mineName);

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testResetMineSuccess() {
        String mineName = "testmine";
        when(mines.get(mineName)).thenReturn(mine);

        commands.reset(sender, mineName, false);

        verify(mine).reset(false);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testResetMineSilent() {
        String mineName = "testmine";
        when(mines.get(mineName)).thenReturn(mine);

        commands.reset(sender, mineName, true);

        verify(mine).reset(true);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testResetMineNotFound() {
        String mineName = "nonexistent";
        when(mines.get(mineName)).thenThrow(new IllegalArgumentException("Mine not found"));

        commands.reset(sender, mineName, false);

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testReloadSuccess() throws IOException {
        Collection<Mine> mineList = Arrays.asList(mine, mine);
        when(mines.getAll()).thenReturn(mineList);

        commands.reload(sender);

        verify(mine, times(2)).stop();
        verify(mines).loadAll();
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testListMinesEmpty() {
        when(mines.getAll()).thenReturn(List.of());

        commands.list(sender);

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testListMinesWithData() {
        when(mines.getAll()).thenReturn(Arrays.asList(mine, mine));
        when(mine.getName()).thenReturn("mine1");
        when(mine.getBlocks()).thenReturn(100);
        when(mine.getTotalVolume()).thenReturn(200);
        when(mine.getPercentFilled()).thenReturn(50.0);

        commands.list(sender);

        verify(sender, atLeast(3)).sendMessage(any(Component.class));
    }
}
