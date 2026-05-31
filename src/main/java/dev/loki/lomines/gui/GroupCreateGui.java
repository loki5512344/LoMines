package dev.loki.lomines.gui;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mines;
import dev.loki.lomines.wand.GroupWandManager;
import dev.loki.lomines.wand.GroupWandSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 6-row chest: left column = create; center 3×3 = nine sub-mines with shared settings.
 */
public final class GroupCreateGui {

    /**
     * Create group (same YAML defaults for every created mine).
     */
    public static final int SLOT_CREATE = 0;
    /**
     * Shows /lm group prefix.
     */
    public static final int SLOT_INFO = 8;
    private static final int SIZE = 54;
    /**
     * 3×3 mine slots mapping index 0..8
     */
    private static final int[] MINE_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    private GroupCreateGui() {
    }

    public static void open(LoMinesPlugin plugin, Player player) {
        GroupCreateGuiHolder holder = new GroupCreateGuiHolder(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text("LoMines · группа (9 шахт)", NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);
        fill(plugin, player, inv);
        player.openInventory(inv);
    }

    public static void refresh(LoMinesPlugin plugin, Player player, Inventory inv) {
        fill(plugin, player, inv);
    }

    private static void fill(LoMinesPlugin plugin, Player player, Inventory inv) {
        GroupWandSession session = plugin.getGroupWandManager().getSession(player.getUniqueId());
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler());
        }
        inv.setItem(SLOT_CREATE, createButton());
        inv.setItem(SLOT_INFO, infoPaper(session));
        for (int i = 0; i < MINE_SLOTS.length; i++) {
            inv.setItem(MINE_SLOTS[i], mineSlotItem(session, i));
        }
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

    private static ItemStack createButton() {
        ItemStack stack = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Создать группу", NamedTextColor.GREEN));
            meta.lore(List.of(
                    Component.text("Создаёт шахты с префиксом_1 … _9", NamedTextColor.GRAY),
                    Component.text("для слотов, где заданы оба угла", NamedTextColor.GRAY)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack infoPaper(GroupWandSession session) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (session.hasBaseName()) {
                meta.displayName(Component.text("Префикс: " + session.getBaseName(), NamedTextColor.AQUA));
                meta.lore(List.of(
                        Component.text("Файлы: " + session.getBaseName() + "_1.yml … _9.yml", NamedTextColor.GRAY),
                        Component.text("Настройки одинаковые; правьте один и копируйте при нужде", NamedTextColor.DARK_GRAY)
                ));
            } else {
                meta.displayName(Component.text("Задайте префикс", NamedTextColor.RED));
                meta.lore(List.of(Component.text("/lm group <префикс>", NamedTextColor.GRAY)));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack mineSlotItem(GroupWandSession session, int index) {
        boolean active = session.getActiveSlot() == index;
        boolean ready = session.isSlotReady(index);
        Material mat;
        NamedTextColor nameColor;
        if (ready) {
            mat = Material.LIME_STAINED_GLASS_PANE;
            nameColor = NamedTextColor.GREEN;
        } else if (session.getPos1(index) != null) {
            mat = Material.YELLOW_STAINED_GLASS_PANE;
            nameColor = NamedTextColor.YELLOW;
        } else {
            mat = Material.RED_STAINED_GLASS_PANE;
            nameColor = NamedTextColor.RED;
        }
        if (active) {
            mat = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String label = "Шахта " + (index + 1);
            meta.displayName(Component.text(label, nameColor));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(active ? "Активный слот палочки" : "ПКМ — выбрать активным", NamedTextColor.GRAY));
            lore.add(Component.text(
                    ready ? "Готово (2 точки)" : (session.getPos1(index) != null ? "Нужна 2-я точка" : "Нужны 2 точки"),
                    NamedTextColor.DARK_GRAY));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static int mineSlotIndex(int rawSlot) {
        for (int i = 0; i < MINE_SLOTS.length; i++) {
            if (MINE_SLOTS[i] == rawSlot) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return true if handled (caller should cancel event)
     */
    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot) {
        if (rawSlot < 0 || rawSlot >= SIZE) {
            return false;
        }
        GroupWandManager mgr = plugin.getGroupWandManager();
        GroupWandSession session = mgr.getSession(player.getUniqueId());
        if (rawSlot == SLOT_CREATE) {
            runCreate(plugin, player, session);
            player.closeInventory();
            return true;
        }
        int mineIdx = mineSlotIndex(rawSlot);
        if (mineIdx >= 0) {
            session.setActiveSlot(mineIdx);
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof GroupCreateGuiHolder) {
                refresh(plugin, player, top);
            }
            player.sendMessage(Component.text("Активный слот палочки: " + (mineIdx + 1), NamedTextColor.YELLOW));
            return true;
        }
        return true;
    }

    private static void runCreate(LoMinesPlugin plugin, Player player, GroupWandSession session) {
        if (!session.hasBaseName()) {
            player.sendMessage(Component.text("Сначала: /lm group <префикс>", NamedTextColor.RED));
            return;
        }
        String base = session.getBaseName();
        if (!base.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(Component.text("Префикс: только латиница, цифры, _ -", NamedTextColor.RED));
            return;
        }
        Mines mines = plugin.getMines();
        int created = 0;
        int skipped = 0;
        for (int i = 0; i < 9; i++) {
            if (!session.isSlotReady(i)) {
                continue;
            }
            String mineName = base + "_" + (i + 1);
            try {
                mines.create(mineName, session.getPos1(i), session.getPos2(i));
                created++;
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Пропуск (уже есть): " + mineName, NamedTextColor.GOLD));
                skipped++;
            } catch (Exception e) {
                player.sendMessage(Component.text("Ошибка " + mineName + ": " + e.getMessage(), NamedTextColor.RED));
                plugin.loLogger().error("Group create failed for " + mineName, e);
            }
        }
        if (created == 0) {
            player.sendMessage(Component.text("Нет готовых слотов (2 точки в каждом). Пустые слоты пропущены.", NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text(
                "Создано шахт: " + created + (skipped > 0 ? " (пропущено существующих: " + skipped + ")" : ""),
                NamedTextColor.GREEN));
    }
}
