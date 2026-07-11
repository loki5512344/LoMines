package dev.loki.lomines.gui.mine.edit.blocks.select;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.common.ItemStackFactory;
import dev.loki.lomines.gui.mine.edit.blocks.view.BlocksGui;
import dev.loki.lomines.gui.mine.holder.edit.BlockMaterialSelectionGuiHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public final class BlockMaterialSelectionGui {

    private static final int SIZE = 54;
    private static final String TITLE = "Выберите материал";
    private static final int SLOT_BACK = 49;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_NEXT = 53;
    private static final int ITEMS_PER_PAGE = 45;

    private BlockMaterialSelectionGui() {
    }

    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        open(plugin, player, mineName, 0);
    }

    public static void open(LoMinesPlugin plugin, Player player, String mineName, int page) {
        BlockMaterialSelectionGuiHolder holder = new BlockMaterialSelectionGuiHolder(
                player.getUniqueId(), mineName, page);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                TITLE + " (стр. " + (page + 1) + ")");
        holder.setInventory(inv);
        fill(inv, page);
        player.openInventory(inv);
    }

    private static void fill(Inventory inv, int page) {
        List<Material> materials = BlockMaterialSelector.getAvailableMaterials();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, ItemStackFactory.filler());
        }

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, materials.size());

        for (int i = 0; i < ITEMS_PER_PAGE && (start + i) < end; i++) {
            Material mat = materials.get(start + i);
            inv.setItem(i, BlockMaterialSelector.materialItem(mat));
        }

        if (page > 0) {
            inv.setItem(SLOT_PREV, BlockMaterialSelector.prevPageItem());
        }
        if (end < materials.size()) {
            inv.setItem(SLOT_NEXT, BlockMaterialSelector.nextPageItem());
        }

        inv.setItem(SLOT_BACK, BlockMaterialSelector.backItem());
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot,
                                       String mineName, int page, boolean leftClick) {
        if (rawSlot < 0 || rawSlot >= SIZE) {
            return false;
        }

        List<Material> materials = BlockMaterialSelector.getAvailableMaterials();

        if (rawSlot == SLOT_BACK) {
            BlocksGui.open(plugin, player, mineName);
            return true;
        }

        if (rawSlot == SLOT_PREV && page > 0) {
            open(plugin, player, mineName, page - 1);
            return true;
        }

        if (rawSlot == SLOT_NEXT && (page + 1) * ITEMS_PER_PAGE < materials.size()) {
            open(plugin, player, mineName, page + 1);
            return true;
        }

        if (rawSlot < ITEMS_PER_PAGE && leftClick) {
            int index = page * ITEMS_PER_PAGE + rawSlot;
            if (index < materials.size()) {
                Material selected = materials.get(index);
                player.sendMessage(Component.text("§aДобавлен блок: §f" + selected.name().toLowerCase()));
                player.sendMessage(Component.text("§7Вес установлен: §f10% (настройте в редакторе)"));
                BlocksGui.open(plugin, player, mineName);
            }
            return true;
        }

        return true;
    }

    public static Material getMaterialAtSlot(int slot, int page) {
        return BlockMaterialSelector.getMaterialAtSlot(slot, page, ITEMS_PER_PAGE);
    }
}
