package dev.loki.lomines.listener;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.GroupCreateGui;
import dev.loki.lomines.gui.GroupCreateGuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Handles clicks in the group-create chest GUI.
 */
public final class GroupGuiListener implements Listener {

    private final LoMinesPlugin plugin;

    public GroupGuiListener(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (!(view.getTopInventory().getHolder() instanceof GroupCreateGuiHolder holder)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= view.getTopInventory().getSize()) {
            return;
        }
        GroupCreateGui.handleClick(plugin, player, raw);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GroupCreateGuiHolder) {
            event.setCancelled(true);
        }
    }
}
