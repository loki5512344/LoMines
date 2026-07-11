package dev.loki.lomines.listener.gui;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.BlockMaterialSelectionGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.BlocksGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.reset.ResetGuiHolder;
import dev.loki.lomines.gui.mine.holder.edit.rewards.RewardsGuiHolder;
import dev.loki.lomines.gui.mine.holder.main.MineEditGuiHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

public final class MineEditGuiListener implements Listener {

    private final GuiActionHandler actionHandler;

    public MineEditGuiListener(LoMinesPlugin plugin) {
        this.actionHandler = new GuiActionHandler(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();

        if (view.getTopInventory().getHolder() instanceof MineEditGuiHolder holder) {
            actionHandler.handleMineEditGui(event, holder);
            return;
        }

        if (view.getTopInventory().getHolder() instanceof BlocksGuiHolder holder) {
            actionHandler.handleBlocksGui(event, holder);
            return;
        }

        if (view.getTopInventory().getHolder() instanceof BlockMaterialSelectionGuiHolder holder) {
            actionHandler.handleBlockMaterialSelectionGui(event, holder);
            return;
        }

        if (view.getTopInventory().getHolder() instanceof ResetGuiHolder holder) {
            actionHandler.handleResetGui(event, holder);
            return;
        }

        if (view.getTopInventory().getHolder() instanceof RewardsGuiHolder holder) {
            actionHandler.handleRewardsGui(event, holder);
            return;
        }

        if (view.getTopInventory().getHolder() instanceof ConfirmDeleteGuiHolder holder) {
            actionHandler.handleConfirmDeleteGui(event, holder);
        }
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
