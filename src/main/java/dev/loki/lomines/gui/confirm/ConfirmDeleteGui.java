package dev.loki.lomines.gui.confirm;

import dev.loki.lomines.LoMinesPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Confirmation GUI for deleting a mine.
 */
public final class ConfirmDeleteGui {

    private static final int SIZE = 27;
    private static final int SLOT_CONFIRM = 11;
    private static final int SLOT_CANCEL = 15;
    private static final int SLOT_INFO = 13;

    private ConfirmDeleteGui() {
    }

    /**
     * Opens the confirmation GUI for deleting a mine.
     *
     * @param plugin   the plugin instance
     * @param player   the player to open for
     * @param mineName the name of the mine to delete
     */
    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        ConfirmDeleteGuiHolder holder = new ConfirmDeleteGuiHolder(player.getUniqueId(), mineName);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text("§cПодтвердить удаление", NamedTextColor.DARK_RED));
        holder.setInventory(inv);

        fill(mineName, inv);
        player.openInventory(inv);
    }

    private static void fill(String mineName, Inventory inv) {
        // Fill background
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler());
        }

        inv.setItem(SLOT_INFO, infoItem(mineName));
        inv.setItem(SLOT_CONFIRM, confirmItem());
        inv.setItem(SLOT_CANCEL, cancelItem());
    }

    private static ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private static ItemStack infoItem(String mineName) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§cВы уверены?", NamedTextColor.RED));
            meta.lore(List.of(
                    Component.text("Шахта: §f" + mineName, NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§cЭто действие нельзя отменить!", NamedTextColor.RED),
                    Component.text("§7Все данные шахты будут удалены.", NamedTextColor.GRAY)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack confirmItem() {
        ItemStack stack = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lУДАЛИТЬ", NamedTextColor.GREEN));
            meta.lore(List.of(
                    Component.text("§cКликните для подтверждения удаления", NamedTextColor.RED)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack cancelItem() {
        ItemStack stack = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§lОТМЕНА", NamedTextColor.RED));
            meta.lore(List.of(
                    Component.text("§aКликните для отмены", NamedTextColor.GREEN)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Handles clicks in the confirmation GUI.
     *
     * @return true if the event should be cancelled
     */
    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot, String mineName) {
        if (rawSlot < 0 || rawSlot >= SIZE) {
            return false;
        }

        if (rawSlot == SLOT_CONFIRM) {
            try {
                plugin.getMines().delete(mineName);
                player.sendMessage(Component.text("§aШахта §f" + mineName + " §aуспешно удалена!", NamedTextColor.GREEN));
            } catch (Exception e) {
                player.sendMessage(Component.text("§cОшибка при удалении шахты: " + e.getMessage(), NamedTextColor.RED));
            }
            player.closeInventory();
            return true;
        }

        if (rawSlot == SLOT_CANCEL) {
            player.sendMessage(Component.text("§aУдаление отменено.", NamedTextColor.GREEN));
            player.closeInventory();
            return true;
        }

        return true; // Cancel all other clicks
    }
}
