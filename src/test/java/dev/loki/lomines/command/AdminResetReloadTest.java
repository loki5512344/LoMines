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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminResetReloadTest {

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
    void testResetMineSuccess() {
        String mineName = "testmine";
        when(mines.get(mineName)).thenReturn(mine);

        commands.handle(sender, "reset", new String[]{mineName});

        verify(mine).reset(false);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testResetMineSilent() {
        String mineName = "testmine";
        when(mines.get(mineName)).thenReturn(mine);

        commands.handle(sender, "reset", new String[]{mineName, "silent"});

        verify(mine).reset(true);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testResetMineNotFound() {
        String mineName = "nonexistent";
        when(mines.get(mineName)).thenThrow(new IllegalArgumentException("Mine not found"));

        commands.handle(sender, "reset", new String[]{mineName});

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testReloadSuccess() throws IOException {
        Collection<Mine> mineList = Arrays.asList(mine, mine);
        when(mines.getAll()).thenReturn(mineList);

        commands.handle(sender, "reload", new String[0]);

        verify(mine, times(2)).stop();
        verify(mines).loadAll();
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testListMinesEmpty() {
        when(mines.getAll()).thenReturn(List.of());

        commands.handle(sender, "list", new String[0]);

        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void testListMinesWithData() {
        when(mines.getAll()).thenReturn(Arrays.asList(mine, mine));
        when(mine.getName()).thenReturn("mine1");
        when(mine.getBlocks()).thenReturn(100);
        when(mine.getTotalVolume()).thenReturn(200);
        when(mine.getPercentFilled()).thenReturn(50.0);

        commands.handle(sender, "list", new String[0]);

        verify(sender, atLeast(3)).sendMessage(any(Component.class));
    }
}
