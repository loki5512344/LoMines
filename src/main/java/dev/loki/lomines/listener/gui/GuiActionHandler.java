package dev.loki.lomines.listener.gui;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGui;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGuiHolder;
import dev.loki.lomines.gui.mine.edit.blocks.select.BlockMaterialSelectionGui;
import dev.loki.lomines.gui.mine.edit.blocks.view.BlocksGui;
import dev.loki.lomines.gui.mine.edit.reset.ResetGui;
import dev.loki.lomines.gui.mine.edit.rewards.RewardsGui;
import dev.loki.lomines.gui.mine.holder.edit.BlockMaterialSelectionGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.BlocksGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.reset.ResetGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.rewards.RewardsGuiHolder;
import dev.loki.lomines.gui.mine.holder.main.MineEditGuiHolder;
import dev.loki.lomines.gui.mine.main.MineEditGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class GuiActionHandler {

    private final LoMinesPlugin plugin;

    public GuiActionHandler(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleMineEditGui(InventoryClickEvent event, MineEditGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }
        MineEditGui.handleClick(plugin, player, raw, holder.getMineName());
    }

    public void handleBlocksGui(InventoryClickEvent event, BlocksGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }

        ClickType click = event.getClick();
        boolean left = click.isLeftClick();
        boolean right = click.isRightClick();
        boolean shift = click.isShiftClick();

        BlocksGui.handleClick(plugin, player, raw, holder.getMineName(),
                left, shift, right,
                BlocksGui.getBlockAtSlot(event.getView().getTopInventory(), raw, plugin, holder.getMineName()));
    }

    public void handleBlockMaterialSelectionGui(InventoryClickEvent event, BlockMaterialSelectionGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }

        BlockMaterialSelectionGui.handleClick(plugin, player, raw, holder.getMineName(),
                holder.getPage(), event.getClick().isLeftClick());
    }

    public void handleResetGui(InventoryClickEvent event, ResetGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }

        ClickType click = event.getClick();
        ResetGui.handleClick(plugin, player, raw, holder.getMineName(),
                click.isLeftClick(), click.isRightClick(), click.isShiftClick());
    }

    public void handleRewardsGui(InventoryClickEvent event, RewardsGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }

        ClickType click = event.getClick();
        int rewardIndex = raw < 45 ? raw : -1;
        RewardsGui.handleClick(plugin, player, raw, holder.getMineName(),
                click.isLeftClick(), click.isRightClick(), rewardIndex);
    }

    public void handleConfirmDeleteGui(InventoryClickEvent event, ConfirmDeleteGuiHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!holder.getPlayerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }
        ConfirmDeleteGui.handleClick(plugin, player, raw, holder.getMineName());
    }
}
