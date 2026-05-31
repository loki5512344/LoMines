package dev.loki.lomines.listener;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

/**
 * Listens for block break events in mines.
 * This is a placeholder implementation that will be completed in task 11.1.
 */
public final class BlockBreakListener implements Listener {

    private final LoMinesPlugin plugin;

    public BlockBreakListener(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<Mine> mine = plugin.getMines().findByLocation(event.getBlock().getLocation());

        if (mine.isPresent()) {
            Mine m = mine.get();
            if (m.appliesToBlock(event.getBlock().getLocation())) {
                m.onBlockBreak(event.getPlayer(), event.getBlock());
            }
        }
    }
}
