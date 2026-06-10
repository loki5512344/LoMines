package dev.loki.lomines.listener;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGui;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGuiHolder;
import dev.loki.lomines.gui.mine.edit.blocks.BlockMaterialSelectionGui;
import dev.loki.lomines.gui.mine.edit.blocks.BlocksGui;
import dev.loki.lomines.gui.mine.edit.reset.ResetGui;
import dev.loki.lomines.gui.mine.edit.rewards.RewardsGui;
import dev.loki.lomines.gui.mine.holder.*;
import dev.loki.lomines.gui.mine.main.MineEditGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Handles clicks in all mine editor GUIs.
 * Covers MineEditGui, BlocksGui, ResetGui, RewardsGui, ConfirmDeleteGui, and BlockMaterialSelectionGui.
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
            handleMineEditGui(event, holder);
            return;
        }

        // Handle BlocksGui
        if (view.getTopInventory().getHolder() instanceof BlocksGuiHolder holder) {
            handleBlocksGui(event, holder);
            return;
        }

        // Handle BlockMaterialSelectionGui
        if (view.getTopInventory().getHolder() instanceof BlockMaterialSelectionGuiHolder holder) {
            handleBlockMaterialSelectionGui(event, holder);
            return;
        }

        // Handle ResetGui
        if (view.getTopInventory().getHolder() instanceof ResetGuiHolder holder) {
            handleResetGui(event, holder);
            return;
        }

        // Handle RewardsGui
        if (view.getTopInventory().getHolder() instanceof RewardsGuiHolder holder) {
            handleRewardsGui(event, holder);
            return;
        }

        // Handle ConfirmDeleteGui
        if (view.getTopInventory().getHolder() instanceof ConfirmDeleteGuiHolder holder) {
            handleConfirmDeleteGui(event, holder);
        }
    }

    private void handleMineEditGui(InventoryClickEvent event, MineEditGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) return;
        MineEditGui.handleClick(plugin, player, raw, holder.getMineName());
    }

    private void handleBlocksGui(InventoryClickEvent event, BlocksGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) return;

        ClickType click = event.getClick();
        boolean left = click.isLeftClick();
        boolean right = click.isRightClick();
        boolean shift = click.isShiftClick();

        BlocksGui.handleClick(plugin, player, raw, holder.getMineName(),
                left, shift, right,
                BlocksGui.getBlockAtSlot(event.getView().getTopInventory(), raw, plugin, holder.getMineName()));
    }

    private void handleBlockMaterialSelectionGui(InventoryClickEvent event, BlockMaterialSelectionGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) return;

        BlockMaterialSelectionGui.handleClick(plugin, player, raw, holder.getMineName(),
                holder.getPage(), event.getClick().isLeftClick());
    }

    private void handleResetGui(InventoryClickEvent event, ResetGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) return;

        ClickType click = event.getClick();
        ResetGui.handleClick(plugin, player, raw, holder.getMineName(),
                click.isLeftClick(), click.isRightClick(), click.isShiftClick());
    }

    private void handleRewardsGui(InventoryClickEvent event, RewardsGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) return;

        ClickType click = event.getClick();
        int rewardIndex = raw < 45 ? raw : -1;
        RewardsGui.handleClick(plugin, player, raw, holder.getMineName(),
                click.isLeftClick(), click.isRightClick(), rewardIndex);
    }

    private void handleConfirmDeleteGui(InventoryClickEvent event, ConfirmDeleteGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) return;
        ConfirmDeleteGui.handleClick(plugin, player, raw, holder.getMineName());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof MineEditGuiHolder ||
                holder instanceof BlocksGuiHolder ||
                holder instanceof BlockMaterialSelectionGuiHolder ||
                holder instanceof ResetGuiHolder ||
                holder instanceof RewardsGuiHolder ||
                holder instanceof ConfirmDeleteGuiHolder) {
            event.setCancelled(true);
        }
    }
}
