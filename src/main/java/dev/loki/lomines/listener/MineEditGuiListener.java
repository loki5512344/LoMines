package dev.loki.lomines.listener;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGui;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGuiHolder;
import dev.loki.lomines.gui.mine.MineEditGui;
import dev.loki.lomines.gui.mine.MineEditGuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Handles clicks in the mine editor GUIs.
 * Covers MineEditGui and ConfirmDeleteGui.
 */
public final class MineEditGuiListener implements Listener {

    private final LoMinesPlugin plugin;

    public MineEditGuiListener(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();

        // Handle MineEditGui
        if (view.getTopInventory().getHolder() instanceof MineEditGuiHolder holder) {
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
            MineEditGui.handleClick(plugin, player, raw, holder.getMineName());
            return;
        }

        // Handle ConfirmDeleteGui
        if (view.getTopInventory().getHolder() instanceof ConfirmDeleteGuiHolder holder) {
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
            ConfirmDeleteGui.handleClick(plugin, player, raw, holder.getMineName());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MineEditGuiHolder ||
                event.getInventory().getHolder() instanceof ConfirmDeleteGuiHolder) {
            event.setCancelled(true);
        }
    }
}
