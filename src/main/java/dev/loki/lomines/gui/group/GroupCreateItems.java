package dev.loki.lomines.gui.group;

import dev.loki.lomines.wand.group.GroupWandSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Item factory for GroupCreateGui.
 */
final class GroupCreateItems {

    private GroupCreateItems() {}

    static ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }
        return pane;
    }

    static ItemStack createButton() {
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

    static ItemStack infoPaper(GroupWandSession session) {
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

    static ItemStack mineSlotItem(GroupWandSession session, int index) {
        boolean active = session.getActiveSlot() == index;
        boolean ready = session.isSlotReady(index);

        Material mat = getMaterialForSlot(session, index, ready, active);
        NamedTextColor nameColor = ready ? NamedTextColor.GREEN :
                (session.getPos1(index) != null ? NamedTextColor.YELLOW : NamedTextColor.RED);

        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String label = "Шахта " + (index + 1);
            meta.displayName(Component.text(label, nameColor));
            meta.lore(buildLore(session, index, active, ready));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static Material getMaterialForSlot(GroupWandSession session, int index, boolean ready, boolean active) {
        if (active) {
            return Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        if (ready) {
            return Material.LIME_STAINED_GLASS_PANE;
        }
        if (session.getPos1(index) != null) {
            return Material.YELLOW_STAINED_GLASS_PANE;
        }
        return Material.RED_STAINED_GLASS_PANE;
    }

    private static List<Component> buildLore(GroupWandSession session, int index, boolean active, boolean ready) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(active ? "Активный слот палочки" : "ПКМ — выбрать активным", NamedTextColor.GRAY));
        lore.add(Component.text(
                ready ? "Готово (2 точки)" : (session.getPos1(index) != null ? "Нужна 2-я точка" : "Нужны 2 точки"),
                NamedTextColor.DARK_GRAY));
        return lore;
    }
}
