package dev.loki.lomines.listener;

import dev.loki.lomines.LoMinesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listens for player join events to load statistics.
 * This is a placeholder implementation that will be completed in task 11.3.
 */
public final class PlayerJoinListener implements Listener {

    private final LoMinesPlugin plugin;

    public PlayerJoinListener(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load or create player statistics
        plugin.getStatsManager().getOrCreate(event.getPlayer().getUniqueId());
    }
}
