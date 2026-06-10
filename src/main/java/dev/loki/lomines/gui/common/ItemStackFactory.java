package dev.loki.lomines.gui.common;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating ItemStacks with name and lore.
 */
public final class ItemStackFactory {

    private ItemStackFactory() {
    }

    public static ItemStack create(Material mat, String name, String... loreLines) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.WHITE));
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Component.text(line, NamedTextColor.GRAY));
            }
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static String loc(Location l) {
        if (l == null || l.getWorld() == null) return "§7не задана";
        return String.format("§7%.0f§8/§7%.0f§8/§7%.0f", l.getX(), l.getY(), l.getZ());
    }

    public static String trunc(String s, int max) {
        if (s == null || s.isEmpty()) return "§7нет";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    public static ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
