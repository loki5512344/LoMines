package dev.loki.lomines.listener;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.group.GroupCreateGui;
import dev.loki.lomines.wand.GroupWandItem;
import dev.loki.lomines.wand.GroupWandSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Group wand: ЛКМ / ПКМ по блоку — углы; Shift+ПКМ — GUI группы.
 * Also starts/stops particle visualization when holding the wand.
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

            // Start showing particles when selection is made
            plugin.getWandParticleService().startShowingParticles(player);
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

            // Start showing particles when selection is made
            plugin.getWandParticleService().startShowingParticles(player);
        }
    }

    /**
     * Starts/stops particle visualization when player switches items.
     */
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Check if player switched to wand
        if (GroupWandItem.isWand(plugin, player.getInventory().getItem(event.getNewSlot()))) {
            plugin.getWandParticleService().startShowingParticles(player);
        }
        // Check if player switched away from wand
        else if (GroupWandItem.isWand(plugin, player.getInventory().getItem(event.getPreviousSlot()))) {
            plugin.getWandParticleService().stopShowingParticles(player.getUniqueId());
        }
    }

    /**
     * Stops particles when player disconnects.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getWandParticleService().stopShowingParticles(event.getPlayer().getUniqueId());
    }
}
