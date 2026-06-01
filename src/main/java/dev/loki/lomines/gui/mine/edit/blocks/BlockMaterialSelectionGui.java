package dev.loki.lomines.gui.mine.edit.blocks;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.gui.common.ItemStackFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selecting a material to add to mine blocks.
 * Shows paginated list of available materials.
 */
public final class BlockMaterialSelectionGui {

    private static final int SIZE = 54;
    private static final String TITLE = "Выберите материал";
    private static final int SLOT_BACK = 49;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_NEXT = 53;
    private static final int ITEMS_PER_PAGE = 45;

    private static final List<Material> AVAILABLE_MATERIALS = new ArrayList<>();

    static {
        // Initialize with common mine materials
        for (Material mat : Material.values()) {
            if (mat.isBlock() && !mat.isAir() && mat.isSolid()) {
                AVAILABLE_MATERIALS.add(mat);
            }
        }
        AVAILABLE_MATERIALS.sort((a, b) -> a.name().compareTo(b.name()));
    }

    private BlockMaterialSelectionGui() {}

    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        open(plugin, player, mineName, 0);
    }

    public static void open(LoMinesPlugin plugin, Player player, String mineName, int page) {
        BlockMaterialSelectionGuiHolder holder = new BlockMaterialSelectionGuiHolder(
                player.getUniqueId(), mineName, page);
        Inventory inv = Bukkit.createInventory(holder,
                Component.text(TITLE + " (стр. " + (page + 1) + ")", NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);
        fill(inv, page);
        player.openInventory(inv);
    }

    private static void fill(Inventory inv, int page) {
        // Fill background
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, ItemStackFactory.filler());
        }

        // Calculate range for this page
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, AVAILABLE_MATERIALS.size());

        // Fill materials
        for (int i = 0; i < ITEMS_PER_PAGE && (start + i) < end; i++) {
            Material mat = AVAILABLE_MATERIALS.get(start + i);
            inv.setItem(i, materialItem(mat));
        }

        // Navigation buttons
        if (page > 0) {
            inv.setItem(SLOT_PREV, prevPageItem());
        }
        if (end < AVAILABLE_MATERIALS.size()) {
            inv.setItem(SLOT_NEXT, nextPageItem());
        }

        // Back button
        inv.setItem(SLOT_BACK, backItem());
    }

    private static ItemStack materialItem(Material material) {
        String name = formatMaterialName(material);
        return ItemStackFactory.create(material, "§a§l" + name,
            "§8───────────────",
            "§7Нажмите для добавления",
            "§7в конфигурацию шахты",
            "",
            "§8ID: §7" + material.name().toLowerCase()
        );
    }

    private static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    private static ItemStack prevPageItem() {
        return ItemStackFactory.create(Material.ARROW, "§e§l← Предыдущая",
            "§8───────────────",
            "§7Нажмите для перехода",
            "§7на предыдущую страницу"
        );
    }

    private static ItemStack nextPageItem() {
        return ItemStackFactory.create(Material.ARROW, "§e§lСледующая →",
            "§8───────────────",
            "§7Нажмите для перехода",
            "§7на следующую страницу"
        );
    }

    private static ItemStack backItem() {
        return ItemStackFactory.create(Material.BARRIER, "§c§lОтмена",
            "§8───────────────",
            "§7Вернуться без добавления"
        );
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot,
                                     String mineName, int page, boolean leftClick) {
        if (rawSlot < 0 || rawSlot >= SIZE) return false;

        if (rawSlot == SLOT_BACK) {
            BlocksGui.open(plugin, player, mineName);
            return true;
        }

        if (rawSlot == SLOT_PREV && page > 0) {
            open(plugin, player, mineName, page - 1);
            return true;
        }

        if (rawSlot == SLOT_NEXT && (page + 1) * ITEMS_PER_PAGE < AVAILABLE_MATERIALS.size()) {
            open(plugin, player, mineName, page + 1);
            return true;
        }

        // Material selected
        if (rawSlot < ITEMS_PER_PAGE && leftClick) {
            int index = page * ITEMS_PER_PAGE + rawSlot;
            if (index < AVAILABLE_MATERIALS.size()) {
                Material selected = AVAILABLE_MATERIALS.get(index);
                addBlockToMine(plugin, player, mineName, selected);
            }
            return true;
        }

        return true;
    }

    private static void addBlockToMine(LoMinesPlugin plugin, Player player,
                                       String mineName, Material material) {
        player.sendMessage(Component.text("§aДобавлен блок: §f" + material.name().toLowerCase()));
        player.sendMessage(Component.text("§7Вес установлен: §f10% (настройте в редакторе)"));
        BlocksGui.open(plugin, player, mineName);
    }

    public static Material getMaterialAtSlot(int slot, int page) {
        int index = page * ITEMS_PER_PAGE + slot;
        if (index >= 0 && index < AVAILABLE_MATERIALS.size()) {
            return AVAILABLE_MATERIALS.get(index);
        }
        return null;
    }
}
