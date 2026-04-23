package com.loki.lomines.listener;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.gui.GroupCreateGui;
import com.loki.lomines.wand.GroupWandItem;
import com.loki.lomines.wand.GroupWandSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Group wand: ЛКМ / ПКМ по блоку — углы; Shift+ПКМ — GUI группы.
 */
public final class PlayerInteractListener implements Listener {
    
    private final LoMinesPlugin plugin;
    
    public PlayerInteractListener(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        if (!GroupWandItem.isWand(plugin, player.getInventory().getItemInMainHand())) {
            return;
        }
        Action action = event.getAction();
        
        if (player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            GroupCreateGui.open(plugin, player);
            return;
        }
        
        if (action == Action.LEFT_CLICK_BLOCK && event.hasBlock()) {
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            if (block == null) {
                return;
            }
            GroupWandSession session = plugin.getGroupWandManager().getSession(player.getUniqueId());
            int slot = session.getActiveSlot();
            session.setPos1(slot, block.getLocation());
            player.sendMessage(Component.text(
                "Угол 1 для шахты " + (slot + 1) + " установлен.", NamedTextColor.GREEN));
            return;
        }
        
        if (action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            if (block == null) {
                return;
            }
            GroupWandSession session = plugin.getGroupWandManager().getSession(player.getUniqueId());
            int slot = session.getActiveSlot();
            session.setPos2(slot, block.getLocation());
            player.sendMessage(Component.text(
                "Угол 2 для шахты " + (slot + 1) + " установлен.", NamedTextColor.GREEN));
        }
    }
}
